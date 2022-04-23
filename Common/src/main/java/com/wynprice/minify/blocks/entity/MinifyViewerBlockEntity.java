package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.client.MinifyViewerClientLevel;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import com.wynprice.minify.management.MinifySourceKey;
import com.wynprice.minify.network.C2SRequestNestedData;
import com.wynprice.minify.network.C2SRequestViewerData;
import com.wynprice.minify.network.S2CSendViewerData;
import com.wynprice.minify.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.*;

public class MinifyViewerBlockEntity extends BlockEntity {

    private MinifyLocationKey locationKey;
    private MinifySourceKey sourceLocationKey;

    //0=NORTH, 1=EAST, 2=SOUTH, 3=WEST
    //Rotation is stored here instead of a state as eventually I want to
    //Do unlimited rotation, and don't want to have to deal with removing
    //state prop and replacing it with a index.
    private int horizontalRotationIndex = 0;
    private int[] signalsInDirections = new int[Direction.values().length];

    private PalettedContainer<BlockState> worldCache;
    private boolean hasClientRequestedData = false;

    //A map of <LocalPosition, BlockEntity>. In the server, the BlockEntity will be the same object as the actual block entity.
    private final Map<BlockPos, BlockEntity> blockEntityMap = new HashMap<>();

    public MinifyViewerBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_VIEWER_BLOCK_ENTITY, pos, state);
    }

    public void setSourceLocationKey(MinifySourceKey sourceLocationKey) {
        this.sourceLocationKey = sourceLocationKey;
        this.setChanged();
        if (this.level instanceof ServerLevel serverLevel) {
            MinifyChunkManager.getManager(serverLevel).copyTo(sourceLocationKey, this.locationKey);

            //Refresh the world cache, then send it to the client
            if(this.worldCache != null) {
                this.refreshWorldCache();
            }
            this.getOrGenerateWorldCache().ifPresent(cache ->
                Services.NETWORK.sendToAllAround(new S2CSendViewerData(this.getBlockPos(), Optional.empty(), cache, this.blockEntityMap), serverLevel, this.getBlockPos())
            );
        }
    }

    public MinifySourceKey getSourceLocationKey() {
        return sourceLocationKey;
    }

    public MinifyLocationKey getLocationKey() {
        return locationKey;
    }

    public Map<BlockPos, BlockEntity> getBlockEntityMap() {
        return blockEntityMap;
    }

    public void setHorizontalRotationIndex(int horizontalRotationIndex) {
        this.horizontalRotationIndex = horizontalRotationIndex;
    }

    public int getHorizontalRotationIndex() {
        return horizontalRotationIndex;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setToData() {
        if(this.level instanceof ServerLevel serverLevel) {
            if(this.locationKey == null) {
                this.locationKey = MinifyChunkManager.getManager(serverLevel).findNextLocation();
                this.setChanged();
            }
            MinifyChunkManager.getManager(serverLevel).setViewerLocation(this.locationKey, this.getBlockPos(), this);
        }
    }

    public void removeFromData() {
        if(this.level instanceof ServerLevel serverLevel && this.locationKey != null) {
            MinifyChunkManager.getManager(serverLevel).onUnloadViewer(this.locationKey);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if(this.locationKey != null) {
            tag.put("location_key", MinifyLocationKey.toNBT(this.locationKey, new CompoundTag()));
        }
        if (this.sourceLocationKey != null) {
            tag.put("source_location_key", MinifySourceKey.toNBT(this.sourceLocationKey, new CompoundTag()));
        }
        tag.putIntArray("signal_values", this.signalsInDirections);
        tag.putInt("horizontal_rotation_index", this.horizontalRotationIndex);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if(tag.contains("location_key", 10)) {
            this.locationKey = MinifyLocationKey.fromNBT(tag.getCompound("location_key"));
        }
        if (tag.contains("source_location_key", 10)) {
            this.sourceLocationKey = MinifySourceKey.fromNBT(tag.getCompound("source_location_key"));
            this.setChanged();
        }
        this.signalsInDirections = tag.getIntArray("signal_values");
        this.horizontalRotationIndex = tag.getInt("horizontal_rotation_index");

        //Ensure size
        if(this.signalsInDirections.length != Direction.values().length) {
            this.signalsInDirections = new int[6];
        }
        super.load(tag);
    }

    public final void updateRedstoneWall() {
        if (this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).updateRedstoneWall(this.locationKey, this.getBlockPos(), this, true);
        }
    }

    public final int getSignal(Direction direction) {
        return this.signalsInDirections[direction.ordinal()];
    }

    public final void setSignal(Direction direction, int signal) {
        if (signal == this.signalsInDirections[direction.ordinal()]) {
            return;
        }
        this.signalsInDirections[direction.ordinal()] = signal;
        this.setChanged();
        if(!this.level.isClientSide) {
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    public Optional<PalettedContainer<BlockState>> getOrGenerateWorldCache() {
        if(this.worldCache == null && this.level.isClientSide) {
            this.worldCache = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        }
        if(!this.level.isClientSide && this.worldCache == null && this.locationKey != null) {
            this.worldCache = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            this.refreshWorldCache();
        }
        return Optional.ofNullable(this.worldCache);
    }

    private void refreshWorldCache() {
        if(this.level.isClientSide) {
            return;
        }
        ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
        BlockPos start = this.locationKey.chunk().getBlockAt(0, this.locationKey.yChunk() * 16, 0).offset(1, 1, 1);

        for (BlockPos offset : BlockPos.betweenClosed(0, 0, 0, 7, 7, 7)) {
            BlockPos blockPos = start.offset(offset);
            this.worldCache.set(offset.getX(), offset.getY(), offset.getZ(), dimension.getBlockState(blockPos));
            BlockEntity blockEntity = dimension.getBlockEntity(blockPos);
            if(blockEntity != null) {
                this.blockEntityMap.put(offset.immutable(), blockEntity);
            } else {
                this.blockEntityMap.remove(offset.immutable());
            }
        }
    }

    public void requestOnClientIfNeeded() {
        if(!this.hasClientRequestedData && this.level.isClientSide) {
            this.hasClientRequestedData = true;
            Services.NETWORK.sendToServer(new C2SRequestViewerData(this.getBlockPos()));
        }
    }

    public void requestNestedClientIfNeeded() {
        if(!this.hasClientRequestedData && this.level instanceof MinifyViewerClientLevel minifyLevel) {
            this.hasClientRequestedData = true;
            Services.NETWORK.sendToServer(new C2SRequestNestedData(minifyLevel.getMainViewer().getBlockPos(), this.getBlockPos()));
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MinifyViewerBlockEntity blockEntity) {
        MinifyViewerClientLevel mLevel = MinifyViewerClientLevel.INSTANCE;
        //We only want to tick the "main" viewer
        if(mLevel.getMainViewer() == null) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache -> mLevel.injectAndRun(blockEntity, n -> mLevel.tickEntities()));
        }
    }
}
