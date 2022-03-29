package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class MinifyViewerBlockEntity extends BaseMinifyBlockEntity {

    private MinifyLocationKey sourceLocationKey;

    public MinifyViewerBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_VIEWER_BLOCK_ENTITY, pos, state, MinifyChunkManager::setViewerLocation, MinifyChunkManager::onUnloadViewer);
    }


    public void setSourceLocationKey(MinifyLocationKey sourceLocationKey) {
        this.sourceLocationKey = sourceLocationKey;
        if (this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).copyTo(sourceLocationKey, this.locationKey);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (this.sourceLocationKey != null) {
            tag.put("source_location_key", MinifyLocationKey.toNBT(this.sourceLocationKey, new CompoundTag()));
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("source_location_key", 10)) {
            this.locationKey = MinifyLocationKey.fromNBT(tag.getCompound("location_key"));
        }
        super.load(tag);
    }

    public final void updateRedstoneWall() {
        if (this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).updateRedstoneWall(this.locationKey, this.getBlockPos(), true);
        }
    }
}
