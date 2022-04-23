package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3f;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

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
        if(!viewer.hasRoom()) {
            return;
        }

        if(viewer.getMainViewer() == null) {
            blockEntity.requestOnClientIfNeeded();
        } else {
            blockEntity.requestNestedClientIfNeeded();
        }

        stack.pushPose();

        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(Vector3f.YP.rotationDegrees(90 * blockEntity.getHorizontalRotationIndex()));
        stack.translate(-0.5, -0.5, -0.5);

        stack.scale(1/8f, 1/8F, 1/8F);
        viewer.injectAndRun(blockEntity, nested -> {
            this.renderBlockAndFluid(viewer, stack, buffer, nested ? 16 : 0);
            blockEntity.getBlockEntityMap().forEach((pos, be) -> {
                stack.pushPose();
                stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                this.blockEntityRenderDispatcher.render(be, renderTicks, stack, buffer);
                stack.popPose();
            });
        });


        stack.popPose();
    }

    //A modified version of ChunkRenderDispatcher#RebuildTask#compile
    private void renderBlockAndFluid(MinifyViewerClientLevel level, PoseStack stack, MultiBufferSource source, int offsetZ) {
        if (level != null) {
            ModelBlockRenderer.enableCaching();
            Random random = new Random();
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

            for(BlockPos pos : BlockPos.betweenClosed(0, 0, offsetZ, 7, 7, 7 + offsetZ)) {
                BlockState state = level.getBlockState(pos);

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

                    //Essentially, call blockRenderDispatcher.renderBatched but without ao
//                    blockRenderDispatcher.renderBatched(state, pos, level, stack, buffer, true, random);
                    try {
                        RenderShape shape = state.getRenderShape();
                        if(shape == RenderShape.MODEL) {
                            Vec3 offset = state.getOffset(level, pos);
                            stack.translate(offset.x, offset.y, offset.z);
                            this.blockRenderDispatcher.getModelRenderer().tesselateWithoutAO(level, this.blockRenderDispatcher.getBlockModel(state), state, pos, stack, buffer, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
                        }
                    } catch (Throwable var11) {
                        CrashReport $$9 = CrashReport.forThrowable(var11, "Tesselating block model");
                        CrashReportCategory $$10 = $$9.addCategory("Block being tesselated");
                        CrashReportCategory.populateBlockDetails($$10, level, pos, state);
                        throw new ReportedException($$9);
                    }
                    stack.popPose();
                }
            }

            ModelBlockRenderer.clearCache();
        }
    }
}
