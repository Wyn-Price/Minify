package com.wynprice.minify.blocks;

import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MinifySourceBlock extends Block implements EntityBlock {
    public MinifySourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinifySourceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack stack = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(stack.getItem() == Items.NAME_TAG && stack.hasCustomHoverName() && blockEntity instanceof MinifySourceBlockEntity source) {
            source.setName(stack.getHoverName().getString());
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, result);
    }
}
