package com.namefix.mixin.tacz;

import com.namefix.deadeye.DeadeyeClient;
import com.tacz.guns.entity.shooter.LivingEntityDrawGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityDrawGun.class)
public class TACZLivingEntityDrawGunMixin {
    @Inject(method = "getDrawCoolDown", at = @At("HEAD"), cancellable = true, remap = false)
    private void deadeye_modifyDrawCooldown(CallbackInfoReturnable<Long> cir) {
        if(DeadeyeClient.isEnabled) cir.setReturnValue(0L);
    }
}
