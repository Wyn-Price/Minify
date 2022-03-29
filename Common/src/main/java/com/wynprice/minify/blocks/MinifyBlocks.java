package com.wynprice.minify.blocks;

import com.wynprice.minify.util.Registered;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;

public class MinifyBlocks {
    private static final List<Registered<Block>> BLOCKS = new ArrayList<>();

    public static final MinificationSourceBlock MINIFICATION_BLOCK = create(
        "minification_source_block",
        new MinificationSourceBlock(BlockBehaviour.Properties.of(Material.METAL))
    );


    public static final MinificationViewerBlock MINIFICATION_VIEWER_BLOCK = create(
        "minification_viewer_block",
        new MinificationViewerBlock(BlockBehaviour.Properties.of(Material.METAL))
    );

    public static final WallRedstoneBlock WALL_REDSTONE_BLOCK = create(
        "wall_redstone_block",
        new WallRedstoneBlock(
            BlockBehaviour.Properties.of(Material.BARRIER)
                .strength(-1.0F, 3600000.8F)
                .noDrops()
                .noOcclusion()
                .isValidSpawn((var1, var2, var3, var4) -> false)
        )
    );

    private static <T extends Block> T create(String name, T object) {
        BLOCKS.add(new Registered<>(object, name));
        return object;
    }

    public static List<Registered<Block>> getBlocks() {
        return BLOCKS;
    }
}
