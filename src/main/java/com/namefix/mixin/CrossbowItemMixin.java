package com.namefix.mixin;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.SERVER)
    private static void deadeyemod_modifyBowPullProgress(int useTicks, ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) {
            float drawSpeedMultiplier = 4.0f;
            cir.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }

    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.CLIENT)
    private static void deadeyemod_modifyBowPullProgressClient(int useTicks, ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if(DeadeyeClient.isEnabled) {
            float drawSpeedMultiplier = 4.0f;
            cir.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }
}
