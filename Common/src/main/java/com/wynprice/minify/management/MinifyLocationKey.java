package com.wynprice.minify.management;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public record MinifyLocationKey(ChunkPos chunk, int yChunk) {

    public static CompoundTag toNBT(MinifyLocationKey key, CompoundTag tag) {
        tag.putLong("chunk", key.chunk().toLong());
        tag.putInt("y", key.yChunk());
        return tag;
    }

    public static MinifyLocationKey fromNBT(CompoundTag tag) {
        return new MinifyLocationKey(
            new ChunkPos(tag.getLong("chunk")),
            tag.getInt("y")
        );
    }
}
