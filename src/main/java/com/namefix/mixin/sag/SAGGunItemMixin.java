package com.namefix.mixin.sag;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import net.elidhan.anim_guns.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GunItem.class)
public class SAGGunItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    public void deadeyemod_modifyUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        GunItem gun = (GunItem)((Object)this);
        if(world.isClient()) {
            if(DeadeyeClient.isEnabled) {
                if (hand == Hand.MAIN_HAND && !user.getItemCooldownManager().isCoolingDown(gun) && !user.isSprinting() && GunItem.isLoaded(stack)) {
                    gun.shoot(world, user, stack);
                    user.getItemCooldownManager().set(gun, gun.getRateOfFire()/4);
                }
            }
        } else {
            if(!DeadeyeServer.deadeyeUsers.isEmpty()) {
                if (hand == Hand.MAIN_HAND && !user.getItemCooldownManager().isCoolingDown(gun) && !user.isSprinting() && GunItem.isLoaded(stack)) {
                    gun.shoot(world, user, stack);
                    user.getItemCooldownManager().set(gun, gun.getRateOfFire()/4);
                }
            }
        }
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "getSpreadMult", at = @At("HEAD"), cancellable = true)
    public void deadeyemod_modifyGetSpreadMultServer(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) cir.setReturnValue(0.0f);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getSpreadMult", at = @At("HEAD"), cancellable = true)
    public void deadeyemod_modifyGetSpreadMultClient(ItemStack stack, CallbackInfoReturnable<Float> cir) {
        if(DeadeyeClient.isEnabled) cir.setReturnValue(0.0f);
    }
}
