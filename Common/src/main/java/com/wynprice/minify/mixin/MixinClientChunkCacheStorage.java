package com.wynprice.minify.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(targets="net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public interface MixinClientChunkCacheStorage {

    @Accessor("chunks")
    AtomicReferenceArray<LevelChunk> accessor_chunks();
}
