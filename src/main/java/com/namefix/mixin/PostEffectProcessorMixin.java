package com.namefix.mixin;

import com.namefix.utils.PostEffectProcessorInterface;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.List;

@Mixin(PostEffectProcessor.class)
public class PostEffectProcessorMixin implements PostEffectProcessorInterface {
    @Shadow @Final private List<PostEffectPass> passes;

    @Unique
    public void deadeyemod_setUniforms(String name, float value) {
        Iterator var3 = passes.iterator();

        while(var3.hasNext()) {
            PostEffectPass postEffectPass = (PostEffectPass)var3.next();
            postEffectPass.getProgram().getUniformByNameOrDummy(name).set(value);
        }
    }
}
