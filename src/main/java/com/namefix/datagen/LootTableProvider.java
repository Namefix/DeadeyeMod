package com.namefix.datagen;

import com.namefix.blocks.TobaccoCropBlock;
import com.namefix.handlers.BlockHandler;
import com.namefix.handlers.ItemHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.predicate.StatePredicate;

public class LootTableProvider extends FabricBlockLootTableProvider {
    public LootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        BlockStatePropertyLootCondition.Builder builder2 = BlockStatePropertyLootCondition.builder(BlockHandler.TOBACCO_CROP)
                .properties(StatePredicate.Builder.create().exactMatch(TobaccoCropBlock.AGE, TobaccoCropBlock.MAX_AGE));
        addDrop(BlockHandler.TOBACCO_CROP, cropDrops(BlockHandler.TOBACCO_CROP, ItemHandler.TOBACCO, ItemHandler.TOBACCO_SEEDS, builder2));

        addDrop(BlockHandler.WILD_TOBACCO, ItemHandler.TOBACCO);
    }
}
