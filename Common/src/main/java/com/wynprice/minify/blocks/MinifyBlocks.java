package com.wynprice.minify.blocks;

import com.wynprice.minify.util.Registered;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;

public class MinifyBlocks {
    private static final List<Registered<Block>> BLOCKS = new ArrayList<>();

    public static final MinificationSourceBlock MINIFY_SOURCE = create(
        "minify_source",
        new MinificationSourceBlock(BlockBehaviour.Properties.of(Material.METAL))
    );


    public static final MinificationViewerBlock MINIFY_VIEWER = create(
        "minify_viewer",
        new MinificationViewerBlock(BlockBehaviour.Properties.of(Material.METAL).noOcclusion())
    );

    public static final WallRedstoneBlock MINIFY_CHUNK_WALL = create(
        "minify_chunk_wall",
        new WallRedstoneBlock(
            BlockBehaviour.Properties.of(Material.BARRIER)
                .strength(-1.0F, 3600000.8F)
                .noDrops()
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
