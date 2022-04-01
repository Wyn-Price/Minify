package com.wynprice.minify.client;

import com.wynprice.minify.blocks.entity.MinifyViewerBlockEntity;
import com.wynprice.minify.mixin.BiomeManagerAccessor;
import com.wynprice.minify.platform.Services;
import com.wynprice.minify.util.LevelChunkSectionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MinifyViewerClientLevel extends ClientLevel {

    public static MinifyViewerClientLevel INSTANCE;
    public static MinifyViewerClientLevel SECONDARY_INSTANCE;

    public static Optional<MinifyViewerClientLevel> getIfPossible() {
        if(INSTANCE.blockEntity == null) {
            return Optional.of(INSTANCE);
        }
        if(SECONDARY_INSTANCE.blockEntity == null) {
            return Optional.of(SECONDARY_INSTANCE);
        }
        return Optional.empty();
    }

    private MinifyViewerBlockEntity blockEntity;

    public MinifyViewerClientLevel(ClientLevel delegate) {
        super(null, delegate.getLevelData(), delegate.dimension(), delegate.dimensionTypeRegistration(), 2, 2, delegate.getProfilerSupplier(), null, false, ((BiomeManagerAccessor) delegate.getBiomeManager()).accessor_getBiomeZoomSeed());


        ClientChunkCache source = this.getChunkSource();
        AtomicReferenceArray<LevelChunk> chunks = Services.PLATFORM.getAccessor(source).accessor_chunks();
        chunks.set(0, new LevelChunk(this, new ChunkPos(0, 0)));
    }

    public void injectAndRun(MinifyViewerBlockEntity blockEntity, Runnable runner) {
        this.blockEntity = blockEntity;
        LevelChunk zeroChunk = this.getChunk(0, 0);
        LevelChunkSection section = zeroChunk.getSection(this.getSectionIndex(0));
        LevelChunkSectionAccessor accessor = (LevelChunkSectionAccessor) section;

        //Make it so the chunk isn't empty, and is ticking.
        //Note in the future, we might just want to write/setstate
        accessor.accessor_setNonEmptyBlockCount((short) (Short.MAX_VALUE / 2));
        accessor.accessor_setTickingBlockCount((short) (Short.MAX_VALUE / 2));
        accessor.accessor_setTickingFluidCount((short) (Short.MAX_VALUE / 2));

        PalettedContainer<BlockState> states = section.getStates();
        accessor.accessor_setStates(blockEntity.getOrGenerateWorldCache().orElseThrow());
        blockEntity.getBlockEntityMap().values().forEach(this::setBlockEntity);

        runner.run();

        accessor.accessor_setStates(states);

        blockEntity.getBlockEntityMap().clear();
        blockEntity.getBlockEntityMap().putAll(zeroChunk.getBlockEntities());
        blockEntity.getBlockEntityMap().keySet().forEach(this::removeBlockEntity);

        accessor.accessor_setNonEmptyBlockCount((short) 0);
        accessor.accessor_setTickingBlockCount((short) 0);
        accessor.accessor_setTickingFluidCount((short) 0);
        this.blockEntity = null;
    }

    public MinifyViewerBlockEntity getCurrentBlockEntity() {
        return blockEntity;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        ClientLevel delegate = Minecraft.getInstance().level;
        if(this.blockEntity != null) {
            return delegate.getBiome(this.blockEntity.getBlockPos());
        }
        return delegate.getBiome(pos);
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return Minecraft.getInstance().level.getBrightness(layer, this.blockEntity.getBlockPos());
    }

    @Override
    public float getTimeOfDay(float time) {
        return Minecraft.getInstance().level.getTimeOfDay(time);
    }

    @Override
    public long getDayTime() {
        return Minecraft.getInstance().level.getDayTime();
    }

    @Override
    public long getGameTime() {
        return Minecraft.getInstance().level.getDayTime();
    }

    // OVERRIDE CALLS TO #connection

    @Override
    public void disconnect() {
        //NO-OP
    }

    @Override
    public void sendPacketToServer(Packet<?> $$0) {
        //NO-OP
    }

    @Override
    public RecipeManager getRecipeManager() {
        return Minecraft.getInstance().level.getRecipeManager();
    }

    @Override
    public RegistryAccess registryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }





    // OVERRIDE CALLS TO #levelRenderer

    @Override
    public void sendBlockUpdated(BlockPos $$0, BlockState $$1, BlockState $$2, int $$3) {
        //NO_OP
    }

    @Override
    public void setBlocksDirty(BlockPos $$0, BlockState $$1, BlockState $$2) {
        //NO_OP
    }

    @Override
    public void setSectionDirtyWithNeighbors(int $$0, int $$1, int $$2) {
        //NO_OP
    }

    @Override
    public void destroyBlockProgress(int $$0, BlockPos $$1, int $$2) {
        //NO_OP
    }

    @Override
    public void globalLevelEvent(int $$0, BlockPos $$1, int $$2) {
        //NO_OP
    }

    @Override
    public void levelEvent(int $$0, BlockPos $$1, int $$2) {
        //NO_OP
    }

    @Override
    public void addParticle(ParticleOptions $$0, double $$1, double $$2, double $$3, double $$4, double $$5, double $$6) {
        //NO_OP
    }

    @Override
    public void addParticle(ParticleOptions $$0, boolean $$1, double $$2, double $$3, double $$4, double $$5, double $$6, double $$7) {
        //NO_OP
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions $$0, double $$1, double $$2, double $$3, double $$4, double $$5, double $$6) {
        //NO_OP
    }

    @Override
    public void addAlwaysVisibleParticle(ParticleOptions $$0, boolean $$1, double $$2, double $$3, double $$4, double $$5, double $$6, double $$7) {
        //NO_OP
    }


}
