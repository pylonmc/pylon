package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param inputItem  the inputItem item
 * @param result the output item
 * @param duration the duration of the recipe in ticks
 */
public record BottlingRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemChoice inputItem,
        @NotNull FluidChoice inputFluid,
        @NotNull ItemStack result,
        int duration
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<BottlingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("bottling")) {

        @Override
        protected @NotNull BottlingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new BottlingRecipe(
                    key,
                    section.getOrThrow("input-item", ConfigAdapter.ITEM_CHOICE),
                    section.getOrThrow("input-fluid", ConfigAdapter.FLUID_CHOICE),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("duration", ConfigAdapter.INTEGER)
            );
        }
    };

    @Override
    public @NotNull List<FluidOrItemChoice> getInputs() {
        return List.of(inputItem, inputFluid);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# f i # b # o # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('f', FluidButton.of(inputFluid))
                .addIngredient('i', ItemButton.of(inputItem))
                .addIngredient('b', GuiItems.progressCyclingItem(duration, ItemStackBuilder.copyOf(PylonItems.BOTTLER)))
                .addIngredient('o', ItemButton.of(result))
                .build();
    }
}