package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param result the output (respects amount)
 */
public record PotionAltarRecipe(
        @NotNull RecipeInput.Item input1,
        @NotNull RecipeInput.Item input2,
        @NotNull ItemStack result,
        int timeTicks
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return PylonKeys.POTION_ALTAR;
    }

    public static final RecipeType<PotionAltarRecipe> RECIPE_TYPE = new RecipeType<>(pylonKey("potion_altar"));

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        List<RecipeInput> inputResult = new ArrayList<>();
        inputResult.add(input1);
        inputResult.add(input2);
        return inputResult;
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder().build();
    }
}
