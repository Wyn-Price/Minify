package com.wynprice.minify.mixin;

import com.wynprice.minify.management.MinifyChunkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Inject(
        method = "playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
        at = @At("HEAD")
    )
    private void playSound(Player player, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, CallbackInfo infoReturnable) {
        MinifyChunkManager.getManager((ServerLevel) (Object) this).onSoundPlayed(x, y, z, sound, source, volume, pitch);
    }
}
