package com.namefix.mixin;

import com.namefix.deadeye.Deadeye;
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
    private static void modifyBowPullProgress(int useTicks, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Float> cir) {
        if(Deadeye.isEnabled) {
            float drawSpeedMultiplier = 4.0f;
            cir.setReturnValue(Math.min((float) useTicks / (20.0f / drawSpeedMultiplier), 1.0f));
        }
    }
}
