package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.RecipeProcessorRebarBlock;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class GenericGrindstone extends GenericMachine<GrindstoneRecipe> implements RecipeProcessorRebarBlock<GrindstoneRecipe> {
    @SuppressWarnings("unused")
    public GenericGrindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setRecipeType(GrindstoneRecipe.RECIPE_TYPE);
    }

    @SuppressWarnings("unused")
    public GenericGrindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull GrindstoneRecipe recipe) {
        return recipe.cycles() * Grindstone.CYCLE_DURATION_TICKS;
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull GrindstoneRecipe recipe) {
        return List.of(recipe.results().getRandom());
    }
}
