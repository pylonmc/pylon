package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.boiler.AbstractBoiler;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.*;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public record BoilerDisplayRecipe(
        NamespacedKey key,
        ItemStack boiler,
        FluidWithAmount input,
        FluidWithAmount result
) implements RebarRecipe {

    public static final RecipeType<BoilerDisplayRecipe> RECIPE_TYPE = new RecipeType<>(pylonKey("boiler_display"));

    static {
        for (RebarItemSchema item : RebarRegistry.ITEMS) {
            if (item.getRebarItem() instanceof AbstractBoiler.Item boiler) {
                RECIPE_TYPE.addRecipe(new BoilerDisplayRecipe(
                        boiler.getKey(),
                        boiler.getStack(),
                        new FluidWithAmount(PylonFluids.WATER, boiler.waterInput),
                        new FluidWithAmount(PylonFluids.STEAM, boiler.steamOutput)
                ));
            }
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(FluidChoice.of(input));
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(result);
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# i # # b # # r #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', FluidButton.of(input))
                .addIngredient('b', ItemButton.of(boiler))
                .addIngredient('r', FluidButton.of(result))
                .build();
    }
}
