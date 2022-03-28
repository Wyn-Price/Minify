package com.wynprice.minify.mixin;

import com.wynprice.minify.management.IChunkManagerGetter;
import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class MixinLevel implements IChunkManagerGetter {
    private final MinifyChunkManager manager = new MinifyChunkManager();

    @Override
    public MinifyChunkManager getManager() {
        return this.manager;
    }

    @Inject(
        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
        at = @At("HEAD")
    )
    public void setBlock(BlockPos pos, BlockState state, int flags, int recursions, CallbackInfoReturnable<Boolean> info) {
        this.manager.onBlockChangedAt((Level) (Object) this, pos, state);
    }
}
