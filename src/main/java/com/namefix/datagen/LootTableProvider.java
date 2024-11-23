package com.namefix.datagen;

import com.namefix.blocks.TobaccoCropBlock;
import com.namefix.handlers.BlockHandler;
import com.namefix.handlers.ItemHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LootTableProvider extends FabricBlockLootTableProvider {
    public LootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        BlockStatePropertyLootCondition.Builder builder2 = BlockStatePropertyLootCondition.builder(BlockHandler.TOBACCO_CROP)
                .properties(StatePredicate.Builder.create().exactMatch(TobaccoCropBlock.AGE, TobaccoCropBlock.MAX_AGE));
        this.addDrop(BlockHandler.TOBACCO_CROP, this.cropDrops(BlockHandler.TOBACCO_CROP, ItemHandler.TOBACCO, ItemHandler.TOBACCO_SEEDS, builder2));
    }
}
