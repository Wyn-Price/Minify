package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.management.IChunkManagerGetter;
import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class MinifyBlockEntity extends BlockEntity {

    private MinifyLocationKey locationKey;

    public MinifyBlockEntity(BlockPos pos, BlockState state) {
        super(MinifyBlockEntityTypes.MINIFICATION_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void clearRemoved() {
        if(this.locationKey == null) {
            this.locationKey = IChunkManagerGetter.getManager(this.level).findNextLocation();
        }
        IChunkManagerGetter.getManager(this.level).setLocation(this.locationKey, this);
        super.clearRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void setRemoved() {
        if(this.locationKey != null) {
            IChunkManagerGetter.getManager(this.level).onUnload(this.locationKey);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if(this.locationKey != null) {
            tag.putLong("location_chunk_key", this.locationKey.chunk().toLong());
            tag.putInt("location_y", this.locationKey.yChunk());
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if(tag.contains("location_chunk_key")) {
            this.locationKey = new MinifyLocationKey(
                new ChunkPos(tag.getLong("location_chunk_key")),
                tag.getInt("location_y")
            );
        }
        super.load(tag);
    }

    public final BlockPos getOffset() {
        return this.getBlockPos().above();
    }

    public final AABB getArea() {
        return new AABB(this.getOffset(), this.getOffset().offset(7, 7, 7));
    }
}
