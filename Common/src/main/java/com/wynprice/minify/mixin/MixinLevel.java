package com.wynprice.minify.mixin;

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

//    @Inject(
//        method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
//        at = @At("RETURN")
//    )
//    public void setBlock(BlockPos pos, BlockState state, int flags, int recursions, CallbackInfoReturnable<Boolean> info) {
//        if(info.getReturnValue()) {
//            Level thiz = (Level) (Object) this;
//            if(thiz instanceof ServerLevel) {
//                MinifyChunkManager.getManager((ServerLevel) thiz).onBlockChangedAt(pos, state);
//            }
//        }
//    }
//
//
//    @Inject(
//        method = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
//        at = @At("RETURN")
//    )
//    public void setBlockEntity(BlockEntity entity, CallbackInfo info) {
//        Level thiz = (Level) (Object) this;
//        if(thiz instanceof ServerLevel) {
//            MinifyChunkManager.getManager((ServerLevel) thiz).onBlockChangedAt(entity.getBlockPos(), thiz.getBlockState(entity.getBlockPos()));
//        }
//    }
//
//
//    @Inject(
//        method = "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V",
//        at = @At("RETURN")
//    )
//    public void removeBlockEntity(BlockPos pos, CallbackInfo info) {
//        Level thiz = (Level) (Object) this;
//        if(thiz instanceof ServerLevel) {
//            MinifyChunkManager.getManager((ServerLevel) thiz).onBlockChangedAt(pos, thiz.getBlockState(pos));
//        }
//    }
}
