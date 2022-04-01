package com.wynprice.minify.platform;

import com.mojang.datafixers.types.Type;
import com.wynprice.minify.mixin.MixinClientChunkCacheStorage;
import com.wynprice.minify.mixin.MixinClientChunkCache;
import com.wynprice.minify.platform.services.IPlatformHelper;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public <T extends BlockEntity> Function<Type<?>, BlockEntityType<T>> createBlockEntity(BiFunction<BlockPos, BlockState, T> function, Block... blocks) {
        return BlockEntityType.Builder.of(function::apply, blocks)::build;
    }

    @Override
    public MixinClientChunkCacheStorage getAccessor(Object o) {
        ClientChunkCache.Storage storage = ((MixinClientChunkCache) o).accessor_chunks();
        return (MixinClientChunkCacheStorage) (Object) storage;
    }
}
