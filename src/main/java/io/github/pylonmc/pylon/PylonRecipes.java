package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.recipes.*;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;


public class PylonRecipes {

    private PylonRecipes() {
        throw new AssertionError("Utility class");
    }

    public static void initialize() {
        AssemblingRecipe.RECIPE_TYPE.register();
        CastingRecipe.RECIPE_TYPE.register();
        DrillingDisplayRecipe.RECIPE_TYPE.register();
        ForgingDisplayRecipe.RECIPE_TYPE.register();
        BloomeryDisplayRecipe.RECIPE_TYPE.register();
        GrindstoneRecipe.RECIPE_TYPE.register();
        HammerRecipe.RECIPE_TYPE.register();
        ShimmerAltarRecipe.RECIPE_TYPE.register();
        MeltingRecipe.RECIPE_TYPE.register();
        MixingPotRecipe.RECIPE_TYPE.register();
        CrucibleRecipe.RECIPE_TYPE.register();
        MoldingRecipe.RECIPE_TYPE.register();
        PipeBendingRecipe.RECIPE_TYPE.register();
        PressRecipe.RECIPE_TYPE.register();
        SmelteryRecipe.RECIPE_TYPE.register();
        PitKilnRecipe.RECIPE_TYPE.register();
        StrainingRecipe.RECIPE_TYPE.register();
        TableSawRecipe.RECIPE_TYPE.register();

        //hardcoded
        initCollimator();
    }

    private static void initCollimator() {
        NamespacedKey key = PylonKeys.COLLIMATOR;
        RecipeInput.Fluid input = RecipeInput.of(PylonFluids.OBSCYRA, Settings.get(key).getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER));
        FluidOrItem output = FluidOrItem.of(PylonItems.COHESIVE_UNIT);
        new SingleRecipe(
                key,
                input,
                output,
                Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# i # # x # # o #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('i', new FluidButton(input.amountMillibuckets(), PylonFluids.OBSCYRA))
                        .addIngredient('x', ItemButton.from(PylonItems.COLLIMATOR))
                        .addIngredient('o', ItemButton.from(PylonItems.COHESIVE_UNIT))
                        ::build
        ).register();
    }
}
