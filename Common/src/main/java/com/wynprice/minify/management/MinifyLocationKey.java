package com.wynprice.minify.management;

import net.minecraft.world.level.ChunkPos;

public record MinifyLocationKey(ChunkPos chunk, int yChunk) {
}
