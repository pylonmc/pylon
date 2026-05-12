package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidWithAmount;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import kotlin.Pair;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record HeatExchangerRecipe(
        @NotNull NamespacedKey key,
        @NotNull Pair<RecipeInput.@NotNull Fluid, @Nullable FluidWithAmount> transferFrom,
        @NotNull Pair<RecipeInput.@NotNull Fluid, @Nullable FluidWithAmount> transferTo
) implements RebarRecipe {

    public static final RecipeType<HeatExchangerRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("heat_exchanger")) {
        @Override
        protected @NotNull HeatExchangerRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            RecipeInput.Fluid transferFromInput = section.getOrThrow("from.input", ConfigAdapter.RECIPE_INPUT_FLUID);
            FluidWithAmount transferFromOutput = section.get("from.output", ConfigAdapter.FLUID_WITH_AMOUNT);
            RecipeInput.Fluid transferToInput = section.getOrThrow("to.input", ConfigAdapter.RECIPE_INPUT_FLUID);
            FluidWithAmount transferToOutput = section.get("to.output", ConfigAdapter.FLUID_WITH_AMOUNT);
            return new HeatExchangerRecipe(
                    key,
                    new Pair<>(transferFromInput, transferFromOutput),
                    new Pair<>(transferToInput, transferToOutput)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(transferFrom.getFirst(), transferTo.getFirst());
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return Stream.of(transferFrom.getSecond(), transferTo.getSecond())
                .filter(Objects::nonNull)
                .map(FluidWithAmount::asFluidOrItem)
                .toList();
    }

    @Override
    public @NonNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# i # # # # # o #",
                        "# # # # x # # # #",
                        "# I # # # # # O #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', new FluidButton(transferFrom.getFirst()))
                .addIngredient('o', transferFrom.getSecond() != null ? new FluidButton(transferFrom.getSecond()) : GuiItems.backgroundBlack())
                .addIngredient('I', new FluidButton(transferTo.getFirst()))
                .addIngredient('O', transferTo.getSecond() != null ? new FluidButton(transferTo.getSecond()) : GuiItems.backgroundBlack())
                .addIngredient('x', ItemButton.from(PylonItems.HEAT_EXCHANGER))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
