package com.wynprice.minify;

import com.wynprice.minify.blocks.MinifyBlocks;
import com.wynprice.minify.blocks.entity.MinifyBlockEntityTypes;
import com.wynprice.minify.client.MinifySourceBlockEntityRenderer;
import com.wynprice.minify.client.MinifyViewerBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

public class MinifyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), MinifyBlocks.MINIFY_VIEWER, MinifyBlocks.MINIFY_CHUNK_WALL);
        BlockEntityRendererRegistry.register(MinifyBlockEntityTypes.MINIFY_VIEWER_BLOCK_ENTITY, MinifyViewerBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(MinifyBlockEntityTypes.MINIFY_SOURCE_BLOCK_ENTITY, c -> new MinifySourceBlockEntityRenderer());

    }
}