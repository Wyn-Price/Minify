package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public record S2CUpdateViewerBlockEntityData(BlockPos blockPos, BlockPos innerPos, CompoundTag tag) {

    public static void encode(S2CUpdateViewerBlockEntityData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeBlockPos(data.innerPos);
        buf.writeNbt(data.tag);
    }

    public static S2CUpdateViewerBlockEntityData decode(FriendlyByteBuf buf) {
        return new S2CUpdateViewerBlockEntityData(
            buf.readBlockPos(),
            buf.readBlockPos(),
            buf.readAnySizeNbt()
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CUpdateViewerBlockEntityData packet) {
        Minecraft minecraft = client.get();
        BlockEntity viewerRaw = minecraft.level.getBlockEntity(packet.blockPos);
        if (viewerRaw instanceof MinifyViewerBlockEntity viewer) {
            viewer.getOrGenerateWorldCache().ifPresent(cache -> {
                var entityList = viewer.getBlockEntityMap();
                BlockEntity blockEntity = entityList.get(packet.innerPos);
                blockEntity.load(packet.tag);
            });
        }
    }

    public static record UpdateData(BlockPos innerPos, BlockState state) { }
}
