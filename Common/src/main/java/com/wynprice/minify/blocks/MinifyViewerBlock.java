package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.items.MinifyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MinifyViewerBlock extends BaseEntityBlock {

    public MinifyViewerBlock(Properties properties) {
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
            if(entity instanceof MinifyViewerBlockEntity blockEntity) {
                blockEntity.removeFromData();
            }
        }
        super.onRemove(oldState, level, pos, newState, doDrops);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, MinifyBlockEntityTypes.MINIFICATION_VIEWER_BLOCK_ENTITY, MinifyViewerBlockEntity::clientTick) : null;
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity living, ItemStack stack) {
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
           blockEntity.setHorizontalRotationIndex(living.getDirection().get2DDataValue());
           blockEntity.updateRedstoneWall();
        }
        super.setPlacedBy(level, pos, state, living, stack);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        BlockEntity entity = level.getBlockEntity(pos);
        if(player.getItemInHand(hand).getItem() != MinifyItems.ITEM_SOURCE_TRANSFER && entity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.setHorizontalRotationIndex((blockEntity.getHorizontalRotationIndex() + 1) % 4);
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, result);
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
