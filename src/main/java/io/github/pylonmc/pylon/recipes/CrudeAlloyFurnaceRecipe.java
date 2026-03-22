package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


/**
 * @param input1 the first input item (respects amount)
 * @param input2 the second input item (respects amount)
 * @param result the output item (respects amount)
 * @param timeTicks the recipe time in ticks
 */
public record CrudeAlloyFurnaceRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input1,
        @NotNull RecipeInput.Item input2,
        @NotNull ItemStack result,
        int timeTicks
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<CrudeAlloyFurnaceRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("crude_alloy_furnace")) {
        @Override
        protected @NotNull CrudeAlloyFurnaceRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new CrudeAlloyFurnaceRecipe(
                    key,
                    section.getOrThrow("input1", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("input2", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("time-ticks", ConfigAdapter.INTEGER)
            );
        }
    };

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        return List.of(input1, input2);
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
                        "# # i j b o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', ItemButton.from(input1))
                .addIngredient('j', ItemButton.from(input2))
                .addIngredient('b', PylonItems.CRUDE_ALLOY_FURNACE)
                .addIngredient('o', ItemButton.from(result))
                .build();
    }
}