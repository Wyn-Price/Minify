package com.wynprice.minify.platform;

import com.mojang.datafixers.types.Type;
import com.wynprice.minify.mixin.MixinClientChunkCacheStorage;
import com.wynprice.minify.mixin.MixinClientChunkCache;
import com.wynprice.minify.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T extends BlockEntity> Function<Type<?>, BlockEntityType<T>> createBlockEntity(BiFunction<BlockPos, BlockState, T> function, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(function::apply, blocks)::build;
    }

    @Override
    public MixinClientChunkCacheStorage getAccessor(Object o) {
        ClientChunkCache.Storage storage = ((MixinClientChunkCache) o).accessor_chunks();
        return (MixinClientChunkCacheStorage) (Object) storage;
    }
}
