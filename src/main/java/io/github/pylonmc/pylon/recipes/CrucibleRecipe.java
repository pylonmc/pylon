package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.simple.Crucible;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record CrucibleRecipe(
    @NotNull NamespacedKey key,
    @NotNull RecipeInput.Item input,
    @NotNull FluidOrItem.Fluid output
) implements RebarRecipe {

    private static Set<NamespacedKey> HEATED_BLOCKS = null;
    private static List<ItemStack> HEAT_SOURCES = null;

    public static final RecipeType<CrucibleRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("crucible")) {
        @Override
        protected @NotNull CrucibleRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            FluidOrItem output = section.getOrThrow("output", ConfigAdapter.FLUID_OR_ITEM);
            if (!(output instanceof FluidOrItem.Fluid fluidOutput)) {
                throw new IllegalArgumentException(key + ": In crucible recipe output must be a fluid.");
            }

            return new CrucibleRecipe(
                key,
                new RecipeInput.Item(section.getOrThrow("input-item", ConfigAdapter.RECIPE_INPUT_ITEM).getItems(), 1),
                fluidOutput
            );
        }
    };

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(output);
    }

    public static Set<NamespacedKey> getHeatedBlocks() {
        if (HEATED_BLOCKS == null) {
            HEATED_BLOCKS = new HashSet<>();
            for (Material material : Crucible.VANILLA_BLOCK_HEAT_MAP.keySet())  {
                HEATED_BLOCKS.add(material.getKey());
            }

            for (RebarBlockSchema schema : RebarRegistry.BLOCKS) {
                if (Crucible.HeatedBlock.class.isAssignableFrom(schema.getBlockClass())) {
                    HEATED_BLOCKS.add(schema.getKey());
                }
            }
        }

        return HEATED_BLOCKS;
    }


    public static List<ItemStack> getHeatSources() {
        if (HEAT_SOURCES == null) {
            HEAT_SOURCES = new ArrayList<>();
            for (NamespacedKey key : getHeatedBlocks()) {
                HEAT_SOURCES.add(PylonUtils.itemFromKey(key));
            }
        }

        return HEAT_SOURCES;
    }

    public static boolean isValid(ItemStack item) {
        for(var entry : CrucibleRecipe.RECIPE_TYPE.getRecipes()) {
            if (entry.matches(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull Gui display() {

        return Gui.builder()
            .setStructure(
                "# # # # # # # # #",
                "# # # # i # # # #",
                "# # # # m # # o #",
                "# # # # h # # # #",
                "# # # # # # # # #"
            )
            .addIngredient('#', GuiItems.backgroundBlack())
            .addIngredient('i', ItemButton.of(input))
            .addIngredient('m', ItemButton.of(PylonItems.CRUCIBLE))
            .addIngredient('h', new ItemButton(getHeatSources()))
            .addIngredient('o', new FluidButton(output.amountMillibuckets(), output.fluid())
        ).build();
    }

    public boolean matches(ItemStack inputItem) {
        return input.matches(inputItem);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
