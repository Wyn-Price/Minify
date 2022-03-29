package com.wynprice.minify.mixin;

import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class MixinLevelChunk {

    @Redirect(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onRemove(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V")
    )
    private void onlyPlaceIfNotEditing(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean drops) {
        if(level.dimension() == DimensionRegistry.WORLD_KEY && MinifyChunkManager.isSilentlyPlacingIntoWorld.get()) {
            return;
        }
        state.onRemove(level, pos, oldState, drops);
    }
}
