package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.generation.EmptyChunkGenerator;
import com.wynprice.minify.items.CreativeTabHolder;
import com.wynprice.minify.items.MinifyItems;
import com.wynprice.minify.network.MinifyNetworkRegistry;
import com.wynprice.minify.util.Registered;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.mixin.blockrenderlayer.MixinBlockRenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class Minify implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");

        CreativeTabHolder.TAB = FabricItemGroupBuilder.build(new ResourceLocation(Constants.MOD_ID, "items"), () -> new ItemStack(MinifyItems.ITEM_SOURCE_TRANSFER));

        register(Registry.BLOCK, MinifyBlocks::getBlocks);
        register(Registry.BLOCK_ENTITY_TYPE, MinifyBlockEntityTypes::getTypes);
        register(Registry.ITEM, MinifyItems::getItems);

        DimensionRegistry.register();
        MinifyNetworkRegistry.registerPackets();
    }

    

    private <T> void register(Registry<T> registry, Supplier<List<Registered<T>>> list) {
        for (Registered<T> registered : list.get()) {
            Registry.register(registry, new ResourceLocation(Constants.MOD_ID, registered.name()), registered.object());
        }
    }
}
