package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.AmbientOcclusionStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
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

        int rotation = blockEntity.getHorizontalRotationIndex();
        int previousRotation = blockEntity.getPreviousHorizontalRotationIndex();

        Matrix4f pose = stack.last().pose();
        if(rotation != previousRotation) {
            //We can lerp between the previous, and the previous + 1
            float change = (blockEntity.ticksToRotate + renderTicks - 1) / MinifyViewerBlockEntity.TICKS_TO_ROTATE;
            pose.multiply(Vector3f.YP.rotationDegrees(90 * (previousRotation + change)));
        } else {
            pose.multiply(Vector3f.YP.rotationDegrees(90 * rotation));
        }

        stack.translate(-0.5, -0.5, -0.5);

        //Render the glass block
        this.blockRenderDispatcher.renderBatched(
            Blocks.GLASS.defaultBlockState(), blockEntity.getBlockPos(), Minecraft.getInstance().level, stack,
            buffer.getBuffer(RenderType.cutout()), true, new Random()
        );

        stack.scale(1/8f, 1/8F, 1/8F);
        viewer.injectAndRun(blockEntity, nested -> {
            this.renderBlockAndFluid(viewer, stack, buffer, nested ? 16 : 0);
            blockEntity.getBlockEntityMap().forEach((pos, be) -> {
                stack.pushPose();
                stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                this.renderBlockEntity(be, renderTicks, stack, buffer);
                stack.popPose();
            });
        });


        stack.popPose();
    }

    private <E extends BlockEntity> void renderBlockEntity(E blockEntity, float ticks, PoseStack stack, MultiBufferSource source) {
        BlockEntityRenderer<E> render = this.blockEntityRenderDispatcher.getRenderer(blockEntity);
        if (render != null) {
            if (blockEntity.hasLevel() && blockEntity.getType().isValid(blockEntity.getBlockState())) {
                try {
                    Level level = blockEntity.getLevel();
                    int light = level == null ? 15728880 : LevelRenderer.getLightColor(level, blockEntity.getBlockPos()) ;
                    render.render(blockEntity, ticks, stack, source, light, OverlayTexture.NO_OVERLAY);
                } catch (Throwable var5) {
                    CrashReport report = CrashReport.forThrowable(var5, "Rendering Nested Block Entity");
                    CrashReportCategory category = report.addCategory("Block Entity Details");
                    blockEntity.fillCrashReportCategory(category);
                    throw new ReportedException(report);
                }
            }
        }
    }

    //A modified version of ChunkRenderDispatcher#RebuildTask#compile
    private void renderBlockAndFluid(MinifyViewerClientLevel level, PoseStack stack, MultiBufferSource source, int offsetX) {
        if (level != null) {
            ModelBlockRenderer.enableCaching();
            Random random = new Random();
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

            for(BlockPos pos : BlockPos.betweenClosed(offsetX, 0, 0, 7 + offsetX, 7, 7)) {
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
