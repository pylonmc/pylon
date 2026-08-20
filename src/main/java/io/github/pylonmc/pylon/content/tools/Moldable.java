package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.recipes.MoldingRecipe;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;


public interface Moldable extends Keyed {
    void doMoldingClick();
    boolean isMoldingFinished();

    default ItemStack moldingInputStack() {
        return RebarRegistry.ITEMS.getOrThrow(getKey()).getItemStack();
    }

    default MoldingRecipe moldingRecipe() {
        ItemStack input = moldingInputStack();
        for (MoldingRecipe recipe : MoldingRecipe.RECIPE_TYPE) {
            if (recipe.isInput(input)) {
                return recipe;
            }
        }
        throw new IllegalStateException("Moldable item " + getKey() + " does not have an associated molding recipe");
    }

    default ItemStack moldingResult() {
        return moldingRecipe().result();
    }

    default int totalMoldingClicks() {
        return moldingRecipe().moldingCycles();
    }
}
