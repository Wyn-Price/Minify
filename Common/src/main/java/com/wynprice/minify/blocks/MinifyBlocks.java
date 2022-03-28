package com.wynprice.minify.blocks;

import com.wynprice.minify.util.Registered;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;

public class MinifyBlocks {
    private static final List<Registered<Block>> BLOCKS = new ArrayList<>();

    public static final MinificationBlock MINIFICATION_BLOCK = create("minification_block", new MinificationBlock(BlockBehaviour.Properties.of(Material.METAL)));

    private static <T extends Block> T create(String name, T object) {
        BLOCKS.add(new Registered<>(object, name));
        return object;
    }

    public static List<Registered<Block>> getBlocks() {
        return BLOCKS;
    }
}
