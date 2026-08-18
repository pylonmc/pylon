package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicPurifier;
import io.github.pylonmc.pylon.recipes.*;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ingredient.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidWithAmount;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;

import net.kyori.adventure.text.Component;
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

        //hardcoded
        initCollimator();
        initPalladiumCondenser();
        initBiorefinery();
        initFermenter();
        initHydraulicFracturingDrill();
        initPumpjack();
    }

    private static void initCollimator() {
        NamespacedKey key = PylonKeys.COLLIMATOR;
        FluidChoice input = FluidChoice.of(PylonFluids.OBSCYRA, ConfigSection.fromSettings(key).getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER));
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
                        .addIngredient('i', FluidButton.of(input.getAmount(), PylonFluids.OBSCYRA))
                        .addIngredient('x', ItemButton.of(PylonItems.COLLIMATOR))
                        .addIngredient('o', ItemButton.of(PylonItems.COHESIVE_UNIT))
                        .build()
        ).register();
    }

    private static void initPalladiumCondenser() {
        NamespacedKey key = PylonKeys.PALLADIUM_CONDENSER;
        ConfigSection setting = ConfigSection.fromSettings(key);

        int totalTicks = setting.getOrThrow("machine-ticks-per-cycle", ConfigAdapter.INTEGER) * setting.getOrThrow("tick-interval", ConfigAdapter.INTEGER) / 20;
        int hydraulicUse = setting.getOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER) * totalTicks;
        int dieselUse = setting.getOrThrow("diesel-per-second", ConfigAdapter.INTEGER) * totalTicks;

        ItemStack dusts = PylonItems.SHIMMER_DUST_2.asQuantity(setting.getOrThrow("shimmer-dust-per-cycle", ConfigAdapter.INTEGER));
        List<FluidOrItemChoice> input = List.of(
                ItemChoice.fuzzy(dusts),
                FluidChoice.of(PylonFluids.BIODIESEL, dieselUse),
                FluidChoice.of(PylonFluids.HYDRAULIC_FLUID, hydraulicUse)
        );

        List<FluidOrItem> output = List.of(
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
                        .addIngredient('x', ItemButton.of(PylonItems.PALLADIUM_CONDENSER))
                        .addIngredient('H', FluidButton.of((double) dieselUse, PylonFluids.BIODIESEL))
                        .addIngredient('d', FluidButton.of((double) hydraulicUse, PylonFluids.HYDRAULIC_FLUID))
                        .addIngredient('s', ItemButton.of(dusts))
                        .addIngredient('p', ItemButton.of(PylonItems.PALLADIUM_DUST))
                        .addIngredient('D', FluidButton.of((double) hydraulicUse, PylonFluids.DIRTY_HYDRAULIC_FLUID))
                        .build()
        ).register();
    }

    private static void initBiorefinery() {
        NamespacedKey key = PylonKeys.BIOREFINERY;
        ConfigSection setting = ConfigSection.fromSettings(key);

        double ethanolPerMbOfBiodiesel = setting.getOrThrow("ethanol-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);
        double plantOilPerMbOfBiodiesel = setting.getOrThrow("plant-oil-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);


        FluidChoice ethanol = FluidChoice.of(PylonFluids.ETHANOL, ethanolPerMbOfBiodiesel);
        FluidChoice plantOil = FluidChoice.of(PylonFluids.PLANT_OIL, plantOilPerMbOfBiodiesel);

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

        ItemChoice input = ItemChoice.fuzzy(ItemTypeWrapper.of(Material.SUGAR_CANE));
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

    private static void initHydraulicFracturingDrill() {
        NamespacedKey key = PylonKeys.HYDRAULIC_FRACTURE;

        ConfigSection config = ConfigSection.fromSettings(PylonKeys.HYDRAULIC_FRACTURING_DRILL);

        FluidWithAmount hydraulicFluid = new FluidWithAmount(
                PylonFluids.HYDRAULIC_FLUID,
                config.getOrThrow("hydraulic-fluid-per-fracture", ConfigAdapter.INTEGER)
        );
        FluidWithAmount steam = new FluidWithAmount(
                PylonFluids.STEAM,
                config.getOrThrow("steam-per-fracture", ConfigAdapter.INTEGER)
        );
        int sandAmount = config.getOrThrow("ticks-to-create-fracture", ConfigAdapter.INTEGER)
                / config.getOrThrow("machine-ticks-per-sand", ConfigAdapter.INTEGER)
                / config.getOrThrow("tick-interval", ConfigAdapter.INTEGER);
        FluidOrItem output = FluidOrItem.of(PylonItems.HYDRAULIC_FRACTURE);

        new SingleRecipe(
                key,
                List.of(
                        FluidChoice.of(hydraulicFluid),
                        FluidChoice.of(steam),
                        ItemChoice.exact(ItemStack.of(Material.SAND))
                ),
                List.of(output),
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# h s a # x # f #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('h', FluidButton.of(hydraulicFluid))
                        .addIngredient('s', FluidButton.of(steam))
                        .addIngredient('a', ItemButton.of(ItemStackBuilder.of(Material.SAND)
                                .name(Component.text(sandAmount + " ").append(new ItemStack(Material.SAND).effectiveName()))
                        ))
                        .addIngredient('x', ItemButton.of(PylonItems.HYDRAULIC_FRACTURING_DRILL))
                        .addIngredient('f', ItemButton.of(PylonItems.HYDRAULIC_FRACTURE))
                        .build()
        ).register();
    }

    private static void initPumpjack() {
        NamespacedKey key = PylonFluids.OIL.getKey();

        ConfigSection config = ConfigSection.fromSettings(PylonKeys.HYDRAULIC_PUMPJACK);

        FluidWithAmount input = new FluidWithAmount(
                PylonFluids.HYDRAULIC_FLUID,
                config.getOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER)
        );

        FluidWithAmount output = new FluidWithAmount(
                PylonFluids.OIL,
                config.getOrThrow("max-oil-per-second", ConfigAdapter.INTEGER)
        );

        new SingleRecipe(
                key,
                FluidChoice.of(input),
                output,
                () -> Gui.builder()
                        .setStructure(
                                "# # # # # # # # #",
                                "# # # # # # # # #",
                                "# # h # x # o # #",
                                "# # # # # # # # #",
                                "# # # # # # # # #"
                        )
                        .addIngredient('#', GuiItems.backgroundBlack())
                        .addIngredient('h', FluidButton.of(input))
                        .addIngredient('x', ItemButton.of(PylonItems.HYDRAULIC_PUMPJACK))
                        .addIngredient('o', FluidButton.of(output))
                        .build()
        ).register();
    }
}
