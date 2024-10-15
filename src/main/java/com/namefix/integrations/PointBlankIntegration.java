package com.namefix.integrations;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.MinecraftClient;
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

    public static boolean canMarkTargets(GunItem gun, int markCount) {
        return markCount < getGunAmmo(gun);
    }

    public static boolean canGunShoot(GunItem gun) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        GunClientState state = GunClientState.getState(player, player.getMainHandStack(), player.getInventory().selectedSlot, false);

        return state.isIdle();
    }

    public static int getGunAmmo(GunItem gun) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        GunClientState state = GunClientState.getState(player, player.getMainHandStack(), player.getInventory().selectedSlot, false);

        return state.getAmmoCount(gun.getMainFireModes().getFirst());
    }
}
