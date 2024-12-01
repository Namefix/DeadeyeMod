package com.namefix.mixin;

import com.namefix.deadeye.DeadeyeClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerAttackMixin extends LivingEntitySwingHandMixin {
	@Override
	protected void deadeyemod_onSwingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
		if(DeadeyeClient.isEnabled) ci.cancel();
    }
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void deadeyemod_onAttack(CallbackInfo ci) {
		if(DeadeyeClient.isEnabled) ci.cancel();
	}
}