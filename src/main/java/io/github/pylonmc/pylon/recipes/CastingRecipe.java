package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record CastingRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemStack mold,
        @NotNull RecipeInput.Fluid input,
        @NotNull ItemStack result
) implements RebarRecipe {

    public static final RecipeType<CastingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("casting")) {
        @Override
        protected @NotNull CastingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            ItemStack cast = section.getOrThrow("mold", ConfigAdapter.ITEM_STACK);
            RecipeInput.Fluid input = section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_FLUID);

            for (CastingRecipe recipe : this) {
                if (recipe.mold().isSimilar(cast)) {
                    if (recipe.input().amountMillibuckets() != input.amountMillibuckets()) {
                        throw new IllegalArgumentException("All casting recipes with the same mold must have the same fluid use: recipe %s uses %f mB but recipe %s uses %f mB, but they use the same mold".formatted(recipe.getKey(), recipe.input().amountMillibuckets(), key, input.amountMillibuckets()));
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
    public Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # c # # # #",
                        "# # # f C r # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('f', new FluidButton(input))
                .addIngredient('c', new ItemButton(mold))
                .addIngredient('C', new ItemButton(PylonItems.CASTING_UNIT))
                .addIngredient('r', new ItemButton(result))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
