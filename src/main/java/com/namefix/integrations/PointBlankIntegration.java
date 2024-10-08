package com.namefix.integrations;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PointBlankIntegration {
    public static boolean isLoaded = false;

    public static void initialize() {
        isLoaded = true;
    }

    public static void shootGun(GunItem gun, PlayerEntity player, Entity target) {
        gun.tryFire(player, player.getMainHandStack(), target);
    }
}
