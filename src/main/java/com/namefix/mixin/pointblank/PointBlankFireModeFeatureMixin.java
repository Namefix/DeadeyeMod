package com.namefix.mixin.pointblank;

import com.mojang.datafixers.util.Pair;
import com.namefix.deadeye.DeadeyeClient;
import com.namefix.deadeye.DeadeyeServer;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.feature.FireModeFeature;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
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

@Mixin(FireModeFeature.class)
public class PointBlankFireModeFeatureMixin implements IMixinConfigPlugin {
    @Inject(method = "getPelletCountAndSpread", at = @At("HEAD"), cancellable = true)
    @Environment(EnvType.SERVER)
    private static void getPelletCountAndSpread(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Pair<Integer, Double>> cir) {
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
    private static void getPelletCountAndSpreadClient(LivingEntity player, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Pair<Integer, Double>> cir) {
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
