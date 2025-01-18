package com.namefix.mixin.tacz;

import com.namefix.deadeye.DeadeyeServer;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InaccuracyType.class)
public class TACZInaccuracyTypeMixin {
    @Inject(method = "getInaccuracyType", at = @At("HEAD"), cancellable = true)
    private static void deadeye_getInaccuracyType(CallbackInfoReturnable<InaccuracyType> cir) {
        if(!DeadeyeServer.deadeyeUsers.isEmpty()) cir.setReturnValue(InaccuracyType.AIM);
    }
}
