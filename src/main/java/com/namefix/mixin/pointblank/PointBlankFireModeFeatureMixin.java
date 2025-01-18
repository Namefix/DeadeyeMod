package com.namefix.mixin.pointblank;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireModeFeature.class)
public class PointBlankFireModeFeatureMixin {
    @Inject(method = "getPelletCountAndSpread", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.SERVER)
    private static void deadeyemod_getPelletCountAndSpread(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Pair<Integer, Double>> cir) {
        if(DeadeyeServer.deadeyeUsers.containsKey(player.getUuid())) {
            Item var4 = itemStack.getItem();
            if (var4 instanceof GunItem gunItem) {
                FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
                cir.setReturnValue(fireModeInstance == null ? Pair.of(gunItem.getPelletCount(), .0) : Pair.of(fireModeInstance.getPelletCount(), .0));
            } else {
                cir.setReturnValue(Pair.of(0, .0));
            }
        }
    }

    @Inject(method = "getPelletCountAndSpread", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.CLIENT)
    private static void deadeyemod_getPelletCountAndSpreadClient(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Pair<Integer, Double>> cir) {
        if(DeadeyeClient.isEnabled) {
            Item var4 = itemStack.getItem();
            if (var4 instanceof GunItem gunItem) {
                FireModeInstance fireModeInstance = GunItem.getFireModeInstance(itemStack);
                cir.setReturnValue(fireModeInstance == null ? Pair.of(gunItem.getPelletCount(), .0) : Pair.of(fireModeInstance.getPelletCount(), .0));
            } else {
                cir.setReturnValue(Pair.of(0, .0));
            }
        }
    }

    @ModifyReturnValue(method = "getDamage", at = @At("RETURN"))
    private static float deadeyemod_getDamage(float original) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) {
            return original * DeadeyeMod.CONFIG.server.deadeyeDamageMultiplier();
        }
        return original;
    }
}
