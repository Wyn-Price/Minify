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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//containerData is from PalettedContainer<BlockState>#write
public record S2CSendViewerData(BlockPos blockPos, Optional<BlockPos> nestedPosition, byte[] containerData, Map<BlockPos, CompoundTag> blockEntities) {

    public S2CSendViewerData(BlockPos blockPos, Optional<BlockPos> nestedPosition, PalettedContainer<BlockState> container, Map<BlockPos, BlockEntity> blockEntities) {
        this(blockPos, nestedPosition, convertToBytes(container), blockEntities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().saveWithId())));
    }

    private static byte[] convertToBytes(PalettedContainer<BlockState> container) {
        //Convert PalledContainer to raw bytes
        ByteBuf buffer = Unpooled.buffer();
        var friendly = new FriendlyByteBuf(buffer);
        int start = buffer.writerIndex();
        container.write(friendly);
        int end = buffer.writerIndex();
        return ByteBufUtil.getBytes(buffer, start, end - start);
    }


    public static void encode(S2CSendViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeOptional(data.nestedPosition, FriendlyByteBuf::writeBlockPos);
        buf.writeByteArray(data.containerData);
        buf.writeMap(data.blockEntities, FriendlyByteBuf::writeBlockPos,FriendlyByteBuf::writeNbt);
    }

    public static S2CSendViewerData decode(FriendlyByteBuf buf) {
        return new S2CSendViewerData(
            buf.readBlockPos(), buf.readOptional(FriendlyByteBuf::readBlockPos), buf.readByteArray(),
            buf.readMap(FriendlyByteBuf::readBlockPos, FriendlyByteBuf::readAnySizeNbt)
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CSendViewerData packet) {
        Minecraft minecraft = client.get();
        BlockEntity entity = minecraft.level.getBlockEntity(packet.blockPos);
        if(entity instanceof MinifyViewerBlockEntity blockEntity) {
            MinifyViewerBlockEntity viewerBlock;
            if(packet.nestedPosition.isPresent()) {
                BlockEntity block = blockEntity.getBlockEntityMap().get(packet.nestedPosition.get());
                if(block instanceof MinifyViewerBlockEntity minifyViewerBlockEntity) {
                    viewerBlock = minifyViewerBlockEntity;
                }  else {
                    return;
                }
            } else {
                viewerBlock = blockEntity;
            }
            viewerBlock.getOrGenerateWorldCache().ifPresent(cache -> {
                ByteBuf buffer = Unpooled.copiedBuffer(packet.containerData);
                var friendly = new FriendlyByteBuf(buffer);
                cache.read(friendly);

                var entityList = viewerBlock.getBlockEntityMap();
                entityList.clear();
                packet.blockEntities.forEach((pos, tag) -> {
                    BlockState state = cache.get(pos.getX(), pos.getY(), pos.getZ());
                    BlockEntity loadStatic = BlockEntity.loadStatic(
                        pos, state, tag
                    );
                    if(loadStatic != null) {
                        entityList.put(pos, loadStatic);
                    } else {
                        entityList.remove(pos);
                    }
                });
            });

        }
    }
}
