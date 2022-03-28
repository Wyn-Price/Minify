package com.wynprice.minify.generation;

import com.wynprice.minify.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class DimensionRegistry {

    public static ResourceKey<Level> WORLD_KEY = ResourceKey.create(
        Registry.DIMENSION_REGISTRY,
        new ResourceLocation(Constants.MOD_ID, "data_world")
    );

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Constants.MOD_ID, "empty"), EmptyChunkGenerator.CODEC);
    }

}
