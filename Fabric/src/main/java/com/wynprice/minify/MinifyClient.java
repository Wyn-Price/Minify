package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class MinifyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), MinifyBlocks.MINIFY_VIEWER, MinifyBlocks.MINIFY_CHUNK_WALL);
    }
}