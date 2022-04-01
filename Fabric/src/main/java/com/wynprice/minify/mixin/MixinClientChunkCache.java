package com.wynprice.minify.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//For some reason Access Transformers / Access Wideners don't mix together in joined.
//Therefore, this mixin has to appear twice
@Mixin(ClientChunkCache.class)
public interface MixinClientChunkCache {

    @Accessor("storage")
    ClientChunkCache.Storage accessor_chunks();
}
