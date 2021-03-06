package com.wynprice.minify.management;

import com.wynprice.minify.Constants;
import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.WallRedstoneBlock;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.network.S2CMinifiyBlockEvent;
import com.wynprice.minify.network.S2CUpdateViewerBlockEntityData;
import com.wynprice.minify.network.S2CUpdateViewerData;
import com.wynprice.minify.platform.Services;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MinifyChunkManager extends SavedData {

    public static ThreadLocal<Boolean> isSilentlyPlacingIntoWorld = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<Boolean> isUpdatingRedstoneWall = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<Boolean> isPlayingMinifiedSound = ThreadLocal.withInitial(() -> false);

    public static ThreadLocal<Stack<MinifyLocationKey>> currentlyProcessedKeys = ThreadLocal.withInitial(Stack::new);
    private final ServerLevel level;

    private final Map<MinifyLocationKey, BlockPos> viewers = new HashMap<>();

    private final Map<MinifyLocationKey, IntSet> scheduledUpdates = new HashMap<>();

    public MinifyChunkManager(ServerLevel level, CompoundTag tag) {
        this.level = level;

        if(tag != null) {
            for (Tag t : tag.getList("viewers", 10)) {
                CompoundTag compound = (CompoundTag) t;
                this.viewers.put(
                    MinifyLocationKey.fromNBT(compound.getCompound("key")),
                    NbtUtils.readBlockPos(compound.getCompound("pos"))
                );
            }
        }
    }

    public static MinifyChunkManager getManager(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            tag -> new MinifyChunkManager(level, tag),
            () -> new MinifyChunkManager(level, null),
            Constants.MOD_ID + ".minify_chunk_data"
        );
    }

    public void setViewerLocation(MinifyLocationKey key, BlockPos pos, MinifyViewerBlockEntity blockEntity) {
        //Do we need this? The onRemove method shoud always remove it.
        for (ServerLevel serverLevel : this.level.getServer().getAllLevels()) {
            MinifyChunkManager manager = getManager(serverLevel);
            if(manager.viewers.containsKey(key)) {
                this.onUnloadViewer(key);
            }
        }

        this.viewers.put(key, pos);
        this.updateRedstoneWall(key, pos, blockEntity, true);
        this.onLoad(key);
        this.setDirty();
    }

    public void onUnloadViewer(MinifyLocationKey key) {
        this.viewers.remove(key);
        boolean stillInUse = StreamSupport.stream(this.level.getServer().getAllLevels().spliterator(), false)
            .flatMap(level -> MinifyChunkManager.getManager(level).viewers.keySet().stream())
            .anyMatch(k -> k.chunk().equals(key.chunk()));
        if(!stillInUse) {
            this.setChunkForceLoaded(key.chunk(), false);
        }
        this.clearChunk(key, true);
        this.setDirty();
    }

    private void onLoad(MinifyLocationKey key) {
        this.setChunkForceLoaded(key.chunk(), true);
    }

    private void clearChunk(MinifyLocationKey key, boolean clearWalls) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        int y = key.yChunk() * 16;

        isSilentlyPlacingIntoWorld.set(true);

        int f = clearWalls ? 0 : 1;
        int t = clearWalls ? 9 : 8;

        for (BlockPos blockPos : BlockPos.betweenClosed(
            key.chunk().getBlockAt(f, y + f, f),
            key.chunk().getBlockAt(t, y + t, t)
        )) {
            dimension.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }

        isSilentlyPlacingIntoWorld.set(false);
    }

    public void onSoundPlayed(double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if(DimensionRegistry.WORLD_KEY.equals(this.level.dimension()) && !isPlayingMinifiedSound.get()) {
            isPlayingMinifiedSound.set(true);
            int xPos = Mth.floor(x) >> 4;
            int yPos = Mth.floor(y) >> 4;
            int zPos = Mth.floor(z) >> 4;
            MinifyLocationKey key = new MinifyLocationKey(new ChunkPos(xPos, zPos), yPos);
            this.findViewerForKey(key).ifPresent(blockEntity -> {
                blockEntity.getLevel().playSound(null, blockEntity.getBlockPos(), sound, source, volume * 0.25F, pitch + 1F);
            });
            isPlayingMinifiedSound.set(false);
        }
    }

    private void setChunkForceLoaded(ChunkPos pos, boolean value) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        if (dimension.getForcedChunks().contains(pos.toLong()) != value) {
            Constants.LOG.debug((value ? "Began" : "Stopped") + " force loading chunk at " + pos);
        }
        dimension.setChunkForced(pos.x, pos.z, value);
    }

    public void onTick() {
        this.scheduledUpdates.forEach((key, updates) -> {
            Optional<MinifyViewerBlockEntity> viewer = this.findViewerForKey(key);
            if(viewer.isEmpty() || updates.isEmpty()) {
                return;
            }

            MinifyViewerBlockEntity blockEntity = viewer.get();
            BlockPos basePos = key.chunk().getBlockAt(0, key.yChunk() * 16, 0).offset(1, 1, 1);

            var updateData = updates.intStream().mapToObj(p -> {
                BlockPos position = this.decode(p);
                BlockPos worldPosition = basePos.offset(position);

                BlockState state = this.level.getBlockState(worldPosition);
                BlockEntity entity = this.level.getBlockEntity(worldPosition);
                if (entity != null) {
                    blockEntity.getBlockEntityMap().put(position, entity);
                    Services.NETWORK.sendToAllAround(new S2CUpdateViewerBlockEntityData(blockEntity.getBlockPos(), position, entity.getUpdateTag()), (ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos());
                } else {
                    blockEntity.getBlockEntityMap().remove(position);
                }
                return new S2CUpdateViewerData.UpdateData(position, state);
            }).toList();

            Services.NETWORK.sendToAllAround(new S2CUpdateViewerData(blockEntity.getBlockPos(), updateData), (ServerLevel) blockEntity.getLevel(), blockEntity.getBlockPos());
        });
        this.scheduledUpdates.clear();
    }

    private int encode(BlockPos pos) {
        //inner has range 0,1,2,3,4,5,6,7
        return (pos.getX() & 7) << 6 | (pos.getY() & 7) << 3 | (pos.getZ() & 7);
    }

    private BlockPos decode(int value) {
        return new BlockPos(
            (value >> 6) & 7,
            (value >> 3) & 7,
            (value) & 7
        );
    }

    public void onBlockChangedAt(BlockPos pos) {
        if(!DimensionRegistry.WORLD_KEY.equals(this.level.dimension()) && !isSilentlyPlacingIntoWorld.get()) {
            return;
        }
        BlockState state = this.level.getBlockState(pos);
        this.getViewerForDataDimensionPosition(pos).ifPresent(location -> {
            //Update the viewer signals
            if(!isUpdatingRedstoneWall.get()) {
                for (Direction value : Direction.values()) {
                    this.updateViewerSignal(location.locationKey(), value);
                }
            }


            this.scheduledUpdates.computeIfAbsent(location.locationKey(), k -> new IntArraySet()).add(this.encode(location.position));
            location.blockEntity().getOrGenerateWorldCache().ifPresent(blockStatePalettedContainer ->
                blockStatePalettedContainer.set(location.position.getX(), location.position.getY(), location.position.getZ(), state)
            );
        });

    }

    private Optional<ViewerLocation> getViewerForDataDimensionPosition(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        int chunkY = pos.getY() >> 4;
        int innerX = pos.getX() & 15;
        int innerY = pos.getY() & 15;
        int innerZ = pos.getZ() & 15;
        MinifyLocationKey key = new MinifyLocationKey(chunkPos, chunkY);
        boolean isInRange = innerX >= 1 && innerY >= 1 && innerZ >= 1 && innerX <= 8 && innerY <= 8 && innerZ <= 8;
        if(isInRange) {
            return this.findViewerForKey(key).map(blockEntity -> new ViewerLocation(blockEntity, new BlockPos(innerX-1, innerY-1, innerZ-1), key));
        }
        return Optional.empty();
    }

    public static record ViewerLocation(MinifyViewerBlockEntity blockEntity, BlockPos position, MinifyLocationKey locationKey) {}

    public Optional<Integer> getRawBrightness(BlockPos pos, int num) {
        if(this.level.dimension() != DimensionRegistry.WORLD_KEY) {
            return Optional.empty();
        }
        return this.getViewerForDataDimensionPosition(pos)
            .map(location -> location.blockEntity().getLevel().getRawBrightness(location.blockEntity().getBlockPos(), num));
    }

    public Optional<Integer> getBrightness(LightLayer layer, BlockPos pos) {
        if(this.level.dimension() != DimensionRegistry.WORLD_KEY) {
            return Optional.empty();
        }
        return this.getViewerForDataDimensionPosition(pos)
            .map(location -> location.blockEntity().getLevel().getBrightness(layer, location.blockEntity().getBlockPos()));
    }

    public void doBlockEvent(BlockEventData eventData) {
        BlockPos pos = eventData.pos();
        this.getViewerForDataDimensionPosition(pos).ifPresent(location ->
            Services.NETWORK.sendToAllAround(
                new S2CMinifiyBlockEvent(
                    location.blockEntity().getBlockPos(), location.position(),
                    eventData.paramA(), eventData.paramB()
                ),
                (ServerLevel) location.blockEntity().getLevel(),
                location.blockEntity().getBlockPos()
            )
        );
    }

    private Optional<ServerLevel> findFromKey(MinifySourceKey key) {
        if(key == null) {
            return Optional.empty();
        }
        for (ServerLevel level : this.level.getServer().getAllLevels()) {
            if (level.dimension().location().equals(key.dimension())) {
                return Optional.of(level);
            }
        }
        return Optional.empty();
    }

    public String getName(MinifySourceKey src) {
        Optional<ServerLevel> levelOptional = this.findFromKey(src);
        if(levelOptional.isPresent() && levelOptional.get().getBlockEntity(src.pos()) instanceof MinifySourceBlockEntity blockEntity) {
            return blockEntity.getName();
        }
        return "";
    }

    public void copyTo(MinifySourceKey src, MinifyLocationKey dest) {
        this.clearChunk(dest, false);
        Optional<ServerLevel> levelOptional = this.findFromKey(src);
        if(levelOptional.isEmpty()) {
            return;
        }
        ServerLevel level = levelOptional.get();

        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);

        BlockPos start = src.pos().above(); //In the future this should be more?
        BlockPos destStart = dest.chunk().getBlockAt(0, dest.yChunk() * 16, 0).offset(1, 1, 1);

        isSilentlyPlacingIntoWorld.set(true);

        for (BlockPos offset : BlockPos.betweenClosed(0, 0, 0, 7, 7, 7)) {
            BlockPos destPos = destStart.offset(offset);
            BlockPos srcPos = start.offset(offset);
            BlockState state = level.getBlockState(srcPos);

            dimension.setBlock(destPos, state, 3);

            BlockEntity entity = level.getBlockEntity(srcPos);
            if(entity instanceof MinifyViewerBlockEntity minifyViewerBlock) {
                MinifyLocationKey locationKey = minifyViewerBlock.getLocationKey();
                Stack<MinifyLocationKey> stack = currentlyProcessedKeys.get();
                if(stack.contains(locationKey)) {
                    Constants.LOG.warn("Location Key is Inside Itself? {} @ {}. Stack={}", locationKey, srcPos, stack);
                } else {
                    BlockEntity rawBlockEntity = dimension.getBlockEntity(destPos);
                    if(rawBlockEntity instanceof MinifyViewerBlockEntity blockEntity) {
                        //setSourceLocationKey will call this method (copyTo), hence recursively copying the data
                        stack.push(locationKey);
                        blockEntity.forceSetHorizontalRotationIndex(minifyViewerBlock.getHorizontalRotationIndex());
                        blockEntity.setName(minifyViewerBlock.getName());
                        blockEntity.setToData();
                        blockEntity.setSourceLocationKey(minifyViewerBlock.getSourceLocationKey());
                        stack.pop();
                    }

                }

            } else if(entity != null) {
                BlockEntity blockEntity = BlockEntity.loadStatic(
                    destPos, state,
                    entity.saveWithId()
                );
                dimension.setBlockEntity(blockEntity);
            }
        }


        isSilentlyPlacingIntoWorld.set(false);

        //Update the viewer signals
        for (Direction value : Direction.values()) {
            this.updateViewerSignal(dest, value);
        }
    }

    public void updateRedstoneWall(MinifyLocationKey key, BlockPos pos, MinifyViewerBlockEntity blockEntity, boolean useRedstoneLevels) {

        isUpdatingRedstoneWall.set(true);
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);

        BlockPos start = key.chunk().getBlockAt(0, key.yChunk() * 16, 0);

        for (Direction rawDir : Direction.values()) {
            int level = Mth.clamp(this.level.getSignal(pos.relative(rawDir), rawDir) - 1, 0, 15);
            Direction dir = rawDir;
            if(dir.getAxis().isHorizontal()) {
                for (int i = 0; i < 4 - blockEntity.getHorizontalRotationIndex(); i++) {
                    dir = dir.getCounterClockWise();
                }
            }
            for (BlockPos blockPos : getAllBlocksForSide(dir)) {
                dimension.setBlock(start.offset(blockPos), MinifyBlocks.MINIFY_CHUNK_WALL.defaultBlockState().setValue(WallRedstoneBlock.LEVEL, useRedstoneLevels ? level : 0), 2);
            }
        }
        for (Direction dir : Direction.values()) {
            for (BlockPos blockPos : getAllBlocksForSide(dir)) {
                dimension.updateNeighborsAt(start.offset(blockPos).relative(dir.getOpposite()), MinifyBlocks.MINIFY_CHUNK_WALL);
            }
        }
        isUpdatingRedstoneWall.set(false);
        for (Direction value : Direction.values()) {
            this.updateViewerSignal(key, value);
        }
    }


    public void updateViewerSignal(MinifyLocationKey key, Direction direction) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        //this.level is the data world
        Optional<MinifyViewerBlockEntity> blockEntity = this.findViewerForKey(key);
        if(blockEntity.isEmpty()) {
            return;
        }

        BlockPos start = key.chunk().getBlockAt(0, key.yChunk() * 16, 0);

        int maxSignal = 0;
        for (BlockPos pos : getAllBlocksForSide(direction.getOpposite())) {
            int signal = dimension.getSignal(pos.offset(start).relative(direction), direction);
            maxSignal = Math.max(maxSignal, signal);
        }

        if(direction.getAxis().isHorizontal()) {
            for (int i = 0; i < blockEntity.get().getHorizontalRotationIndex(); i++) {
                direction = direction.getCounterClockWise();
            }

        }
        blockEntity.get().setSignal(direction, Mth.clamp(maxSignal - 1, 0, 15));
    }

    //TODO: A better way to doing this
    private Optional<MinifyViewerBlockEntity> findViewerForKey(MinifyLocationKey key) {
        for (ServerLevel serverLevel : this.level.getServer().getAllLevels()) {
            MinifyChunkManager manager = getManager(serverLevel);
            if(manager.viewers.containsKey(key)) {
                BlockEntity entity = serverLevel.getBlockEntity(manager.viewers.get(key));
                if(entity instanceof MinifyViewerBlockEntity be) {
                    return Optional.of(be);
                }
            }
        }
        return Optional.empty();
    }

    private static Iterable<BlockPos> getAllBlocksForSide(Direction dir) {
        int max = 9;

        //We need to set the blocks in the given plane now.
        //The planes start at 0,0,0, and end at 9,9,9 (inc), as the inner area is a 8x8x8 block.
        //
        //-x    0,0,0 -> 0,1,1  west
        //+x    1,0,0 -> 1,1,1  east
        //-y    0,0,0 -> 1,0,1  down
        //+y    0,1,0 -> 1,1,1  up
        //-z    0,0,0 -> 1,1,0  north
        //+z    0,0,1 -> 1,1,1  south

        int fromX = dir == Direction.EAST ? max : 0;
        int toX = dir == Direction.WEST ? 0 : max;

        int fromY = dir == Direction.UP ? max : 0;
        int toY = dir == Direction.DOWN ? 0 : max;

        int fromZ = dir == Direction.SOUTH ? max : 0;
        int toZ = dir == Direction.NORTH ? 0 : max;

        //Clip the last blocks of the edges of the axis
        if(dir.getAxis() != Direction.Axis.X) {
            fromX += 1;
            toX -= 1;
        }
        if(dir.getAxis() != Direction.Axis.Y) {
            fromY += 1;
            toY -= 1;
        }
        if(dir.getAxis() != Direction.Axis.Z) {
            fromZ += 1;
            toZ -= 1;
        }

        return BlockPos.betweenClosed(fromX, fromY, fromZ, toX, toY, toZ);
    }

    //https://stackoverflow.com/a/3706260
    //Spiral search to find next chunk
    public MinifyLocationKey findNextLocation() {
        Set<MinifyLocationKey> allViewers = StreamSupport.stream(this.level.getServer().getAllLevels().spliterator(), false)
            .flatMap(s -> getManager(s).viewers.keySet().stream())
            .collect(Collectors.toSet());
        // (dx, dz) is a vector - direction in which we move right now
        int dx = 0;
        int dz = 1;
        // length of current segment
        int segment_length = 1;

        // current position (x, z) and how much of current segment we passed
        int x = 0;
        int z = 0;
        int segment_passed = 0;

        do {
            for(int i = 0; i < 16; i++) {
                MinifyLocationKey testKey = new MinifyLocationKey(new ChunkPos(x, z), i);
                if(!allViewers.contains(testKey)) {
                    return testKey;
                }
            }

            // make a step, add 'direction' vector (dx, dz) to current position (x, z)
            x += dx;
            z += dz;

            if (++segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = dz;
                dz = -dx;
                dx = buffer;

                // increase segment length if necessary
                if (dx == 0) {
                    ++segment_length;
                }
            }
        } while (true);
    }

    private ListTag writeMapToList(Map<MinifyLocationKey, BlockPos> map) {
        ListTag list = new ListTag();
        map.forEach((key, pos) -> {
            CompoundTag tag = new CompoundTag();
            tag.put("key", MinifyLocationKey.toNBT(key, new CompoundTag()));
            tag.put("pos", NbtUtils.writeBlockPos(pos));
            list.add(tag);
        });
        return list;
    }
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("viewers", this.writeMapToList(this.viewers));
        return tag;
    }

}
