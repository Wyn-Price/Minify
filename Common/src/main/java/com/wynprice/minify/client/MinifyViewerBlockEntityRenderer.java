package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

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

        MinifyViewerClientLevel viewer = MinifyViewerClientLevel.INSTANCE;
        if(MinifyViewerClientLevel.INSTANCE == blockEntity.getLevel()) {
            //Run when nested once
            blockEntity.requestNestedClientIfNeeded();
            viewer = MinifyViewerClientLevel.SECONDARY_INSTANCE;
        } else if(MinifyViewerClientLevel.SECONDARY_INSTANCE != blockEntity.getLevel()) {
            //Run when not nested
            blockEntity.requestOnClientIfNeeded();
        } else {
            //Run when nested twice
            return;
        }

        stack.pushPose();

        AmbientOcclusionStatus ambientOcclusion = Minecraft.getInstance().options.ambientOcclusion;
        Minecraft.getInstance().options.ambientOcclusion = AmbientOcclusionStatus.OFF;

        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(90 * blockEntity.getHorizontalRotationIndex()));
        stack.translate(-0.5, -0.5, -0.5);

        stack.scale(1/8f, 1/8F, 1/8F);
        MinifyViewerClientLevel finalViewer = viewer;
        viewer.injectAndRun(blockEntity, () -> {
            this.renderBlockAndFluid(finalViewer, stack, buffer);
            blockEntity.getBlockEntityMap().forEach((pos, be) -> {
                stack.pushPose();
                stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                this.blockEntityRenderDispatcher.render(be, renderTicks, stack, buffer);
                stack.popPose();
            });
        });

        Minecraft.getInstance().options.ambientOcclusion = ambientOcclusion;

        stack.popPose();
    }

    //A modified version of ChunkRenderDispatcher#RebuildTask#compile
    private void renderBlockAndFluid(MinifyViewerClientLevel level, PoseStack stack, MultiBufferSource source) {
        if (level != null) {
            ModelBlockRenderer.enableCaching();
            Random random = new Random();
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

            for(BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 7, 7, 7)) {
                BlockState state = level.getBlockState(pos);

//                if (state.hasBlockEntity()) {
//                    BlockEntity blockEntity = level.getBlockEntity(pos);
//                    if (blockEntity != null) {
//                        this.blockEntityRenderDispatcher.render(blockEntity, renderTicks, stack, source);
//                    }
//                }

                FluidState fluidState = state.getFluidState();
                if (!fluidState.isEmpty()) {
                    RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                    VertexConsumer buffer = source.getBuffer(renderType);
                    blockRenderDispatcher.renderLiquid(pos, level, buffer, state, fluidState);
                }

                if (state.getRenderShape() != RenderShape.INVISIBLE) {
                    RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
                    VertexConsumer buffer = source.getBuffer(renderType);

                    stack.pushPose();
                    stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                    blockRenderDispatcher.renderBatched(state, pos, level, stack, buffer, true, random);
                    stack.popPose();
                }
            }

            ModelBlockRenderer.clearCache();
        }
    }
}
