package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidWithAmount;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record GasTurbineRecipe(
        @NotNull NamespacedKey key,
        @NotNull FluidChoice input,
        @NotNull FluidWithAmount output,
        double powerProduction
) implements RebarRecipe {

    public static final RecipeType<GasTurbineRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("gas_turbine")) {
        @Override
        protected @NonNull GasTurbineRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new GasTurbineRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.FLUID_CHOICE),
                    section.getOrThrow("output", ConfigAdapter.FLUID_WITH_AMOUNT),
                    section.getOrThrow("power-production", ConfigAdapter.DOUBLE)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(output);
    }

    private static final UnitFormat WATTS_PER_MILLIBUCKET = UnitFormat.WATTS.divide(UnitFormat.MILLIBUCKETS);

    @Override
    public @NonNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# i # # x # # o #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', FluidButton.of(input))
                .addIngredient('x', ItemButton.of(ItemStackBuilder.of(PylonItems.GAS_TURBINE.clone())
                        .lore(
                                Component.empty(),
                                Component.translatable(
                                        "pylon.gui.watts-per-mb",
                                        RebarArgument.of("power", WATTS_PER_MILLIBUCKET.format(powerProduction / input.getAmount()).decimalPlaces(1))
                                )
                        )
                        .build()))
                .addIngredient('o', FluidButton.of(output))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
