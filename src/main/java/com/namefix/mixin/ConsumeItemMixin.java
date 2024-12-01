package com.namefix.mixin;

import com.namefix.deadeye.DeadeyeServer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ConsumeItemMixin {
    @Inject(method="consumeItem", at=@At("HEAD"))
    private void deadeyemod_modifyEatFood(CallbackInfo ci) {
        if(((Object) this) instanceof PlayerEntity player) {
            if(player.getWorld().isClient) return;
            ItemStack item = player.getActiveItem();
            FoodComponent foodComponent = item.getItem().getFoodComponent();
            if(foodComponent == null) return;

            if (foodComponent.getHunger() >= 8) {
                DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) player, foodComponent.getHunger(), false);
            } else {
                DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) player, foodComponent.getHunger()/2f, true);
            }
        }
    }
}
