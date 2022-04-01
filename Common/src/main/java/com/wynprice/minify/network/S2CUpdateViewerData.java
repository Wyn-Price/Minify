package com.wynprice.minify.network;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public record S2CUpdateViewerData(BlockPos blockPos, List<UpdateData> updateData) {

    public static void encode(S2CUpdateViewerData data, FriendlyByteBuf buf) {
        buf.writeBlockPos(data.blockPos);
        buf.writeCollection(data.updateData, (buf1, updateData) -> {
            buf1.writeBlockPos(updateData.innerPos);
            buf1.writeVarInt(Block.getId(updateData.state));
        });
    }

    public static S2CUpdateViewerData decode(FriendlyByteBuf buf) {
        return new S2CUpdateViewerData(
            buf.readBlockPos(),
            buf.readList(friendlyByteBuf -> new UpdateData(
                buf.readBlockPos(),
                Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt())
            ))
        );
    }

    public static void handle(Supplier<Minecraft> client, S2CUpdateViewerData packet) {
        Minecraft minecraft = client.get();
        BlockEntity entity = minecraft.level.getBlockEntity(packet.blockPos);
        if (entity instanceof MinifyViewerBlockEntity blockEntity) {
            blockEntity.getOrGenerateWorldCache().ifPresent(cache -> packet.updateData.forEach(updateData -> {
                cache.set(updateData.innerPos.getX(), updateData.innerPos.getY(), updateData.innerPos.getZ(), updateData.state);

//                var entityList = blockEntity.getBlockEntityMap();
//                entityList.clear();
//                BlockState state = cache.get(updateData.innerPos.getX(), updateData.innerPos.getY(), updateData.innerPos.getZ());
//                BlockEntity loadStatic = updateData.blockEntity == null ? null : BlockEntity.loadStatic(
//                    BlockEntity.getPosFromTag(updateData.blockEntity), state, updateData.blockEntity
//                );
//                if (loadStatic != null) {
//                    entityList.put(updateData.innerPos, loadStatic);
//                } else {
//                    entityList.remove(updateData.innerPos);
//                }
            }));
        }
    }

    public static record UpdateData(BlockPos innerPos, BlockState state) { }
}
