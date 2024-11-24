package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.items.CigaretteItem;
import com.namefix.items.SnakeOilItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemHandler {
    public static final Item TOBACCO_SEEDS = register("tobacco_seeds", new AliasedBlockItem(BlockHandler.TOBACCO_CROP, new Item.Settings()));
    public static final Item TOBACCO = register("tobacco", new Item(new Item.Settings()));

    public static final Item TOBACCO_WATER = register("tobacco_water", new Item(new Item.Settings()));
    public static final Item POTENT_TOBACCO_WATER = register("potent_tobacco_water", new Item(new Item.Settings()));
    public static final Item SPECIAL_TOBACCO_WATER = register("special_tobacco_water", new Item(new Item.Settings()));

    public static final Item SNAKE_OIL = register("snake_oil", new SnakeOilItem(new PotionItem.Settings(), 1));
    public static final Item POTENT_SNAKE_OIL = register("potent_snake_oil", new SnakeOilItem(new PotionItem.Settings(), 2));
    public static final Item SPECIAL_SNAKE_OIL = register("special_snake_oil", new SnakeOilItem(new PotionItem.Settings(), 3));

    public static final Item CIGARETTE = register("cigarette", new CigaretteItem(new PotionItem.Settings()));

    public static Item register(String id, Item item) {
        Identifier itemID = Identifier.of(DeadeyeMod.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ItemHandler.TOBACCO_SEEDS));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                .register((itemGroup) -> itemGroup.add(ItemHandler.TOBACCO));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ItemHandler.TOBACCO_WATER));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ItemHandler.POTENT_TOBACCO_WATER));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> itemGroup.add(ItemHandler.SPECIAL_TOBACCO_WATER));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.SNAKE_OIL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.POTENT_SNAKE_OIL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.SPECIAL_SNAKE_OIL));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.CIGARETTE));
    }
}
