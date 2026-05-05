package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public interface HydraulicPurifier extends RebarTickingBlock {
    NamespacedKey HYDRAULIC_PURIFICATION_KEY = pylonKey("hydraulic_purification");

    RecipeType<RebarRecipe> RECIPE_TYPE = new RecipeType<>(HYDRAULIC_PURIFICATION_KEY) {{
        addRecipe(
                new RebarRecipe() {
                    @Override
                    public @NotNull List<@NotNull RecipeInput> getInputs() {
                        return List.of(RecipeInput.of(PylonFluids.DIRTY_HYDRAULIC_FLUID, 1));
                    }

                    @Override
                    public @NotNull List<@NotNull FluidOrItem> getResults() {
                        return List.of(FluidOrItem.of(PylonFluids.HYDRAULIC_FLUID, 1));
                    }

                    @Override
                    public @NotNull Gui display() {
                        return Gui.builder()
                                .setStructure(
                                        "# # # # # # # # #",
                                        "# # # # # # # # #",
                                        "# d # # x # # h #",
                                        "# # # # # # # # #",
                                        "# # # # # # # # #"
                                )
                                .addIngredient('#', GuiItems.backgroundBlack())
                                .addIngredient('d', new FluidButton(PylonFluids.DIRTY_HYDRAULIC_FLUID))
                                .addIngredient('x', new ItemButton(getPurifiers()))
                                .addIngredient('h', new FluidButton(PylonFluids.HYDRAULIC_FLUID))
                                .build();
                    }

                    @Override
                    public @NotNull NamespacedKey getKey() {
                        return HYDRAULIC_PURIFICATION_KEY;
                    }
                }
        );
    }};

    double getPurificationSpeed();
    double getPurificationEfficiency();

    static List<ItemStack> getPurifiers() {
        List<ItemStack> purifiers = new ArrayList<>();
        for (RebarBlockSchema blockSchema : RebarRegistry.BLOCKS.getValues()) {
            var clazz = blockSchema.getBlockClass();
            if (!HydraulicPurifier.class.isAssignableFrom(clazz)) continue;

            RebarItemSchema itemSchema = RebarRegistry.ITEMS.get(blockSchema.getKey());
            if (itemSchema == null) continue; // should never happen

            purifiers.add(itemSchema.createNewItem());
        }

        return purifiers;
    }
}
