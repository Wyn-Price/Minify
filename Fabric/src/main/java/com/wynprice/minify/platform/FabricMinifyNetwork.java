package com.wynprice.minify.platform;

import com.wynprice.minify.Constants;
import com.wynprice.minify.platform.services.MinifyNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricMinifyNetwork implements MinifyNetwork {

    private int packetIncrementor;

    private final Map<Class<?>, PacketData<?>> clazzToDataMap = new HashMap<>();


    @Override
    public void sendToServer(Object packet) {
        this.encodeAndSend(packet, ClientPlayNetworking::send);
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        this.encodeAndSend(packet, (location, buf) -> ServerPlayNetworking.send(player, location, buf));
    }

    @Override
    public void sendToAllInDimension(Object packet, ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            this.encodeAndSend(packet, (location, buf) -> ServerPlayNetworking.send(player, location, buf));
        }

    }

    @Override
    public void sendToAllAround(Object packet, ServerLevel level, BlockPos pos) {
        final LevelChunk chunk = level.getChunkAt(pos);
        ((ServerChunkCache)chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(player ->
            this.encodeAndSend(packet, (location, buf) -> ServerPlayNetworking.send(player, location, buf))
        );
    }

    private <P> void encodeAndSend(P packet, BiConsumer<ResourceLocation, FriendlyByteBuf> sender) {
        PacketData<P> packetData = (PacketData<P>) this.clazzToDataMap.get(packet.getClass());
        if(packetData == null) {
            Constants.LOG.warn("Unable to find packet data of class {}", packet.getClass().getSimpleName());
            return;
        }

        FriendlyByteBuf byteBuf = PacketByteBufs.create();
        packetData.encoder().accept(packet, byteBuf);
        sender.accept(packetData.location(), byteBuf);
    }

    @Override
    public <P> void registerClientBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, S2CHandler<P> handler) {
        ResourceLocation location = this.getNextResourceLocation();
        this.clazzToDataMap.put(clazz, new PacketData<>(location, encoder));
        ClientPlayNetworking.registerGlobalReceiver(location, (client, packetListener, buf, responseSender) -> {
            P packet = decoder.apply(buf);
            client.execute(() -> handler.handle(() -> client, packet));
        });
    }

    @Override
    public <P> void registerServerBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, C2SHandler<P> handler) {
        ResourceLocation location = this.getNextResourceLocation();
        this.clazzToDataMap.put(clazz, new PacketData<>(location, encoder));
        ServerPlayNetworking.registerGlobalReceiver(location, (server, player, packetListener, buf, responseSender) -> {
            P packet = decoder.apply(buf);
            server.execute(() -> handler.handle(() -> server, player, packet));
        });
    }

    private ResourceLocation getNextResourceLocation() {
        return new ResourceLocation(Constants.MOD_ID, "packet_" + (this.packetIncrementor++));
    }

    private record PacketData<P>(ResourceLocation location, BiConsumer<P, FriendlyByteBuf> encoder) {
    }
}
