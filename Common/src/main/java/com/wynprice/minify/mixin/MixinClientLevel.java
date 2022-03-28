package com.wynprice.minify.mixin;

import com.wynprice.minify.management.MinifyChunkStorage;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLevel.class)
public class MixinClientLevel {

    @Redirect(method = "<init>()V", at = @At(value = "NEW", args = "class=net/minecraft/client/multiplayer/ClientChunkCache") )
    private static ClientChunkCache _load_with_cache_override(ClientLevel level, int distance) {
        return new MinifyChunkStorage(level, distance);
    }

}
