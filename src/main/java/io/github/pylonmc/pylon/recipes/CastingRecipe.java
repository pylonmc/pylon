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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param input the input inputFluid, of which CAST_AMOUNT mB is used per recipe
 * @param result the output of the cast (respects amount)
 * @param temperature the minimum temperature the smeltery must be
 */
public record CastingRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Fluid input,
        @NotNull ItemStack result,
        double temperature
) implements RebarRecipe {

    public static final RecipeType<CastingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("casting")) {
        @Override
        protected @NotNull CastingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new CastingRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_FLUID),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("temperature", ConfigAdapter.DOUBLE)
            );
        }
    };

    public static @Nullable CastingRecipe getCastRecipeFor(@NotNull RebarFluid fluid) {
        for (CastingRecipe recipe : RECIPE_TYPE) {
            if (recipe.input.contains(fluid)) {
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
    public @NotNull List<RecipeInput> getInputs() {
        return List.of(input);
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
                        "# c # # i t o # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('c', ItemButton.of(PylonItems.SMELTERY_CASTER))
                .addIngredient('i', new FluidButton(input))
                .addIngredient('t', ItemStackBuilder.of(Material.BLAZE_POWDER)
                        .name(net.kyori.adventure.text.Component.translatable(
                                "pylon.guide.recipe.melting",
                                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature))
                        ))
                )
                .addIngredient('o', ItemButton.of(result))
                .build();
    }
}
