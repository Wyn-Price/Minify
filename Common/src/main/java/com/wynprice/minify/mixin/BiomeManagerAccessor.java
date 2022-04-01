package com.wynprice.minify.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//We only need this on the client
@Mixin(BiomeManager.class)
public interface BiomeManagerAccessor {

    @Accessor("biomeZoomSeed")
    long accessor_getBiomeZoomSeed();
}
