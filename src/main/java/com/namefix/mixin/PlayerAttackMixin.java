package com.namefix.mixin;

import com.namefix.deadeye.Deadeye;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerAttackMixin extends LivingEntitySwingHandMixin {
	@Override
	protected void onSwingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
		if(Deadeye.isEnabled) ci.cancel();
    }
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void onAttack(CallbackInfo ci) {
		if(Deadeye.isEnabled) ci.cancel();
	}
}