package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.generation.EmptyChunkGenerator;
import com.wynprice.minify.util.Registered;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class Minify implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");

        register(Registry.BLOCK, MinifyBlocks::getBlocks);
        register(Registry.BLOCK_ENTITY_TYPE, MinifyBlockEntityTypes::getTypes);

        DimensionRegistry.register();
    }

    private <T> void register(Registry<T> registry, Supplier<List<Registered<T>>> list) {
        for (Registered<T> registered : list.get()) {
            Registry.register(registry, new ResourceLocation(Constants.MOD_ID, registered.name()), registered.object());
        }
    }
}
