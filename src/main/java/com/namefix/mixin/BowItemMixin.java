package com.namefix.mixin;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.SERVER)
    private static void modifyBowPullProgress(int useTicks, CallbackInfoReturnable<Float> info) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) {
            float drawSpeedMultiplier = 4.0f;
            info.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }

    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.CLIENT)
    private static void modifyBowPullProgressClient(int useTicks, CallbackInfoReturnable<Float> info) {
        if(DeadeyeClient.isEnabled) {
            float drawSpeedMultiplier = 4.0f;
            info.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }
}
