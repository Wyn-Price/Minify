package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
            return blockEntity.getSignal(direction);
        }
        return 0;
    }


    @Override
    public MinifyViewerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinifyViewerBlockEntity(pos, state);
    }
}
