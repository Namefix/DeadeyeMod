package com.namefix.mixin.pointblank;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GunItem.class)
public class PointBlankGunItemMixin {
    @Inject(method = "adjustInaccuracy", at = @At(value = "HEAD"), cancellable = true)
    @Environment(EnvType.SERVER)
    private void modifyAdjustInaccuracy(PlayerEntity player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
        if(DeadeyeServer.deadeyeUsers.containsKey(player.getUuid())) {
            cir.setReturnValue(0.0);
        }
    }

    @Inject(method = "adjustInaccuracy", at = @At(value = "HEAD"), cancellable = true)
    @Environment(EnvType.CLIENT)
    private void modifyAdjustInaccuracyClient(PlayerEntity player, ItemStack itemStack, boolean isAiming, CallbackInfoReturnable<Double> cir) {
        if(DeadeyeClient.isEnabled) {
            cir.setReturnValue(0.0);
        }
    }

    @Inject(method = "getDrawCooldownDuration", at = @At(value = "HEAD"), cancellable = true)
    private void modifyGetDrawCooldownDuration(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Long> cir) {
        if(DeadeyeClient.isEnabled) {
            cir.setReturnValue(0L);
        }
    }
}
