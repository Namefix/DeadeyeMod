package com.namefix.mixin.pointblank;

import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(GunItem.class)
public class PointBlankGunItemMixin implements IMixinConfigPlugin {
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

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("pointblank");
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
