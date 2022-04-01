package com.wynprice.minify.mixin;

import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(
        method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
        at = @At("HEAD")
    )
    private void playSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, CallbackInfo infoReturnable) {
        MinifyChunkManager.getManager((ServerLevel) (Object) this).onSoundPlayed(x, y, z, sound, source, volume, pitch);
    }

    @Inject(
        method = "sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V",
        at = @At("HEAD")
    )
    private void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo info) {
        MinifyChunkManager.getManager((ServerLevel) (Object) this).onBlockChangedAt(pos);
    }

    @Inject(
        method = "tick(Ljava/util/function/BooleanSupplier;)V",
        at = @At("RETURN")
    )
    private void tick(BooleanSupplier hasTime, CallbackInfo info) {
        MinifyChunkManager.getManager((ServerLevel) (Object) this).onTick();
    }

    @Inject(
        method = "doBlockEvent(Lnet/minecraft/world/level/BlockEventData;)Z",
        at = @At("RETURN")
    )
    private void doBlockEvent(BlockEventData data, CallbackInfoReturnable<Boolean> infoReturnable) {
        if(infoReturnable.getReturnValue()) {
            MinifyChunkManager.getManager((ServerLevel) (Object) this).doBlockEvent(data);
        }
    }
}
