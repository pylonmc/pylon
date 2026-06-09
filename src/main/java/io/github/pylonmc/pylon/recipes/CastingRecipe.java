package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.recipe.ingredients.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredients.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredients.ItemChoice;
import io.github.pylonmc.rebar.recipe.ingredients.FluidOrItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record CastingRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemChoice mold,
        @NotNull FluidChoice input,
        @NotNull ItemStack result
) implements RebarRecipe {

    public static final RecipeType<CastingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("casting")) {
        @Override
        protected @NotNull CastingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new CastingRecipe(
                    key,
                    section.getOrThrow("mold", ConfigAdapter.ITEM_CHOICE),
                    section.getOrThrow("input", ConfigAdapter.FLUID_CHOICE),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
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
                .addIngredient('f', FluidButton.of(input))
                .addIngredient('c', ItemButton.of(mold))
                .addIngredient('C', ItemButton.of(PylonItems.CASTING_UNIT))
                .addIngredient('r', ItemButton.of(result))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
