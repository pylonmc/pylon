package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param input the input item (respects amount)
 * @param result the output item (respects amount)
 * @param particleData the block data to use for particles
 * @param timeTicks the recipe time in ticks
 */
public record PipeBendingRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        @NotNull ItemStack result,
        @NotNull BlockData particleData,
        int timeTicks
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<PipeBendingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("pipe_bending")) {
        @Override
        protected @NotNull PipeBendingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new PipeBendingRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("particle-data", ConfigAdapter.BLOCK_DATA),
                    section.getOrThrow("time-ticks", ConfigAdapter.INTEGER)
            );
        }
    };

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
                        "# # # i b o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', ItemButton.from(input))
                .addIngredient('b', GuiItems.progressCyclingItem(timeTicks, ItemStackBuilder.of(PylonItems.HYDRAULIC_PIPE_BENDER)))
                .addIngredient('o', ItemButton.from(result))
                .build();
    }
}