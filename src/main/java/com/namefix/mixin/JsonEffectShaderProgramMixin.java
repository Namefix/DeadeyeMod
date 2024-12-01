package com.namefix.mixin;

import com.namefix.DeadeyeMod;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// All this ain't nice, but couldn't find a better way to do it so this stays until it gets replaced.
@Mixin(JsonEffectShaderProgram.class)
public class JsonEffectShaderProgramMixin {
    @Mutable
    @Shadow @Final private String name;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;", shift = At.Shift.BEFORE))
    public void deadeyemod_onInit(ResourceFactory factory, String name, CallbackInfo ci) {
        this.name = name;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private Identifier deadeyemod_modifyIdentifier(String path) {
        if(name.startsWith("deadeye-mod:")) {
            String deadeyeName = name.replace("deadeye-mod:", "");
            return Identifier.of(DeadeyeMod.MOD_ID, "shaders/program/"+deadeyeName+".json");
        }
        else return Identifier.ofVanilla(path);
    }

    @Redirect(method = "loadEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
    private static Identifier deadeyemod_loadEffect(String path) {
        if(path.startsWith("shaders/program/deadeye-mod:")) {
            String replaced = path.replace("shaders/program/deadeye-mod:", "");
            return Identifier.of(DeadeyeMod.MOD_ID, "shaders/program/"+replaced);
        } else {
            return Identifier.ofVanilla(path);
        }
    }

}
