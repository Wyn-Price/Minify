package com.wynprice.minify.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynprice.minify.blocks.entity.MinifySourceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;

public class MinifySourceBlockEntityRenderer implements BlockEntityRenderer<MinifySourceBlockEntity> {
    @Override
    public void render(MinifySourceBlockEntity blockEntity, float renderTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        BlockPos offset = blockEntity.getOffset();
        stack.translate(offset.getX(), offset.getY(), offset.getZ());

        LevelRenderer.renderLineBox(
            stack, buffer.getBuffer(RenderType.LINES),
            0, 0, 0,
            8, 8, 8,
            0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F
        );
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}
