package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.world.feature.WildTobaccoFeature;
import com.namefix.world.feature.WildTobaccoFeatureConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

public class FeatureHandler {
    public static final Identifier WILD_TOBACCO_FEATURE_ID = Identifier.of(DeadeyeMod.MOD_ID, "wild_tobacco");
    public static final WildTobaccoFeature WILD_TOBACCO_FEATURE = new WildTobaccoFeature(WildTobaccoFeatureConfig.CODEC);

    public static void initialize() {
        Registry.register(Registries.FEATURE, WILD_TOBACCO_FEATURE_ID, WILD_TOBACCO_FEATURE);

        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.VEGETAL_DECORATION,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, WILD_TOBACCO_FEATURE_ID));
    }
}
