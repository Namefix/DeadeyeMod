package com.namefix.integrations;

import net.elidhan.anim_guns.item.GunItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SAGIntegration {
    public static boolean isLoaded = false;

    public static void initialize() {
        isLoaded = true;
    }

    public static boolean isItemGun(ItemStack stack) {
        if(!isLoaded) return false;
        return stack.getItem() instanceof GunItem;
    }

    public static void shootGun(PlayerEntity player, ItemStack item) {
        if(!(item.getItem() instanceof GunItem gun)) return;

        gun.shoot(player.getWorld(), player, item);
    }

    public static boolean canMarkTargets(ItemStack item, int markCount) {
        if(!(item.getItem() instanceof GunItem)) return false;

        return markCount < getGunAmmo(item);
    }

    public static boolean canGunShoot(PlayerEntity user, ItemStack item) {
        if(!(item.getItem() instanceof GunItem gun)) return false;

        return !user.getItemCooldownManager().isCoolingDown(gun) && !user.isSprinting() && GunItem.isLoaded(item);
    }

    public static int getGunAmmo(ItemStack item) {
        if(!(item.getItem() instanceof GunItem)) return 0;

        return GunItem.remainingAmmo(item);
    }
}
