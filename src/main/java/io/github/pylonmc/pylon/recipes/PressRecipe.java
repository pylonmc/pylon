package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.simple.Press;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
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
 * @param input input item (assumed to be of item amount 1)
 */
public record PressRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        double oilAmount
) implements RebarRecipe {

    public static final RecipeType<PressRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("press")) {
        @Override
        protected @NotNull PressRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new PressRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("oil-amount", ConfigAdapter.DOUBLE)
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
        return List.of(FluidOrItem.of(PylonFluids.PLANT_OIL, oilAmount));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# p # # i c o # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('p', ItemButton.of(PylonItems.PRESS))
                .addIngredient('i', ItemButton.of(input))
                .addIngredient('c', GuiItems.progressCyclingItem(Press.TIME_PER_ITEM_TICKS,
                        ItemStackBuilder.of(Material.CLOCK)
                                .name(net.kyori.adventure.text.Component.translatable(
                                        "pylon.guide.recipe.press",
                                        RebarArgument.of("time", UnitFormat.SECONDS.format(Press.TIME_PER_ITEM_TICKS / 20.0))
                                ))
                ))
                .addIngredient('o', new FluidButton(oilAmount, PylonFluids.PLANT_OIL))
                .build();
    }
}