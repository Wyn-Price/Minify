package com.wynprice.minify.platform;

import com.wynprice.minify.Constants;
import com.wynprice.minify.platform.services.MinifyNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ForgeMinifyNetwork implements MinifyNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Constants.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private int incrementId = 0;

    @Override
    public void sendToServer(Object packet) {
        NETWORK.sendToServer(packet);
    }

    @Override
    public void sendToPlayer(Object packet, ServerPlayer player) {
        NETWORK.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    @Override
    public void sendToAllInDimension(Object packet, ServerLevel level) {
        NETWORK.send(PacketDistributor.DIMENSION.with(level::dimension), packet);

    }

    @Override
    public void sendToAllAround(Object packet, ServerLevel level, BlockPos pos) {
        NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    @Override
    public <P> void registerClientBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, S2CHandler<P> handler) {
        NETWORK.registerMessage(this.getNextId(), clazz, encoder, decoder, (packet, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> handler.handle(Minecraft::getInstance, packet));
            context.setPacketHandled(true);
        });
    }

    @Override
    public <P> void registerServerBound(Class<P> clazz, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder, C2SHandler<P> handler) {
        NETWORK.registerMessage(this.getNextId(), clazz, encoder, decoder, (packet, supplier) -> {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                handler.handle(() -> sender.server, sender, packet);
            });
            context.setPacketHandled(true);
        });
    }



    private int getNextId() {
        return this.incrementId++;
    }
}
