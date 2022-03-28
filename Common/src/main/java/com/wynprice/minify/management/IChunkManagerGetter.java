package com.wynprice.minify.management;

import net.minecraft.world.level.Level;

public interface IChunkManagerGetter {
    MinifyChunkManager getManager();

    static MinifyChunkManager getManager(Level level) {
        return ((IChunkManagerGetter) level).getManager();
    }
}
