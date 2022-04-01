package com.wynprice.minify.mixin;

import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sql.rowset.BaseRowSet;
import java.util.Optional;

@Mixin(BlockAndTintGetter.class)
public interface MixinBlockAndTintGetter {


    @Shadow
    LevelLightEngine getLightEngine();

//    @Inject(
//        method = "getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I",
//        at = @At("RETURN"),
//        cancellable = true
//    )
    @Overwrite
    default int getBrightness(LightLayer layer, BlockPos pos) {
        if((Object) this instanceof ServerLevel sl) {
            Optional<Integer> brightness = MinifyChunkManager.getManager(sl).getBrightness(layer, pos);
            if (brightness.isPresent()) {
                return brightness.get();
            }
        }
        return this.getLightEngine().getLayerListener(layer).getLightValue(pos);
    }

//    @Inject(
//        method = "getRawBrightness(Lnet/minecraft/core/BlockPos;I)I",
//        at = @At("RETURN"),
//        cancellable = true
//    )
    @Overwrite
    default int getRawBrightness(BlockPos pos, int num) {
        if((Object) this instanceof ServerLevel sl) {
            Optional<Integer> brightness = MinifyChunkManager.getManager(sl).getRawBrightness(pos, num);
            if (brightness.isPresent()) {
                return brightness.get();
            }
        }
        return this.getLightEngine().getRawBrightness(pos, num);
    }

}
