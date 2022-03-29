package com.wynprice.minify.items;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.util.Registered;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MinifyItems {
    private static final List<Registered<Item>> ITEMS = new ArrayList<>();

    public static final BlockItem MINIFICATION_BLOCK = createFromBlock(MinifyBlocks.MINIFICATION_BLOCK, null);
    public static final BlockItem MINIFICATION_VIEWER_BLOCK = createFromBlock(MinifyBlocks.MINIFICATION_VIEWER_BLOCK, null);
    public static final BlockItem WALL_REDSTONE_BLOCK = createFromBlock(MinifyBlocks.WALL_REDSTONE_BLOCK, null);
    public static final ItemSourceTransfer ITEM_SOURCE_TRANSFER = create("item_source_transfer", new ItemSourceTransfer(new Item.Properties()));

    private static BlockItem createFromBlock(Block block, CreativeModeTab tab) {
        ResourceLocation key = Registry.BLOCK.getKey(block);
        BlockItem object = new BlockItem(block, new Item.Properties().tab(tab));
        return create(key.getPath(), object);
    }

    private static <T extends Item> T create(String name, T object) {
        ITEMS.add(new Registered<>(object, name));
        return object;
    }

    public static List<Registered<Item>> getItems() {
        return ITEMS;
    }
}
