package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class MinifyViewerBlockEntityRenderer implements BlockEntityRenderer<MinifyViewerBlockEntity> {

    private final BlockRenderDispatcher blockRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    public MinifyViewerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
        this.blockEntityRenderDispatcher = context.getBlockEntityRenderDispatcher();
    }
    @Override
    public void render(MinifyViewerBlockEntity blockEntity, float renderTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        stack.pushPose();

        blockEntity.requestOnClientIfNeeded();

//        RenderType renderType = ItemBlockRenderTypes.getRenderType(blockState);
//        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
//        this.blockRenderer.getModelRenderer().tesselateBlock(level, this.blockRenderer.getBlockModel(blockState), blockState, blockPos, poseStack, vertexConsumer, bl, new Random(), blockState.getSeed(blockPos), i);

        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(90 * blockEntity.getHorizontalRotationIndex()));
        stack.translate(-0.5, -0.5, -0.5);

        stack.scale(1/8f, 1/8F, 1/8F);
        blockEntity.getOrGenerateWorldCache().ifPresent(cache -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
                        stack.pushPose();
                        stack.translate(x, y, z);
                        BlockState state = cache.get(x, y, z);
                        this.blockRenderDispatcher.renderSingleBlock(state, stack, buffer, light, overlay);
                        stack.popPose();
                    }
                }
            }
        });

        stack.popPose();
    }
}
