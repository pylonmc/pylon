package io.github.pylonmc.pylon.recipes;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.api.MeltingPoint;
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
import xyz.xenondevs.invui.gui.Gui;

public record MeltingRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        @NotNull RebarFluid result,
        double resultAmount
) implements RebarRecipe {

    public static final RecipeType<MeltingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("melting")) {
        @Override
        protected @NotNull MeltingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new MeltingRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.REBAR_FLUID),
                    section.getOrThrow("amount", ConfigAdapter.DOUBLE)
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
                .addIngredient('h', ItemButton.from(PylonItems.SMELTERY_HOPPER))
                .addIngredient('i', ItemButton.from(input))
                .addIngredient('t', ItemStackBuilder.of(Material.BLAZE_POWDER)
                        .name(Component.translatable(
                                "pylon.guide.recipe.melting",
                                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(result.getTag(MeltingPoint.class).temperature()))
                        ))
                )
                .addIngredient('o', new FluidButton(resultAmount, result))
                .build();
    }
}
