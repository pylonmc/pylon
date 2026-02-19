package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.recipes.*;


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
    }
}
