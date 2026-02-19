package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public record BloomeryDisplayRecipe(
        NamespacedKey key,
        ItemStack input,
        ItemStack result
) implements RebarRecipe {

    public static final RecipeType<BloomeryDisplayRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("bloomery_display")) {
        @Override
        protected @NotNull BloomeryDisplayRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new BloomeryDisplayRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(RecipeInput.of(input));
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # i # b # r # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', new ItemButton(input))
                .addIngredient('b', new ItemButton(PylonItems.BLOOMERY))
                .addIngredient('r', new ItemButton(result))
                .build();
    }
}