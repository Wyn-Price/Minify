package com.wynprice.minify.management;

import com.wynprice.minify.Constants;
import com.wynprice.minify.blocks.MinificationSourceBlock;
import com.wynprice.minify.blocks.MinificationViewerBlock;
import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.WallRedstoneBlock;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.generation.DimensionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class MinifyChunkManager extends SavedData {

    public static ThreadLocal<Boolean> isSilentlyPlacingIntoWorld = ThreadLocal.withInitial(() -> false);

    private final ServerLevel level;
    //TODO: Instead of keeping track of ALL the locations -> blockpos here,
    //In the data world, when a chunk (16x16x16) is being used, the bottom
    //left block can be a non air block, otherwise have no blocks there.
    private final Map<MinifyLocationKey, BlockPos> sources = new HashMap<>();
    private final Map<MinifyLocationKey, BlockPos> viewers = new HashMap<>();

    public MinifyChunkManager(ServerLevel level, CompoundTag tag) {
        this.level = level;

        if(tag != null) {
            this.readListToMap(tag.getList("sources", 10), this.sources);
            this.readListToMap(tag.getList("viewers", 10), this.viewers);
        }
    }

    public static MinifyChunkManager getManager(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            tag -> new MinifyChunkManager(level, tag),
            () -> new MinifyChunkManager(level, null),
            Constants.MOD_ID + ".minify_chunk_data");
    }

    public void setSourceLocation(MinifyLocationKey key, BlockPos pos) {
        this.sources.put(key, pos);
        this.updateRedstoneWall(key, pos, false);
        this.onLoad(key);
    }

    public void onUnloadSource(MinifyLocationKey key) {
        this.sources.remove(key);
        this.onUnload(key);
    }

    public void setViewerLocation(MinifyLocationKey key, BlockPos pos) {
        this.viewers.put(key, pos);
        this.updateRedstoneWall(key, pos, true);
        this.onLoad(key);
    }

    public void onUnloadViewer(MinifyLocationKey key) {
        this.viewers.remove(key);
        this.onUnload(key);
    }

    private void onLoad(MinifyLocationKey key) {
        this.setChunkForceLoaded(key.chunk(), true);
    }

    private void onUnload(MinifyLocationKey key) {
        boolean stillInUse = this.sources.containsKey(key) || this.viewers.containsKey(key);
        if(!stillInUse) {
            this.setChunkForceLoaded(key.chunk(), false);
        }
        this.clearChunk(key);
    }

    private void clearChunk(MinifyLocationKey key) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        int y = key.yChunk() * 16;
        for (BlockPos blockPos : BlockPos.betweenClosed(
            key.chunk().getBlockAt(0, y, 0),
            key.chunk().getBlockAt(15, y + 15, 15)
        )) {
            dimension.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private void setChunkForceLoaded(ChunkPos pos, boolean value) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        dimension.setChunkForced(pos.x, pos.z, value);
    }


    public void onBlockChangedAt(BlockPos pos, BlockState state) {
        if(DimensionRegistry.WORLD_KEY.equals(this.level.dimension())) {
            //Update clients
        } else {
            this.updateSource(pos, state);
        }
    }

    private void updateSource(BlockPos pos, BlockState state) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);

        this.sources.forEach((key, blockPos) -> {
            if(!this.level.isLoaded(blockPos)) {
                return;
            }
            BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
            if (!(blockEntity instanceof MinifySourceBlockEntity source)) {
                return;
            }
            if(source.getArea().contains(pos.getX(), pos.getY(), pos.getZ())) {
                BlockPos offset = pos.subtract(source.getOffset());
                BlockPos blockAt = key.chunk().getBlockAt(offset.getX(), key.yChunk() * 16 + offset.getY(), offset.getZ());
                dimension.setBlock(blockAt.offset(1, 1, 1), state, 18);
            }
        });
    }

    public void copyTo(MinifyLocationKey src, MinifyLocationKey dest) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);

        BlockPos srcStart = src.chunk().getBlockAt(0, src.yChunk() * 16, 0);
        BlockPos destStart = dest.chunk().getBlockAt(0, dest.yChunk() * 16, 0);

        for (BlockPos offset : BlockPos.betweenClosed(BlockPos.ZERO, BlockPos.ZERO.offset(15, 15, 15))) {
            dimension.setBlock(destStart.offset(offset), dimension.getBlockState(srcStart.offset(offset)), 3);
        }
    }

    public void updateRedstoneWall(MinifyLocationKey key, BlockPos pos, boolean useRedstoneLevels) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);

        BlockPos start = key.chunk().getBlockAt(0, key.yChunk() * 16, 0);

        int max = 9;

        for (Direction dir : Direction.values()) {
            int level = this.level.getSignal(pos.relative(dir), dir);

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

            for (BlockPos blockPos : BlockPos.betweenClosed(fromX, fromY, fromZ, toX, toY, toZ)) {
                dimension.setBlock(start.offset(blockPos), MinifyBlocks.WALL_REDSTONE_BLOCK.defaultBlockState().setValue(WallRedstoneBlock.LEVEL, useRedstoneLevels ? level : 0), 3);
            }


        }
    }

    public void sourcePlacedCopyOver(MinifyLocationKey src, MinifySourceBlockEntity blockEntity) {
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        BlockPos srcStart = src.chunk().getBlockAt(0, src.yChunk() * 16, 0).offset(1, 1, 1);

        isSilentlyPlacingIntoWorld.set(true);
        for (BlockPos relative : BlockPos.betweenClosed(0, 0, 0, 7, 7, 7)) {
            BlockPos blockPos = blockEntity.getOffset().offset(relative);
            dimension.setBlock(srcStart.offset(relative), this.level.getBlockState(blockPos), 18);
        }
        isSilentlyPlacingIntoWorld.set(false);
    }

    //https://stackoverflow.com/a/3706260
    //Spiral search to find next chunk
    public MinifyLocationKey findNextLocation() {
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
                if(!this.sources.containsKey(testKey)) {
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

    private void readListToMap(ListTag tag, Map<MinifyLocationKey, BlockPos> map) {
        for (Tag t : tag) {
            CompoundTag compound = (CompoundTag) t;
            map.put(
                MinifyLocationKey.fromNBT(compound.getCompound("key")),
                NbtUtils.readBlockPos(compound.getCompound("pos"))
            );
        }
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
        tag.put("sources", this.writeMapToList(this.sources));
        tag.put("viewers", this.writeMapToList(this.viewers));
        return tag;
    }

}
