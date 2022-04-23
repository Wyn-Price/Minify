package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.client.MinifyViewerClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public record S2CMinifiyBlockEvent(BlockPos viewerPos, BlockPos innerPos, int dataA, int dataB) {

    public static void encode(S2CMinifiyBlockEvent data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.viewerPos);
        buf.writeBlockPos(data.innerPos);
        buf.writeByte(data.dataA);
        buf.writeByte(data.dataB);
    }

    public static S2CMinifiyBlockEvent decode(FriendlyByteBuf buf) {
        return new S2CMinifiyBlockEvent(
            buf.readBlockPos(), buf.readBlockPos(),
            buf.readUnsignedByte(), buf.readUnsignedByte()
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CMinifiyBlockEvent packet) {
        BlockEntity entityRaw = client.get().level.getBlockEntity(packet.viewerPos());
        if(entityRaw instanceof MinifyViewerBlockEntity blockEntity && MinifyViewerClientLevel.INSTANCE.getMainViewer() == null) {
            MinifyViewerClientLevel.INSTANCE.injectAndRun(blockEntity, nested -> {
                BlockState blockState = MinifyViewerClientLevel.INSTANCE.getBlockState(packet.innerPos());
                blockState.triggerEvent(MinifyViewerClientLevel.INSTANCE, packet.innerPos(), packet.dataA, packet.dataB);
            });
        }
    }
}
