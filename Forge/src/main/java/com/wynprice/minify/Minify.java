package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.generation.EmptyChunkGenerator;
import com.wynprice.minify.util.Registered;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
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

        modEventBus.addListener(Minify::init);
    }

    private static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(DimensionRegistry::register);
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