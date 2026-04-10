package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param input the input item (assumed to have an amount of one)
 * @param result the output inputFluid, of which MELT_AMOUNT will is produced per recipe
 * @param temperature the minimum temperature the smeltery must be at
 */
public record MeltingRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        @NotNull RebarFluid result,
        double resultAmount,
        double temperature
) implements RebarRecipe {

    public static final RecipeType<MeltingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("melting")) {
        @Override
        protected @NotNull MeltingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new MeltingRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.REBAR_FLUID),
                    section.getOrThrow("amount", ConfigAdapter.DOUBLE),
                    section.getOrThrow("temperature", ConfigAdapter.DOUBLE)
            );
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result, resultAmount));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# h # # i t o # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('h', ItemButton.of(PylonItems.SMELTERY_HOPPER))
                .addIngredient('i', ItemButton.of(input))
                .addIngredient('t', ItemStackBuilder.of(Material.BLAZE_POWDER)
                        .name(net.kyori.adventure.text.Component.translatable(
                                "pylon.guide.recipe.melting",
                                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature))
                        ))
                )
                .addIngredient('o', new FluidButton(resultAmount, result))
                .build();
    }
}
