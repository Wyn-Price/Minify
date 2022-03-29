package com.wynprice.minify.platform.services;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MinifyNetwork {

    //C2S
    void sendToServer(Object packet);

    //S2C
    void sendToPlayer(Object packet, ServerPlayer player);
    void sendToAllInDimension(Object packet, ServerLevel level);
    void sendToAllAround(Object packet, ServerLevel level, BlockPos pos);

    <P> void registerClientBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, S2CHandler<P> handler);

    <P> void registerServerBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, C2SHandler<P> handler);

    interface C2SHandler<P> {
        void handle(Supplier<MinecraftServer> server, ServerPlayer player, P packet);
    }

    interface S2CHandler<P> {
        void handle(Supplier<Minecraft> client, P packet);
    }
}
