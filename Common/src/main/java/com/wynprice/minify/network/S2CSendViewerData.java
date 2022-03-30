package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class S2CSendViewerData {
    private final BlockPos blockPos;
    private final byte[] containerData; //PalettedContainer<BlockState>#write
    private final Map<BlockPos, CompoundTag> blockEntities;

    public S2CSendViewerData(BlockPos blockPos, PalettedContainer<BlockState> container, Map<BlockPos, BlockEntity> blockEntities) {
        this.blockPos = blockPos;

        //Convert PalledContainer to raw bytes
        ByteBuf buffer = Unpooled.buffer();
        var friendly = new FriendlyByteBuf(buffer);
        int start = buffer.writerIndex();
        container.write(friendly);
        int end = buffer.writerIndex();
        this.containerData = ByteBufUtil.getBytes(buffer, start, end - start);

        this.blockEntities = blockEntities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().saveWithFullMetadata()));
    }

    public S2CSendViewerData(BlockPos pos, byte[] containerData, Map<BlockPos, CompoundTag> blockEntities) {
        this.blockPos = pos;
        this.containerData = containerData;
        this.blockEntities = blockEntities;
    }


    public static void encode(S2CSendViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeByteArray(data.containerData);
        buf.writeMap(data.blockEntities, FriendlyByteBuf::writeBlockPos,FriendlyByteBuf::writeNbt);
    }

    public static S2CSendViewerData decode(FriendlyByteBuf buf) {
        return new S2CSendViewerData(
            buf.readBlockPos(), buf.readByteArray(),
            buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readAnySizeNbt)
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CSendViewerData packet) {
        Minecraft minecraft = client.get();
        BlockEntity entity = minecraft.level.getBlockEntity(packet.blockPos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache -> {
                ByteBuf buffer = Unpooled.copiedBuffer(packet.containerData);
                var friendly = new FriendlyByteBuf(buffer);
                cache.read(friendly);

                var entityList = blockEntity.getBlockEntityMap();
                entityList.clear();
                packet.blockEntities.forEach((pos, tag) -> {
                    BlockState state = cache.get(pos.getX(), pos.getY(), pos.getZ());
                    BlockEntity loadStatic = BlockEntity.loadStatic(
                        BlockEntity.getPosFromTag(tag), state, tag
                    );
                    entityList.put(pos, loadStatic);
                });
            });

        }
    }
}
