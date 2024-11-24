package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.blocks.TobaccoCropBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockHandler {
    public static final CropBlock TOBACCO_CROP = (CropBlock) register("tobacco_crop", new TobaccoCropBlock(AbstractBlock.Settings.copy(Blocks.WHEAT)), false);
    public static final Block WILD_TOBACCO = register("wild_tobacco", new Block(AbstractBlock.Settings.copy(Blocks.WHEAT)), true);

    public static Block register(String name, Block block, boolean shouldRegisterItem) {
        Identifier id = Identifier.of(DeadeyeMod.MOD_ID, name);

        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }
}
