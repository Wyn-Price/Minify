package com.wynprice.minify.platform.services;

import com.mojang.datafixers.types.Type;
import com.wynprice.minify.mixin.MixinClientChunkCacheStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();



    <T extends BlockEntity> Function<Type<?>, BlockEntityType<T>> createBlockEntity(BiFunction<BlockPos, BlockState, T> function, Block... blocks);

    //Object: ClientChunkCache
    MixinClientChunkCacheStorage getAccessor(Object o);
}
