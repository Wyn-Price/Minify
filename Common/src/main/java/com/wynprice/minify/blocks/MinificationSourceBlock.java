package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.BaseMinifyBlockEntity;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinificationSourceBlock extends BaseEntityBlock {
    public MinificationSourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean doDrops) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof BaseMinifyBlockEntity) {
                ((BaseMinifyBlockEntity) entity).removeFromData();
            }
        }
        super.onRemove(oldState, level, pos, newState, doDrops);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean drops) {
        BaseMinifyBlockEntity blockEntity = this.newBlockEntity(pos, state);
        level.setBlockEntity(blockEntity);
        blockEntity.setToData();
        super.onPlace(state, level, pos, oldState, drops);
    }


    @Override
    public BaseMinifyBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinifySourceBlockEntity(pos, state);
    }
}
