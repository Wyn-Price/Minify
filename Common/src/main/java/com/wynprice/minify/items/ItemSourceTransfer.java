package com.wynprice.minify.items;

import com.wynprice.minify.Constants;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemSourceTransfer extends Item {

    public ItemSourceTransfer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack inHand = context.getItemInHand();
        BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());
        if(entity instanceof MinifySourceBlockEntity) {
            MinifyLocationKey locationKey = ((MinifySourceBlockEntity) entity).getLocationKey();
            MinifyLocationKey.toNBT(locationKey, inHand.getOrCreateTagElement("source_location"));
            return InteractionResult.SUCCESS;
        }
        if(entity instanceof MinifyViewerBlockEntity) {
            CompoundTag tagElement = inHand.getTagElement("source_location");
            if(tagElement != null) {
                MinifyLocationKey location = MinifyLocationKey.fromNBT(tagElement);
                ((MinifyViewerBlockEntity) entity).setSourceLocationKey(location);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
