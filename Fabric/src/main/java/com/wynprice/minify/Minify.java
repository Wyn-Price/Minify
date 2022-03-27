package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.generation.EmptyChunkGenerator;
import com.wynprice.minify.util.Registered;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class Minify implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");

        register(Registry.BLOCK, MinifyBlocks.BLOCKS);

        DimensionRegistry.register();
    }

    private <T> void register(WritableRegistry<T> registry, List<Registered<T>> list) {
        for (Registered<T> registered : list) {
            Registry.register(registry, new ResourceLocation(Constants.MOD_ID, registered.name()), registered.object());
        }
    }
}
