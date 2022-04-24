package com.wynprice.minify.items;

import com.wynprice.minify.Constants;
import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.management.MinifyLocationKey;
import com.wynprice.minify.management.MinifySourceKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
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
        BlockEntity entity = level.getBlockEntity(pos);

        if(entity instanceof MinifySourceBlockEntity minifySourceBlock) {
            MinifySourceKey locationKey = new MinifySourceKey(level.dimension().location(), pos);
            MinifySourceKey.toNBT(locationKey, inHand.getOrCreateTagElement("source_location"));
            if(!minifySourceBlock.getName().isBlank()) {
                inHand.getOrCreateTag().putString("fallback_name", minifySourceBlock.getName());
            }
            return InteractionResult.SUCCESS;
        }

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

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement("source_location");
        String name = null;

        if (MinifySourceKey.hasKey(tag)) {
            MinifySourceKey src = MinifySourceKey.fromNBT(tag);
            ClientLevel level = Minecraft.getInstance().level;
            if(src.dimension().equals(level.dimension().location()) && level.isLoaded(src.pos()) && level.getBlockEntity(src.pos()) instanceof MinifySourceBlockEntity minifySourceBlock && !minifySourceBlock.getName().isBlank()) {
               name = minifySourceBlock.getName();
            }
        }

        if(name == null || name.isBlank()) {
            name = stack.getOrCreateTag().getString("fallback_name");
        }

        if(!name.isBlank()) {
            return new TranslatableComponent(Constants.MOD_ID + ".item.source_transfer.named", name);
        }

        return super.getName(stack);
    }
}
