package com.namefix.mixin.tacz;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeServer;
import com.tacz.guns.entity.EntityKineticBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityKineticBullet.class)
public class TACZEntityKineticBulletMixin {
    @ModifyReturnValue(method = "getDamage", at = @At("RETURN"))
    private float deadeyemod_getDamage(float original) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty())
            return original * DeadeyeMod.CONFIG.server.deadeyeDamageMultiplier();
        else
            return original;
    }
}
