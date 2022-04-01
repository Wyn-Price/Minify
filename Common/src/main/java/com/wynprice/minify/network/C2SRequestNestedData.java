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

public record C2SRequestNestedData(BlockPos blockPos, BlockPos innerPos) {

    public static void encode(C2SRequestNestedData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeBlockPos(data.innerPos);
    }

    public static C2SRequestNestedData decode(FriendlyByteBuf buf) {
        return new C2SRequestNestedData(buf.readBlockPos(), buf.readBlockPos());
    }

    public static void handle(Supplier<MinecraftServer> server, ServerPlayer player, C2SRequestNestedData packet) {
        BlockEntity rawEntity = player.level.getBlockEntity(packet.blockPos);
        if (rawEntity instanceof MinifyViewerBlockEntity blockEntity) {
            BlockEntity rawInnerEntity = blockEntity.getBlockEntityMap().get(packet.innerPos);
            if(rawInnerEntity instanceof MinifyViewerBlockEntity innerEntity) {
                innerEntity.getOrGenerateWorldCache().ifPresent(cache ->
                    Services.NETWORK.sendToPlayer(new S2CSendViewerData(packet.blockPos, Optional.of(packet.innerPos), cache, innerEntity.getBlockEntityMap()), player)
                );
            }
        }

    }
}
