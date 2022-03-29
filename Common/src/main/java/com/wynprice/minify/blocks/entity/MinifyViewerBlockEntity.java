package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.HashMap;
import java.util.Map;

public class MinifyViewerBlockEntity extends BaseMinifyBlockEntity {

    private MinifyLocationKey sourceLocationKey;

    private int[] signalsInDirections = new int[Direction.values().length];

    public MinifyViewerBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_VIEWER_BLOCK_ENTITY, pos, state, MinifyChunkManager::setViewerLocation, MinifyChunkManager::onUnloadViewer);
    }

    public void setSourceLocationKey(MinifyLocationKey sourceLocationKey) {
        this.sourceLocationKey = sourceLocationKey;
        this.setChanged();
        if (this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).copyTo(sourceLocationKey, this.locationKey);
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
}
