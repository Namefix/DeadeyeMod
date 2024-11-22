package com.namefix.mixin;

import com.namefix.DeadeyeMod;
import com.namefix.deadeye.DeadeyeServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ConsumeItemMixin {
    @Inject(method="consumeItem", at=@At("HEAD"))
    private void modifyEatFood(CallbackInfo ci) {
        if(((Object) this) instanceof PlayerEntity player) {
            if(player.getWorld().isClient) return;
            ItemStack item = player.getActiveItem();
            FoodComponent foodComponent = item.get(DataComponentTypes.FOOD);
            if(foodComponent == null) return;

            if (foodComponent.nutrition() >= 8) {
                DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) player, foodComponent.nutrition(), false);
            } else {
                DeadeyeServer.addDeadeyeCore((ServerPlayerEntity) player, foodComponent.nutrition()/2f, true);
            }
        }
    }
}
