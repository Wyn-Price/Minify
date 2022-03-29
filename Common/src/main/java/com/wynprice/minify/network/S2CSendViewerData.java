package com.wynprice.minify.network;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.function.Supplier;

public class S2CSendViewerData {
    private final BlockPos blockPos;
    private final byte[] containerData; //PalettedContainer<BlockState>#write

    public S2CSendViewerData(BlockPos blockPos, PalettedContainer<BlockState> container) {
        this.blockPos = blockPos;

        //Convert PalledContainer to raw bytes
        ByteBuf buffer = Unpooled.buffer();
        var friendly = new FriendlyByteBuf(buffer);
        int start = buffer.writerIndex();
        container.write(friendly);
        int end = buffer.writerIndex();
        this.containerData = ByteBufUtil.getBytes(buffer, start, end - start);
    }

    public S2CSendViewerData(BlockPos pos, byte[] containerData) {
        this.blockPos = pos;
        this.containerData = containerData;
    }


    public static void encode(S2CSendViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeByteArray(data.containerData);
    }

    public static S2CSendViewerData decode(FriendlyByteBuf buf) {
        return new S2CSendViewerData(buf.readBlockPos(), buf.readByteArray());
    }

    public static void handle(Supplier<Minecraft> client, S2CSendViewerData packet) {
        Minecraft minecraft = client.get();
        BlockEntity entity = minecraft.level.getBlockEntity(packet.blockPos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache -> {
                ByteBuf buffer = Unpooled.copiedBuffer(packet.containerData);
                var friendly = new FriendlyByteBuf(buffer);
                cache.read(friendly);
            });
        }
    }
}
