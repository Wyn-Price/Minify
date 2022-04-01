package com.wynprice.minify.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EmptyChunkGenerator extends ChunkGenerator {

    public static final Codec<EmptyChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
        commonCodec(instance).and(
            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((d) -> d.biomes)
        )
            .apply(instance, instance.stable(EmptyChunkGenerator::new)));

    private final Registry<Biome> biomes;

    public EmptyChunkGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomes) {
        super(structureSets, Optional.empty(), new FixedBiomeSource(biomes.getOrCreateHolder(Biomes.THE_END)));
        this.biomes = biomes;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long var1) {
        return this;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Climate.empty();
    }

    @Override
    public void applyCarvers(WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7) {

    }

    @Override
    public void buildSurface(WorldGenRegion var1, StructureFeatureManager var2, ChunkAccess var3) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion var1) {

    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, Blender var2, StructureFeatureManager var3, ChunkAccess var4) {
        return CompletableFuture.completedFuture(var4);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> var1, BlockPos var2) {

    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel $$0, ChunkAccess $$1, StructureFeatureManager $$2) {

    }


}
