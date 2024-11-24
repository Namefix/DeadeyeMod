package com.namefix.datagen;

import com.namefix.handlers.BlockHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends FabricTagProvider<Block> {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BLOCK, registriesFuture);
    }

    public static final TagKey<Block> CROPS = BlockTags.CROPS;
    public static final TagKey<Block> MAINTAINS_FARMLAND = BlockTags.MAINTAINS_FARMLAND;

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(CROPS).add(BlockHandler.TOBACCO_CROP);
        getOrCreateTagBuilder(MAINTAINS_FARMLAND).add(BlockHandler.TOBACCO_CROP);
    }
}
