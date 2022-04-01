package com.wynprice.minify.mixin;

import com.wynprice.minify.client.MinifyViewerClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(
        method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V",
        at = @At("RETURN")
    )
    public void setLevel(ClientLevel level, CallbackInfo info) {
        MinifyViewerClientLevel.INSTANCE = new MinifyViewerClientLevel(level);
        MinifyViewerClientLevel.SECONDARY_INSTANCE = new MinifyViewerClientLevel(level);
    }
}
