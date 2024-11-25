package com.namefix.datagen;

import com.namefix.DeadeyeMod;
import com.namefix.handlers.ItemHandler;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput generator, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(generator, registryLookup);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // Tobacco Seeds
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ItemHandler.TOBACCO_SEEDS)
                .input(ItemHandler.TOBACCO)
                .criterion(hasItem(ItemHandler.TOBACCO), conditionsFromItem(ItemHandler.TOBACCO))
                .offerTo(exporter);

        // Tobacco Water
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BREWING, ItemHandler.TOBACCO_WATER)
                .input(Items.POTION)
                .input(ItemHandler.TOBACCO)
                .criterion(hasItem(Items.POTION), conditionsFromItem(Items.POTION))
                .criterion(hasItem(ItemHandler.TOBACCO), conditionsFromItem(ItemHandler.TOBACCO))
                .offerTo(exporter);

        // Potent Tobacco Water
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BREWING, ItemHandler.POTENT_TOBACCO_WATER)
                .input(Items.POTION)
                .input(ItemHandler.TOBACCO)
                .input(ItemHandler.TOBACCO)
                .criterion(hasItem(Items.POTION), conditionsFromItem(Items.POTION))
                .criterion(hasItem(ItemHandler.TOBACCO), conditionsFromItem(ItemHandler.TOBACCO))
                .offerTo(exporter);

        // Special Tobacco Water
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BREWING, ItemHandler.SPECIAL_TOBACCO_WATER)
                .input(Items.POTION)
                .input(ItemHandler.TOBACCO)
                .input(ItemHandler.TOBACCO)
                .input(ItemHandler.TOBACCO)
                .criterion(hasItem(Items.POTION), conditionsFromItem(Items.POTION))
                .criterion(hasItem(ItemHandler.TOBACCO), conditionsFromItem(ItemHandler.TOBACCO))
                .offerTo(exporter);

        // Cigarette
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ItemHandler.CIGARETTE)
                .input(ItemHandler.TOBACCO)
                .input(Items.PAPER)
                .criterion(hasItem(ItemHandler.TOBACCO), conditionsFromItem(ItemHandler.TOBACCO))
                .criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
                .offerTo(exporter);


        offerSmelting(exporter, List.of(ItemHandler.TOBACCO_WATER), RecipeCategory.BREWING, ItemHandler.SNAKE_OIL, 0.5f, 200, DeadeyeMod.MOD_ID);
        offerSmelting(exporter, List.of(ItemHandler.POTENT_TOBACCO_WATER), RecipeCategory.BREWING, ItemHandler.POTENT_SNAKE_OIL, 1.0f, 200, DeadeyeMod.MOD_ID);
        offerSmelting(exporter, List.of(ItemHandler.SPECIAL_TOBACCO_WATER), RecipeCategory.BREWING, ItemHandler.SPECIAL_SNAKE_OIL, 1.5f, 200, DeadeyeMod.MOD_ID);
        offerSmelting(exporter, List.of(ItemHandler.TOBACCO), RecipeCategory.BREWING, ItemHandler.CHEWING_TOBACCO, 0.5f, 200, DeadeyeMod.MOD_ID);
    }
}
