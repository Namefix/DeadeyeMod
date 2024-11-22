package com.namefix.handlers;

import com.namefix.DeadeyeMod;
import com.namefix.items.SnakeOilItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemHandler {
    public static final Item SNAKE_OIL = register(new SnakeOilItem(new PotionItem.Settings(), 1), "snake_oil");
    public static final Item POTENT_SNAKE_OIL = register(new SnakeOilItem(new PotionItem.Settings(), 2), "potent_snake_oil");
    public static final Item SPECIAL_SNAKE_OIL = register(new SnakeOilItem(new PotionItem.Settings(), 3), "special_snake_oil");

    public static Item register(Item item, String id) {
        Identifier itemID = Identifier.of(DeadeyeMod.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemID, item);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.SNAKE_OIL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.POTENT_SNAKE_OIL));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register((itemGroup) -> itemGroup.add(ItemHandler.SPECIAL_SNAKE_OIL));
    }
}
