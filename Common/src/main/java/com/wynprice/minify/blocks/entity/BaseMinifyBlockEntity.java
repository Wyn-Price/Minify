package com.wynprice.minify.blocks.entity;

import com.wynprice.minify.management.MinifyChunkManager;
import com.wynprice.minify.management.MinifyLocationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

public class BaseMinifyBlockEntity extends BlockEntity {

    protected MinifyLocationKey locationKey;

    private final TriConsumer<MinifyChunkManager, MinifyLocationKey, BlockPos> loadFunction;
    private final BiConsumer<MinifyChunkManager, MinifyLocationKey> unloadFunction;

    public BaseMinifyBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state, TriConsumer<MinifyChunkManager, MinifyLocationKey, BlockPos> loadFunction, BiConsumer<MinifyChunkManager, MinifyLocationKey> unloadFunction) {
        super(entityType, pos, state);
        this.loadFunction = loadFunction;
        this.unloadFunction = unloadFunction;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void setToData() {
        if(this.level instanceof ServerLevel) {
            if(this.locationKey == null) {
                this.locationKey = MinifyChunkManager.getManager((ServerLevel) this.level).findNextLocation();
            }
            this.loadFunction.accept(MinifyChunkManager.getManager((ServerLevel) this.level), this.locationKey, this.getBlockPos());

        }
    }

    public void removeFromData() {
        if(this.level instanceof ServerLevel && this.locationKey != null) {
            this.unloadFunction.accept(MinifyChunkManager.getManager((ServerLevel) this.level), this.locationKey);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if(this.locationKey != null) {
            tag.put("location_key", MinifyLocationKey.toNBT(this.locationKey, new CompoundTag()));
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        //OLD: REMOVE WHEN DONE WITH
        if(tag.contains("location_chunk_key")) {
            this.locationKey = new MinifyLocationKey(
                new ChunkPos(tag.getLong("location_chunk_key")),
                tag.getInt("location_y")
            );
        }
        //-------------------------

        if(tag.contains("location_key", 10)) {
            this.locationKey = MinifyLocationKey.fromNBT(tag.getCompound("location_key"));
        }
        super.load(tag);
    }

    public MinifyLocationKey getLocationKey() {
        return locationKey;
    }

}
