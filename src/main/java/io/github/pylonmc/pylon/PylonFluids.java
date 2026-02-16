package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.machines.smelting.Slurry;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.recipe.IngredientCalculator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class PylonFluids {

    private PylonFluids() {
        throw new AssertionError("Utility class");
    }

    public static final RebarFluid WATER = new RebarFluid(
            pylonKey("water"),
            Material.BLUE_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        WATER.register();
        IngredientCalculator.addBaseIngredient(WATER);
    }

    public static final RebarFluid LAVA = new RebarFluid(
            pylonKey("lava"),
            Material.ORANGE_CONCRETE
    ).addTag(FluidTemperature.HOT);
    static {
        LAVA.register();
        IngredientCalculator.addBaseIngredient(LAVA);
    }

    public static final RebarFluid PLANT_OIL = new RebarFluid(
            pylonKey("plant_oil"),
            Material.YELLOW_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        PLANT_OIL.register();
        IngredientCalculator.addBaseIngredient(PLANT_OIL);
    }

    public static final RebarFluid HYDRAULIC_FLUID = new RebarFluid(
            pylonKey("hydraulic_fluid"),
            Material.BLUE_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        HYDRAULIC_FLUID.register();
    }


    public static final RebarFluid DIRTY_HYDRAULIC_FLUID = new RebarFluid(
            pylonKey("dirty_hydraulic_fluid"),
            Material.BROWN_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        DIRTY_HYDRAULIC_FLUID.register();
    }

    public static final RebarFluid REFLECTOR_FLUID = new RebarFluid(
            pylonKey("reflector_fluid"),
            Material.WHITE_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        REFLECTOR_FLUID.register();
    }

    public static final RebarFluid SUGARCANE = new RebarFluid(
            pylonKey("sugarcane"),
            Material.LIME_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        SUGARCANE.register();
        RebarGuide.hideFluid(SUGARCANE.getKey());
    }

    public static final RebarFluid ETHANOL = new RebarFluid(
            pylonKey("ethanol"),
            Material.LIGHT_GRAY_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        ETHANOL.register();
    }

    public static final RebarFluid BIODIESEL = new RebarFluid(
            pylonKey("biodiesel"),
            Material.YELLOW_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        BIODIESEL.register();
    }

    public static final RebarFluid OBSCYRA = new RebarFluid(
            pylonKey("obscyra"),
            Material.BLACK_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        OBSCYRA.register();
    }

    public static final RebarFluid SULFUR = new RebarFluid(
            pylonKey("sulfur"),
            Material.YELLOW_TERRACOTTA
    ).addTag(FluidTemperature.HOT);
    static {
        SULFUR.register();
    }

    public static final RebarFluid MERCURY = new RebarFluid(
            pylonKey("mercury"),
            Material.CYAN_TERRACOTTA
    ).addTag(FluidTemperature.NORMAL);
    static {
        MERCURY.register();
    }

    public static final RebarFluid COPPER = new RebarFluid(
            pylonKey("copper"),
            Material.TERRACOTTA
    ).addTag(FluidTemperature.HOT);
    static {
        COPPER.register();
    }

    public static final RebarFluid IRON = new RebarFluid(
            pylonKey("iron"),
            Material.RED_TERRACOTTA
    ).addTag(FluidTemperature.HOT);
    static {
        IRON.register();
    }

    public static final RebarFluid GOLD = new RebarFluid(
            pylonKey("gold"),
            Material.YELLOW_CONCRETE
    ).addTag(FluidTemperature.HOT);
    static {
        GOLD.register();
    }

    public static final RebarFluid TIN = new RebarFluid(
            pylonKey("tin"),
            Material.GREEN_TERRACOTTA
    ).addTag(FluidTemperature.HOT);
    static {
        TIN.register();
    }

    public static final RebarFluid BRONZE = new RebarFluid(
            pylonKey("bronze"),
            Material.BROWN_CONCRETE
    ).addTag(FluidTemperature.HOT);
    static {
        BRONZE.register();
    }

    public static final RebarFluid STEEL = new RebarFluid(
            pylonKey("steel"),
            Material.GRAY_CONCRETE
    ).addTag(FluidTemperature.HOT);
    static {
        STEEL.register();
    }

    public static final RebarFluid PALLADIUM = new RebarFluid(
            pylonKey("palladium"),
            Material.LIGHT_BLUE_TERRACOTTA
    ).addTag(FluidTemperature.HOT);
    static {
        PALLADIUM.register();
    }

    public static final RebarFluid SLURRY = new RebarFluid(
            pylonKey("slurry"),
            Material.LIGHT_GRAY_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        SLURRY.register();
    }

    public static final RebarFluid COAL_SLURRY = new Slurry(
            pylonKey("slurry_coal"),
            PylonItems.COAL_DUST
    ).addTag(FluidTemperature.NORMAL);
    static {
        COAL_SLURRY.register();
    }

    public static final RebarFluid CARBON_SLURRY = new Slurry(
            pylonKey("slurry_carbon"),
            PylonItems.CARBON
    ).addTag(FluidTemperature.NORMAL);
    static {
        CARBON_SLURRY.register();
    }

    public static final RebarFluid RAW_COPPER_SLURRY = new Slurry(
            pylonKey("slurry_raw_copper"),
            PylonItems.CRUSHED_RAW_COPPER
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_COPPER_SLURRY.register();
    }

    public static final RebarFluid RAW_IRON_SLURRY = new Slurry(
            pylonKey("slurry_raw_iron"),
            PylonItems.CRUSHED_RAW_IRON
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_IRON_SLURRY.register();
    }

    public static final RebarFluid RAW_GOLD_SLURRY = new Slurry(
            pylonKey("slurry_raw_gold"),
            PylonItems.CRUSHED_RAW_GOLD
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_GOLD_SLURRY.register();
    }

    public static final RebarFluid REDSTONE_SLURRY = new Slurry(
            pylonKey("slurry_redstone"),
            new ItemStack(Material.REDSTONE)
    ).addTag(FluidTemperature.NORMAL);
    static {
        REDSTONE_SLURRY.register();
    }

    public static final RebarFluid RAW_TIN_SLURRY = new Slurry(
            pylonKey("slurry_raw_tin"),
            PylonItems.CRUSHED_RAW_TIN
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_TIN_SLURRY.register();
    }

    public static final RebarFluid SPONGE_IRON_SLURRY = new Slurry(
            pylonKey("slurry_sponge_iron"),
            PylonItems.SPONGE_IRON
    ).addTag(FluidTemperature.NORMAL);
    static {
        SPONGE_IRON_SLURRY.register();
    }

    /**
     * Calling this function will run the static blocks
     */
    static void initialize() {
    }
}
