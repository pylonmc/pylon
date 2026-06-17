package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicPurifier;
import io.github.pylonmc.pylon.recipes.*;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidWithAmount;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.gui.Gui;


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
        KilnRecipe.RECIPE_TYPE.register();
        StrainingRecipe.RECIPE_TYPE.register();
        TableSawRecipe.RECIPE_TYPE.register();
        SiloConverterRecipe.RECIPE_TYPE.register();
        HydraulicPurifier.RECIPE_TYPE.register();
        CrudeAlloyFurnaceRecipe.RECIPE_TYPE.register();
        FormingRecipe.RECIPE_TYPE.register();
        GasTurbineRecipe.RECIPE_TYPE.register();
        HeatExchangerRecipe.RECIPE_TYPE.register();

        //hardcoded
        initCollimator();
        initBiorefinery();
        initFermenter();
        initCombustionTower();
    }

    private static void initCollimator() {
        NamespacedKey key = PylonKeys.COLLIMATOR;
        RecipeInput.Fluid input = RecipeInput.of(PylonFluids.OBSCYRA, ConfigSection.fromSettings(key).getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER));
        FluidOrItem output = FluidOrItem.of(PylonItems.COHESIVE_UNIT);
        new SingleRecipe(
                key,
                input,
                output,
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# i # # x # # o #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('i', FluidButton.of(input.amountMillibuckets(), PylonFluids.OBSCYRA))
                        .addIngredient('x', ItemButton.of(PylonItems.COLLIMATOR))
                        .addIngredient('o', ItemButton.of(PylonItems.COHESIVE_UNIT))
                        .build()
        ).register();
    }

    private static void initBiorefinery() {
        NamespacedKey key = PylonKeys.BIOREFINERY;
        ConfigSection setting = ConfigSection.fromSettings(key);

        double ethanolPerMbOfBiodiesel = setting.getOrThrow("ethanol-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);
        double plantOilPerMbOfBiodiesel = setting.getOrThrow("plant-oil-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);


        RecipeInput.Fluid ethanol = RecipeInput.of(PylonFluids.ETHANOL, ethanolPerMbOfBiodiesel);
        RecipeInput.Fluid plantOil = RecipeInput.of(PylonFluids.PLANT_OIL, plantOilPerMbOfBiodiesel);

        FluidOrItem output = FluidOrItem.of(PylonFluids.BIODIESEL, 1);

        new SingleRecipe(
                key,
                List.of(ethanol, plantOil),
                List.of(output),
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# p # # # # # # #",
                                "# # # # x # # o #",
                                "# e # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('x', ItemButton.of(PylonItems.BIOREFINERY))
                        .addIngredient('o', FluidButton.of(1.0, PylonFluids.BIODIESEL))
                        .addIngredient('p', FluidButton.of(plantOilPerMbOfBiodiesel, PylonFluids.PLANT_OIL))
                        .addIngredient('e', FluidButton.of(ethanolPerMbOfBiodiesel, PylonFluids.ETHANOL))
                        .build()
        ).register();
    }

    private static void initFermenter() {
        NamespacedKey key = PylonKeys.FERMENTER;
        ConfigSection setting = ConfigSection.fromSettings(key);

        double ethanolPerSugarcane = setting.getOrThrow("ethanol-per-sugarcane", ConfigAdapter.DOUBLE);

        RecipeInput.Item input = RecipeInput.of(ItemStack.of(Material.SUGAR_CANE));
        FluidOrItem output = FluidOrItem.of(PylonFluids.ETHANOL, ethanolPerSugarcane);

        new SingleRecipe(
                key,
                input,
                output,
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# i # # x # # o #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('i', ItemButton.of(ItemStack.of(Material.SUGAR_CANE)))
                        .addIngredient('x', ItemButton.of(PylonItems.FERMENTER))
                        .addIngredient('o', FluidButton.of(ethanolPerSugarcane, PylonFluids.ETHANOL))
                        .build()
        ).register();
    }

    private static void initCombustionTower() {
        NamespacedKey key = PylonKeys.COMBUSTION_TOWER;
        ConfigSection setting = ConfigSection.fromSettings(key);

        double dieselUsage = setting.getOrThrow("diesel-usage", ConfigAdapter.DOUBLE);
        double exhaustProduction = setting.getOrThrow("exhaust-production", ConfigAdapter.DOUBLE);

        RecipeInput.Fluid input = RecipeInput.of(PylonFluids.BIODIESEL, dieselUsage);
        FluidWithAmount output = new FluidWithAmount(PylonFluids.VERY_HOT_EXHAUST, exhaustProduction);

        new SingleRecipe(
                key,
                input,
                output.asFluidOrItem(),
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# d # # x # # e #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('d', FluidButton.of(input))
                        .addIngredient('x', ItemButton.of(PylonItems.COMBUSTION_TOWER))
                        .addIngredient('e', FluidButton.of(output))
                        .build()
        ).register();
    }
}
