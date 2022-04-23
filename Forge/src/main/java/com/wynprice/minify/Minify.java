package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.client.MinifySourceBlockEntityRenderer;
import com.wynprice.minify.client.MinifyViewerBlockEntityRenderer;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.items.CreativeTabHolder;
import com.wynprice.minify.items.MinifyItems;
import com.wynprice.minify.network.MinifyNetworkRegistry;
import com.wynprice.minify.util.Registered;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class Minify {
    
    public Minify() {
        Constants.LOG.info("Hello Forge world!");

        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();

        register(modEventBus, Block.class, MinifyBlocks::getBlocks);
        register(modEventBus, (Class<BlockEntityType<?>>) (Object) BlockEntityType.class, MinifyBlockEntityTypes::getTypes);
        register(modEventBus, Item.class, MinifyItems::getItems);

        modEventBus.addListener(Minify::init);
        modEventBus.addListener(Minify::clientInit);
        modEventBus.addListener(Minify::registerBlockEntityRenders);

        CreativeTabHolder.TAB = new CreativeModeTab(String.format("%s.%s", Constants.MOD_ID, "items")) {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(MinifyItems.ITEM_SOURCE_TRANSFER);
            }
        };
    }

    private static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DimensionRegistry.register();
            MinifyNetworkRegistry.registerPackets();
        });

    }

    private static void clientInit(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(MinifyBlocks.MINIFY_CHUNK_WALL, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(MinifyBlocks.MINIFY_VIEWER, RenderType.cutout());
    }

    private static void registerBlockEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(MinifyBlockEntityTypes.MINIFY_VIEWER_BLOCK_ENTITY, MinifyViewerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(MinifyBlockEntityTypes.MINIFY_SOURCE_BLOCK_ENTITY, c -> new MinifySourceBlockEntityRenderer());
    }

    private <T extends IForgeRegistryEntry<T>> void register(IEventBus modEventBus, Class<T> clazz, Supplier<List<Registered<T>>> list) {
        modEventBus.addGenericListener(clazz, (RegistryEvent.Register<T> event) -> {
            for (Registered<T> registered : list.get()) {
                registered.object().setRegistryName(new ResourceLocation(Constants.MOD_ID, registered.name()));
                event.getRegistry().register(registered.object());
            }
        });
    }
}