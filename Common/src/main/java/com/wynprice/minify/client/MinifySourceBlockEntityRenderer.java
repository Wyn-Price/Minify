package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;

public class MinifySourceBlockEntityRenderer implements BlockEntityRenderer<MinifySourceBlockEntity> {
    @Override
    public void render(MinifySourceBlockEntity blockEntity, float renderTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        BlockPos offset = blockEntity.getOffset();
        stack.pushPose();
        stack.translate(offset.getX(), offset.getY(), offset.getZ());

        LevelRenderer.renderLineBox(
            stack, buffer.getBuffer(RenderType.LINES),
            0, 0, 0,
            8, 8, 8,
            0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F
        );
        stack.popPose();

        renderNameOverBlock(stack, buffer, blockEntity.getName(), null);

    }

    public static void renderNameOverBlock(PoseStack stack, MultiBufferSource buffer, String draw, Quaternion rotation) {
        if(draw.isBlank()) {
            return;
        }
        stack.pushPose();
        stack.translate(0.5, 1.25, 0.5);
        if(rotation != null) {
            stack.mulPose(rotation);
        }
        stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        stack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = stack.last().pose();

        float fOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int iOpacity = (int)(fOpacity * 255.0F) << 24;
        Font font = Minecraft.getInstance().font;

        float x = (float)(-font.width(draw) / 2);
        font.drawInBatch(draw, x, 0, 0x20FFFFFF, false, matrix, buffer, true, iOpacity, LightTexture.FULL_BRIGHT);
        font.drawInBatch(draw, x, 0, -1, false, matrix, buffer, false, 0, LightTexture.FULL_BRIGHT);


        stack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
