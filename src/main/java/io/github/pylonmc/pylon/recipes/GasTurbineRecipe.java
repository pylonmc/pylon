package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidWithAmount;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.*;
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
        @NotNull RecipeInput.Fluid input,
        @NotNull FluidWithAmount output,
        double powerProduction
) implements RebarRecipe {

    public static final RecipeType<GasTurbineRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("gas_turbine")) {
        @Override
        protected @NonNull GasTurbineRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new GasTurbineRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_FLUID),
                    section.getOrThrow("output", ConfigAdapter.FLUID_WITH_AMOUNT),
                    section.getOrThrow("power-production", ConfigAdapter.DOUBLE)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(output.asFluidOrItem());
    }

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
                .addIngredient('i', new FluidButton(input))
                .addIngredient('x', ItemButton.from(ItemStackBuilder.of(PylonItems.GAS_TURBINE.clone())
                        .lore(
                                Component.empty(),
                                Component.translatable("pylon.gui.watts-per-mb", UnitFormat.WATTS_PER_MILLIBUCKET.format(powerProduction / input.amountMillibuckets()).decimalPlaces(1))
                        )
                        .build()))
                .addIngredient('o', new FluidButton(output))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
