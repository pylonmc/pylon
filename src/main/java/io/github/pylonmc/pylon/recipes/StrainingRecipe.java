package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record StrainingRecipe(
        @NotNull NamespacedKey key,
        @NotNull FluidChoice input,
        @NotNull RebarFluid outputFluid,
        @NotNull ItemStack outputItem
) implements RebarRecipe {

    public static final RecipeType<StrainingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("fluid_strainer")) {
        @Override
        protected @NotNull StrainingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new StrainingRecipe(
                    key,
                    section.getOrThrow("input-fluid", ConfigAdapter.FLUID_CHOICE),
                    section.getOrThrow("output-fluid", ConfigAdapter.REBAR_FLUID),
                    section.getOrThrow("output-item", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    public static @Nullable StrainingRecipe getRecipeForFluid(RebarFluid fluid) {
        for (StrainingRecipe recipe : RECIPE_TYPE) {
            if (recipe.input().getFluids().contains(fluid)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<FluidOrItemChoice> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(outputItem), FluidOrItem.of(outputFluid, input.getAmount()));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # i # # # # #",
                        "# # # s # t # # #",
                        "# # # o # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', FluidButton.of(input))
                .addIngredient('s', PylonItems.FLUID_STRAINER)
                .addIngredient('o', FluidButton.of(input.getAmount(), outputFluid))
                .addIngredient('t', ItemButton.of(outputItem))
                .build();
    }
}