package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MinificationViewerBlock extends MinificationSourceBlock {
    public MinificationViewerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos posFrom, boolean var) {
        BlockEntity entity = level.getBlockEntity(pos);
        //TODO: only update a direction, instead of everything
        if(entity instanceof MinifyViewerBlockEntity) {
            ((MinifyViewerBlockEntity) entity).updateRedstoneWall();
        }
        super.neighborChanged(state, level, pos, block, pos, var);
    }


    @Override
    public MinifyViewerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinifyViewerBlockEntity(pos, state);
    }
}
