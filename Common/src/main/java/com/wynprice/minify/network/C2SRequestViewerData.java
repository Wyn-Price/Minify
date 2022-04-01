package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import java.util.function.Supplier;

public record C2SRequestViewerData(BlockPos blockPos) {

    public static void encode(C2SRequestViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
    }

    public static C2SRequestViewerData decode(FriendlyByteBuf buf) {
        return new C2SRequestViewerData(buf.readBlockPos());
    }

    public static void handle(Supplier<MinecraftServer> server, ServerPlayer player, C2SRequestViewerData packet) {
        BlockEntity rawEntity = player.level.getBlockEntity(packet.blockPos);
        if (rawEntity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache ->
                Services.NETWORK.sendToPlayer(new S2CSendViewerData(packet.blockPos, Optional.empty(), cache, blockEntity.getBlockEntityMap()), player)
            );
        }

    }
}
