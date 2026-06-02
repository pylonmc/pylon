package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.recipes.MoldingRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class GenericBrickMolder extends GenericMachine<MoldingRecipe> {

    public final int ticksPerMoldingCycle = getSettingOrThrow("ticks-per-molding-cycle", ConfigAdapter.INTEGER);

    public GenericBrickMolder(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(MoldingRecipe.RECIPE_TYPE);
    }

    public GenericBrickMolder(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull MoldingRecipe recipe) {
        return recipe.moldingCycles() * tickInterval * ticksPerMoldingCycle;
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull MoldingRecipe recipe) {
        return List.of(recipe.result().clone());
    }
}
