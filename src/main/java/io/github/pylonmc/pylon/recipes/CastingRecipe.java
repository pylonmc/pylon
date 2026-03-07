package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.recipe.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record CastingRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemStack cast,
        @NotNull RecipeInput.Fluid input,
        @NotNull ItemStack result
) implements RebarRecipe {

    public static final RecipeType<CastingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("casting")) {
        @Override
        protected @NonNull CastingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            ItemStack cast = section.getOrThrow("cast", ConfigAdapter.ITEM_STACK);
            RecipeInput.Fluid input = section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_FLUID);

            for (CastingRecipe recipe : this) {
                if (recipe.cast().isSimilar(cast)) {
                    if (recipe.input().amountMillibuckets() != input.amountMillibuckets()) {
                        throw new IllegalArgumentException("All casting recipes with the same cast must have the same fluid use: recipe %s uses %f mB but recipe %s uses %f mB, but they use the same cast".formatted(recipe.getKey(), recipe.input().amountMillibuckets(), key, input.amountMillibuckets()));
                    } else {
                        break;
                    }
                }
            }

            return new CastingRecipe(
                    key,
                    cast,
                    input,
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @Nullable Gui display() {
        return null;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
