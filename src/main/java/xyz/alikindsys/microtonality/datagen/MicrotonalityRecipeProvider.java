package xyz.alikindsys.microtonality.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class MicrotonalityRecipeProvider extends FabricRecipeProvider {
    public MicrotonalityRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
        return new RecipeProvider(provider, recipeOutput) {
            @Override
            public void buildRecipes() {
                HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                shaped(RecipeCategory.TOOLS, Items.CRAFTING_TABLE)
                        .pattern("N N")
                        .pattern("NNN")
                        .pattern(" B ")
                        .define('N', Items.IRON_NUGGET)
                        .define('B', Items.NOTE_BLOCK)
                        // This is new mostly cause I found it cute. Not a part of the original mod.
                        // Datagen makes it so easy.
                        .unlockedBy(getHasName(Items.NOTE_BLOCK), has(Items.CRAFTING_TABLE))
                        .save(output);

            }
        };
    }

    @Override
    public String getName() {
        return "MicrotonalityRecipeProvider";
    }
}
