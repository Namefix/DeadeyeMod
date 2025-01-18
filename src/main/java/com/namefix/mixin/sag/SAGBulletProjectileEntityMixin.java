package com.namefix.mixin.sag;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeServer;
import net.elidhan.anim_guns.entity.projectile.BulletProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BulletProjectileEntity.class)
public class SAGBulletProjectileEntityMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;FI)V", at = @At("HEAD"), argsOnly = true)
    private static float deadeyemod_modifyDamage(float damage) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty())
            return damage * DeadeyeMod.CONFIG.server.deadeyeDamageMultiplier();
        else
            return damage;
    }
}
