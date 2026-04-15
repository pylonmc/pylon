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

    default ItemStack moldingResult() {
        for (MoldingRecipe recipe : MoldingRecipe.RECIPE_TYPE) {
            if (recipe.isInput(moldingInputStack())) {
                return recipe.result();
            }
        }
        throw new IllegalStateException("Moldable item " + getKey() + " does not have an associated molding recipe");
    }

    default int totalMoldingClicks() {
        for (MoldingRecipe recipe : MoldingRecipe.RECIPE_TYPE) {
            if (recipe.isInput(moldingInputStack())) {
                return recipe.moldingCycles();
            }
        }
        throw new IllegalStateException("Moldable item " + getKey() + " does not have an associated molding recipe");
    }
}
