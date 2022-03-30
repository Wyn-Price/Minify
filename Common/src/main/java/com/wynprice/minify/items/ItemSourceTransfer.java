package com.wynprice.minify.items;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.management.MinifyLocationKey;
import com.wynprice.minify.management.MinifySourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSourceTransfer extends Item {

    public ItemSourceTransfer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack inHand = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() == MinifyBlocks.MINIFY_SOURCE) {
            MinifySourceKey locationKey = new MinifySourceKey(level.dimension().location(), pos);
            MinifySourceKey.toNBT(locationKey, inHand.getOrCreateTagElement("source_location"));
            return InteractionResult.SUCCESS;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof MinifyViewerBlockEntity minifyViewerBlock) {
            CompoundTag tagElement = inHand.getTagElement("source_location");
            if(tagElement != null) {
                MinifySourceKey location = MinifySourceKey.fromNBT(tagElement);
                minifyViewerBlock.setSourceLocationKey(location);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
