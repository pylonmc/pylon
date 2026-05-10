package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicPurifier;
import io.github.pylonmc.pylon.recipes.*;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
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
        PitKilnRecipe.RECIPE_TYPE.register();
        StrainingRecipe.RECIPE_TYPE.register();
        TableSawRecipe.RECIPE_TYPE.register();
        SiloConverterRecipe.RECIPE_TYPE.register();
        HydraulicPurifier.RECIPE_TYPE.register();
        FormingRecipe.RECIPE_TYPE.register();

        //hardcoded
        initCollimator();
        initPalladiumCondenser();
        initBiorefinery();
        initFermenter();
    }

    private static void initCollimator() {
        NamespacedKey key = PylonKeys.COLLIMATOR;
        RecipeInput.Fluid input = RecipeInput.of(PylonFluids.OBSCYRA, Settings.get(key).getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER));
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
                        .addIngredient('i', new FluidButton(input.amountMillibuckets(), PylonFluids.OBSCYRA))
                        .addIngredient('x', ItemButton.from(PylonItems.COLLIMATOR))
                        .addIngredient('o', ItemButton.from(PylonItems.COHESIVE_UNIT))
                        .build()
        ).register();
    }

    private static void initPalladiumCondenser() {
        NamespacedKey key = PylonKeys.PALLADIUM_CONDENSER;
        Config setting = Settings.get(key);

        int totalTicks = setting.getOrThrow("machine-ticks-per-cycle", ConfigAdapter.INTEGER) * setting.getOrThrow("tick-interval", ConfigAdapter.INTEGER) / 20;
        int hydraulicUse = setting.getOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER) * totalTicks;
        int dieselUse = setting.getOrThrow("diesel-per-second", ConfigAdapter.INTEGER) * totalTicks;

        ItemStack dusts = PylonItems.SHIMMER_DUST_2.asQuantity(setting.getOrThrow("shimmer-dust-per-cycle", ConfigAdapter.INTEGER));
        var input = List.of(
                RecipeInput.of(dusts),
                RecipeInput.of(PylonFluids.BIODIESEL, dieselUse),
                RecipeInput.of(PylonFluids.HYDRAULIC_FLUID, hydraulicUse)
        );

        var output = List.of(
                FluidOrItem.of(PylonItems.PALLADIUM_DUST),
                FluidOrItem.of(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicUse)
        );

        new SingleRecipe(
                key,
                input,
                output,
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# H # # # # # p #",
                                "# d # # x # # # #",
                                "# s # # # # # D #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('x', ItemButton.from(PylonItems.PALLADIUM_CONDENSER))
                        .addIngredient('H', new FluidButton((double) dieselUse, PylonFluids.BIODIESEL))
                        .addIngredient('d', new FluidButton((double) hydraulicUse, PylonFluids.HYDRAULIC_FLUID))
                        .addIngredient('s', ItemButton.from(dusts))
                        .addIngredient('p', ItemButton.from(PylonItems.PALLADIUM_DUST))
                        .addIngredient('D', new FluidButton((double) hydraulicUse, PylonFluids.DIRTY_HYDRAULIC_FLUID))
                        .build()
        ).register();
    }

    private static void initBiorefinery() {
        NamespacedKey key = PylonKeys.BIOREFINERY;
        Config setting = Settings.get(key);

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
                        .addIngredient('x', ItemButton.from(PylonItems.BIOREFINERY))
                        .addIngredient('o', new FluidButton(1.0, PylonFluids.BIODIESEL))
                        .addIngredient('p', new FluidButton(plantOilPerMbOfBiodiesel, PylonFluids.PLANT_OIL))
                        .addIngredient('e', new FluidButton(ethanolPerMbOfBiodiesel, PylonFluids.ETHANOL))
                        .build()
        ).register();
    }

    private static void initFermenter() {
        NamespacedKey key = PylonKeys.FERMENTER;
        Config setting = Settings.get(key);

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
                        .addIngredient('i', ItemButton.from(ItemStack.of(Material.SUGAR_CANE)))
                        .addIngredient('x', ItemButton.from(PylonItems.FERMENTER))
                        .addIngredient('o', new FluidButton(ethanolPerSugarcane, PylonFluids.ETHANOL))
                        .build()
        ).register();
    }
}
