package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.WeightedSet;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param input the input item (respects amount)
 * @param results the result items and their corresponding probabilities
 *                (respects item amount) (maximum 9 items)
 * @param cycles the number of full rotations needed to complete the recipe
 * @param particleBlockData the block data to use for the particles shown while grinding
 */
public record GrindstoneRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        @NotNull WeightedSet<ItemStack> results,
        int cycles,
        @NotNull BlockData particleBlockData
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<GrindstoneRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("grindstone")) {
        @Override
        protected @NotNull GrindstoneRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new GrindstoneRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("results", ConfigAdapter.WEIGHTED_SET.from(ConfigAdapter.ITEM_STACK)),
                    section.getOrThrow("cycles", ConfigAdapter.INTEGER),
                    section.getOrThrow("particle-data", ConfigAdapter.BLOCK_DATA)
            );
        }

        @Override
        public void addRecipe(@NonNull GrindstoneRecipe recipe) {
            super.addRecipe(recipe);
            if (recipe.results.size() > 1) {
                IngredientCalculator.addBaseRecipe(recipe);
            }
        }
    };

    public int timeTicks() {
        return cycles * Grindstone.CYCLE_DURATION_TICKS;
    }

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return results.getElements().stream()
                .map(FluidOrItem::of)
                .toList();
    }

    @Override
    public @NotNull Gui display() {
        var gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # c # 1 2 3 #",
                        "# i # g # 4 5 6 #",
                        "# # # # # 7 8 9 #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('g', ItemButton.from(PylonItems.GRINDSTONE))
                .addIngredient('i', ItemButton.from(input))
                .addIngredient('c', GuiItems.progressCyclingItem(cycles * Grindstone.CYCLE_DURATION_TICKS,
                        ItemStackBuilder.of(Material.CLOCK)
                                .name(net.kyori.adventure.text.Component.translatable(
                                        "pylon.guide.recipe.grindstone.time",
                                        RebarArgument.of("time", UnitFormat.SECONDS.format(cycles * Grindstone.CYCLE_DURATION_TICKS / 20))
                                ))
                ));

        float totalWeight = results.stream().map(WeightedSet.Element::weight).reduce(0f, Float::sum);
        int i = 1;
        for (WeightedSet.Element<ItemStack> element : results) {
            ItemStack stack = element.element().clone();
            List<Component> lore = element.element().lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            float normalizedWeight = element.weight() / totalWeight;
            lore.add(Component.empty());
            lore.add(Component.translatable(
                    "pylon.guide.recipe.grindstone.chance",
                    RebarArgument.of(
                            "chance",
                            UnitFormat.PERCENT.format(Math.round(normalizedWeight * 100)).decimalPlaces(2))
            ));
            stack.lore(lore);
            gui.addIngredient((char) ('0' + i), ItemButton.from(stack));
            i++;
        }

        return gui.build();
    }
}