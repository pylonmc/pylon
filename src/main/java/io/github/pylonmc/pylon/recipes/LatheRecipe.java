package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record LatheRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemChoice input,
        @NotNull ItemStack result,
        @Nullable ItemStack byproduct,
        float completionValue,
        float marginOfError
) implements RebarRecipe {

    public LatheRecipe {
        Preconditions.checkArgument(completionValue >= 0 && completionValue <= 1, "completionValue must be in [0, 1]");
        Preconditions.checkArgument(marginOfError >= 0 && marginOfError <= 1, "marginOfError must be in [0, 1]");
    }

    public static final RecipeType<LatheRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("lathe")) {
        @Override
        protected @NonNull LatheRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            float completionValue = section.getOrThrow("completion-value", ConfigAdapter.FLOAT);
            Preconditions.checkState(completionValue >= 0, "completion-value must be >= 0");
            Preconditions.checkState(completionValue <= 1, "completion-value must be <= 1");
            float marginOfError = section.getOrThrow("margin-of-error", ConfigAdapter.FLOAT);
            Preconditions.checkState(marginOfError >= 0, "margin-of-error must be >= 0");
            Preconditions.checkState(marginOfError <= 1, "margin-of-error must be <= 1");
            return new LatheRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.ITEM_CHOICE),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.get("byproduct", ConfigAdapter.ITEM_STACK),
                    completionValue,
                    marginOfError
            );
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NonNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # # i l o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', ItemButton.of(input))
                .addIngredient('l', PylonItems.LATHE)
                .addIngredient('o', ItemButton.of(result))
                .build();
    }
}
