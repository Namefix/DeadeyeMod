package com.namefix.integrations;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

// Timeless and Classics Zero mod integration
public class TACZIntegration {
    public static boolean isLoaded = false;

    public static void initialize() {
        isLoaded = true;
    }

    public static void shootGun() {
        IClientPlayerGunOperator.fromLocalPlayer(MinecraftClient.getInstance().player).shoot();
    }

    public static boolean canGunShoot(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        IGun gun = IGun.getIGunOrNull(stack);
        if(gun == null) return false;

        // this doesn't work
        FireMode fireMode = gun.getFireMode(stack);
        long shootCooldown = IClientPlayerGunOperator.fromLocalPlayer(client.player).getClientShootCoolDown();

        if(fireMode.equals(FireMode.AUTO) && shootCooldown == 0) return true;
        else if(fireMode.equals(FireMode.AUTO)) return shootCooldown > 0;
        else return shootCooldown == 0;
    }

    public static boolean canMarkTargets(ItemStack stack, int markCount) {
        return markCount < getGunAmmo(stack);
    }

    public static int getGunAmmo(ItemStack stack) {
        IGun gun = IGun.getIGunOrNull(stack);
        if(gun == null) return -1;

        Identifier gunId = gun.getGunId(stack);
        Optional gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        CommonGunIndex gunIndex = (CommonGunIndex)gunIndexOptional.get();
        boolean hasAmmoInBarrel = gun.hasBulletInBarrel(stack) && gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT;
        return gun.getCurrentAmmoCount(stack)+(hasAmmoInBarrel?1:0);
    }

    public static void setDrawCooldown(ServerPlayerEntity player) {
        ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(player, 0L);
    }
}
