package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class S2CUpdateViewerData {
    private final BlockPos blockPos;
    private final BlockPos innerPos;
    private final BlockState state;

    public S2CUpdateViewerData(BlockPos blockPos, BlockPos innerPos, BlockState state) {
        this.blockPos = blockPos;
        this.innerPos = innerPos;
        this.state = state;
    }


    public static void encode(S2CUpdateViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeBlockPos(data.innerPos);
        buf.writeVarInt(Block.getId(data.state));

    }

    public static S2CUpdateViewerData decode(FriendlyByteBuf buf) {
        return new S2CUpdateViewerData(
            buf.readBlockPos(), buf.readBlockPos(),
            Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt())
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CUpdateViewerData packet) {
        Minecraft minecraft = client.get();
        BlockEntity entity = minecraft.level.getBlockEntity(packet.blockPos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache ->
                cache.set(packet.innerPos.getX(), packet.innerPos.getY(), packet.innerPos.getZ(), packet.state)
            );
        }
    }
}
