package com.wynprice.minify.management;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.Consumer;

public class MinifyChunkStorage extends ClientChunkCache {

    public MinifyChunkStorage(ClientLevel level, int distance) {
        super(level, distance);
    }

    @Override
    public LevelChunk getChunk(int x, int y, ChunkStatus status, boolean needsExistence) {
        return super.getChunk(x, y, status, needsExistence);
    }


    @Override
    public void onLightUpdate(LightLayer $$0, SectionPos $$1) {
        super.onLightUpdate($$0, $$1);
    }

    @Override
    public LevelChunk replaceWithPacketData(int $$0, int $$1, FriendlyByteBuf $$2, CompoundTag $$3, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> $$4) {
        return super.replaceWithPacketData($$0, $$1, $$2, $$3, $$4);
    }
}
