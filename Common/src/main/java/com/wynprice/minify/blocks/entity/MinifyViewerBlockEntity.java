package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import com.wynprice.minify.network.C2SRequestViewerData;
import com.wynprice.minify.network.S2CSendViewerData;
import com.wynprice.minify.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.Optional;

public class MinifyViewerBlockEntity extends BaseMinifyBlockEntity {

    private MinifyLocationKey sourceLocationKey;

    private int[] signalsInDirections = new int[Direction.values().length];

    private PalettedContainer<BlockState> worldCache;
    private boolean hasClientRequestedData = false;

    public MinifyViewerBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_VIEWER_BLOCK_ENTITY, pos, state, MinifyChunkManager::setViewerLocation, MinifyChunkManager::onUnloadViewer);
    }

    public void setSourceLocationKey(MinifyLocationKey sourceLocationKey) {
        this.sourceLocationKey = sourceLocationKey;
        this.setChanged();
        if (this.level instanceof ServerLevel serverLevel) {
            MinifyChunkManager.getManager(serverLevel).copyTo(sourceLocationKey, this.locationKey);
            this.getOrGenerateWorldCache().ifPresent(cache ->
                Services.NETWORK.sendToAllAround(new S2CSendViewerData(this.getBlockPos(), cache), serverLevel, this.getBlockPos())
            );
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (this.sourceLocationKey != null) {
            tag.put("source_location_key", MinifyLocationKey.toNBT(this.sourceLocationKey, new CompoundTag()));
        }
        tag.putIntArray("signal_values", this.signalsInDirections);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("source_location_key", 10)) {
            this.locationKey = MinifyLocationKey.fromNBT(tag.getCompound("location_key"));
            this.setChanged();
        }
        this.signalsInDirections = tag.getIntArray("signal_values");

        //Ensure size
        if(this.signalsInDirections.length != Direction.values().length) {
            this.signalsInDirections = new int[6];
        }
        super.load(tag);
    }

    public final void updateRedstoneWall() {
        if (this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).updateRedstoneWall(this.locationKey, this.getBlockPos(), true);
        }
    }

    public final int getSignal(Direction direction) {
        return this.signalsInDirections[direction.ordinal()];
    }

    public final void setSignal(Direction direction, int signal) {
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
            ServerLevel dimension = this.level.getServer().getLevel(DimensionRegistry.WORLD_KEY);
            this.worldCache = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);

            BlockPos start = this.locationKey.chunk().getBlockAt(0, this.locationKey.yChunk() * 16, 0).offset(1, 1, 1);

            for (BlockPos offset : BlockPos.betweenClosed(0, 0, 0, 7, 7, 7)) {
                this.worldCache.set(offset.getX(), offset.getY(), offset.getZ(), dimension.getBlockState(start.offset(offset)));
            }
        }
        return Optional.ofNullable(this.worldCache);
    }

    public void requestOnClientIfNeeded() {
        if(!this.hasClientRequestedData && this.level.isClientSide) {
            this.hasClientRequestedData = true;
            Services.NETWORK.sendToServer(new C2SRequestViewerData(this.getBlockPos()));
        }
    }
}