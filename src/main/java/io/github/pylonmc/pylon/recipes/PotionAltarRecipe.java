package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * For internal use only
 *
 * @param result
 *         the output (respects amount)
 *
 * @author balugaq
 */
public record PotionAltarRecipe(
        @NotNull Player player,
        @NotNull RecipeInput.Item input1,
        @Nullable RecipeInput.Item input2,
        @Nullable PotionCatalyst catalyst,
        @NotNull ItemStack result,
        int timeTicks,
        boolean catalystApplied
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
