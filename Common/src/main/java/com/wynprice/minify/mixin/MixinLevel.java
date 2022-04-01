package com.wynprice.minify.mixin;

import com.wynprice.minify.generation.DimensionRegistry;
import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class MixinLevel {

    @Inject(
        method = "getDayTime()J",
        at = @At("RETURN"),
        cancellable = true
    )
    private void getDayTime(CallbackInfoReturnable<Long> infoReturnable) {
        var thiz = (Level) (Object) this;
        if(thiz instanceof ServerLevel && thiz.dimension() == DimensionRegistry.WORLD_KEY) {
            infoReturnable.setReturnValue(thiz.getServer().getLevel(Level.OVERWORLD).getDayTime());
        }
    }

    @Inject(
        method = "getGameTime()J",
        at = @At("RETURN"),
        cancellable = true
    )
    private void getGameTime(CallbackInfoReturnable<Long> infoReturnable) {
        var thiz = (Level) (Object) this;
        if(thiz instanceof ServerLevel && thiz.dimension() == DimensionRegistry.WORLD_KEY) {
            infoReturnable.setReturnValue(thiz.getServer().getLevel(Level.OVERWORLD).getGameTime());
        }
    }

}
