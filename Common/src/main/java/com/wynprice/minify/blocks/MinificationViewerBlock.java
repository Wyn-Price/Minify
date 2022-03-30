package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MinificationViewerBlock extends BaseEntityBlock {

    public MinificationViewerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos posFrom, boolean var) {
        BlockEntity entity = level.getBlockEntity(pos);

        if(entity instanceof MinifyViewerBlockEntity) {
            ((MinifyViewerBlockEntity) entity).updateRedstoneWall();
        }
        super.neighborChanged(state, level, pos, block, pos, var);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean doDrops) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof MinifyViewerBlockEntity) {
                ((MinifyViewerBlockEntity) entity).removeFromData();
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
        MinifyViewerBlockEntity blockEntity = this.newBlockEntity(pos, state);
        level.setBlockEntity(blockEntity);
        blockEntity.setToData();
        super.onPlace(state, level, pos, oldState, drops);
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
    public VoxelShape getVisualShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState $$0, BlockGetter $$1, BlockPos $$2) {
        return true;
    }


    @Override
    public MinifyViewerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinifyViewerBlockEntity(pos, state);
    }
}
