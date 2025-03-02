package com.namefix.integrations;

import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.FireMode;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PointBlankIntegration {
    public static boolean isLoaded = false;

    public static void initialize() {
        isLoaded = true;
    }

    public static boolean isItemGun(ItemStack stack) {
        if(!isLoaded) return false;
        return stack.getItem() instanceof GunItem;
    }

    public static void shootGun(GunItem gun, PlayerEntity player, Entity target) {
        gun.tryFire(player, player.getMainHandStack(), target);
    }

    public static boolean canMarkTargets(ItemStack item, int markCount) {
        return markCount < getGunAmmo(item);
    }

    @Environment(EnvType.CLIENT)
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
        return GunItem.getAmmo(item, GunItem.getFireModeInstance(item));
    }

    public static FireMode getGunFiremode(ItemStack item) {
        if(!(item.getItem() instanceof GunItem)) return null;
        return GunItem.getFireModeInstance(item).getType();
    }

    public static int getMaxAmmo(ItemStack item) {
        if(!((item.getItem()) instanceof GunItem gun)) return 0;
        return gun.getMaxAmmoCapacity(item, GunItem.getFireModeInstance(item));
    }
}
