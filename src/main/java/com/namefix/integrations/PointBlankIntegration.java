package com.namefix.integrations;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PointBlankIntegration {
    public static boolean isLoaded = false;

    public static void initialize() {
        isLoaded = true;
    }

    public static void shootGun(GunItem gun, PlayerEntity player, Entity target) {
        gun.tryFire(player, player.getMainHandStack(), target);
    }

    public static boolean canMarkTargets(ItemStack item, int markCount) {
        return markCount < getGunAmmo(item);
    }

    public static boolean canGunShoot(ItemStack item) {
        if(!(item.getItem() instanceof GunItem)) return false;
        PlayerEntity player = MinecraftClient.getInstance().player;
        GunClientState state = GunClientState.getState(player, item, player.getInventory().selectedSlot, false);

        FireMode fireMode = getGunFiremode(item);
        if(fireMode == FireMode.AUTOMATIC && state.isIdle()) return true;
        else if(fireMode == FireMode.AUTOMATIC) return state.isFiring();
        else return state.isIdle();
    }

    public static int getGunAmmo(ItemStack item) {
        if(!(item.getItem() instanceof GunItem)) return 0;
        PlayerEntity player = MinecraftClient.getInstance().player;
        GunClientState state = GunClientState.getState(player, item, player.getInventory().selectedSlot, false);

        return state.getAmmoCount(GunItem.getFireModeInstance(item));
    }

    public static FireMode getGunFiremode(ItemStack item) {
        if(!(item.getItem() instanceof GunItem)) return null;
        return GunItem.getFireModeInstance(item).getType();
    }
}
