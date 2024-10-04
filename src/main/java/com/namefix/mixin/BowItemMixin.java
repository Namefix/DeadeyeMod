package com.namefix.mixin;

import com.namefix.deadeye.Deadeye;
import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "getPullProgress", at = @At("HEAD"), cancellable = true)
    private static void modifyBowPullProgress(int useTicks, CallbackInfoReturnable<Float> info) {
        if(Deadeye.isEnabled) {
            float drawSpeedMultiplier = 4.0f;
            info.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }
}
