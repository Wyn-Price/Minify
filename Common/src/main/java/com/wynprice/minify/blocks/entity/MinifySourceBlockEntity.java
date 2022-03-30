package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MinifySourceBlockEntity extends BaseMinifyBlockEntity {

    public MinifySourceBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_SOURCE_BLOCK_ENTITY, pos, state, MinifyChunkManager::setSourceLocation, MinifyChunkManager::onUnloadSource);
    }

    @Override
    public void setToData() {
        super.setToData();
        if(this.level instanceof ServerLevel) {
            MinifyChunkManager.getManager((ServerLevel) this.level).sourcePlacedCopyOver(this.locationKey, this);
        }
    }

    public final BlockPos getOffset() {
        return this.getBlockPos().above();
    }

    public final AABB getArea() {
        return new AABB(this.getOffset(), this.getOffset().offset(8, 8, 8)); //8,8,8 box
    }
}
