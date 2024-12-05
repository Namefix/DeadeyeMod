package com.namefix.mixin.tacz;

import com.namefix.deadeye.DeadeyeServer;
import com.tacz.guns.item.ModernKineticGunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModernKineticGunItem.class)
public class ModernKineticGunItemMixin {
    @ModifyVariable(method = "doSpawnBulletEntity", at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private float deadeye_modifyAccuracy(float inaccuracy) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) {
            return 0f;
        }
        return inaccuracy;
    }
}
