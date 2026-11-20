package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * Represents using a fireproof rune as a catalyst to create an upgraded item.
 * It happens when the player drops the fireproof rune on an item.
 *
 * @param input  the item to be upgraded
 * @param result the upgraded item
 * @author balugaq
 */
public record FireproofRuneRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemChoice input,
        @NotNull ItemStack result
) implements RebarRecipe {
    public static final ConfigurableRecipeType<FireproofRuneRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("fireproof_rune")) {
        @Override
        protected @NotNull FireproofRuneRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new FireproofRuneRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.ITEM_CHOICE),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # # i f o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('f', ItemButton.of(PylonItems.FIREPROOF_RUNE))
                .addIngredient('i', ItemButton.of(input))
                .addIngredient('o', ItemButton.of(result))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }
}
