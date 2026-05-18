package io.github.pylonmc.pylon;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.pylonmc.pylon.api.MeltingPoint;
import io.github.pylonmc.pylon.content.machines.smelting.Slurry;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.recipe.IngredientCalculator;

public final class PylonFluids {

    private PylonFluids() {
        throw new AssertionError("Utility class");
    }

    public static final RebarFluid WATER = new RebarFluid(
            pylonKey("water"),
            TextColor.fromHexString("#4040ff"),
            Material.BLUE_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        WATER.register();
        IngredientCalculator.addBaseIngredient(WATER);
    }

    public static final RebarFluid LAVA = new RebarFluid(
            pylonKey("lava"),
            TextColor.fromHexString("#fc4e03"),
            Material.ORANGE_CONCRETE
    ).addTag(FluidTemperature.HOT);
    static {
        LAVA.register();
        IngredientCalculator.addBaseIngredient(LAVA);
    }

    public static final RebarFluid PLANT_OIL = new RebarFluid(
            pylonKey("plant_oil"),
            TextColor.fromHexString("#c4b352"),
            Material.YELLOW_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        PLANT_OIL.register();
        IngredientCalculator.addBaseIngredient(PLANT_OIL);
    }

    public static final RebarFluid HYDRAULIC_FLUID = new RebarFluid(
            pylonKey("hydraulic_fluid"),
            TextColor.fromHexString("#212d99"),
            Material.BLUE_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        HYDRAULIC_FLUID.register();
    }


    public static final RebarFluid DIRTY_HYDRAULIC_FLUID = new RebarFluid(
            pylonKey("dirty_hydraulic_fluid"),
            TextColor.fromHexString("#48459b"),
            Material.BROWN_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        DIRTY_HYDRAULIC_FLUID.register();
    }

    public static final RebarFluid REFLECTOR_FLUID = new RebarFluid(
            pylonKey("reflector_fluid"),
            TextColor.fromHexString("#d1c9ab"),
            Material.WHITE_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        REFLECTOR_FLUID.register();
    }

    public static final RebarFluid SUGARCANE = new RebarFluid(
            pylonKey("sugarcane"),
            TextColor.fromHexString("#a3ed2d"),
            Material.LIME_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        SUGARCANE.register();
        RebarGuide.hideFluid(SUGARCANE.getKey());
    }

    public static final RebarFluid ETHANOL = new RebarFluid(
            pylonKey("ethanol"),
            TextColor.fromHexString("#d8d8d8"),
            Material.LIGHT_GRAY_CONCRETE_POWDER
    ).addTag(FluidTemperature.NORMAL);
    static {
        ETHANOL.register();
    }

    public static final RebarFluid BIODIESEL = new RebarFluid(
            pylonKey("biodiesel"),
            TextColor.fromHexString("#eaa627"),
            Material.YELLOW_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        BIODIESEL.register();
    }

    public static final RebarFluid OBSCYRA = new RebarFluid(
            pylonKey("obscyra"),
            TextColor.fromHexString("#5c4a66"),
            Material.BLACK_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        OBSCYRA.register();
    }

    public static final RebarFluid SULFUR = new RebarFluid(
            pylonKey("sulfur"),
            TextColor.fromHexString("#fff387"),
            Material.YELLOW_TERRACOTTA
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(112.8));
    static {
        SULFUR.register();
    }

    public static final RebarFluid MERCURY = new RebarFluid(
            pylonKey("mercury"),
            TextColor.fromHexString("#dedede"),
            Material.CYAN_TERRACOTTA
    ).addTag(FluidTemperature.NORMAL);
    static {
        MERCURY.register();
    }

    public static final RebarFluid TIN = new RebarFluid(
            pylonKey("tin"),
            TextColor.fromHexString("#62b98e"),
            Material.GREEN_TERRACOTTA
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(231.9));
    static {
        TIN.register();
    }

    public static final RebarFluid COPPER = new RebarFluid(
            pylonKey("copper"),
            TextColor.fromHexString("#e16f22"),
            Material.TERRACOTTA
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(1083));
    static {
        COPPER.register();
    }

    public static final RebarFluid IRON = new RebarFluid(
            pylonKey("iron"),
            TextColor.fromHexString("#e5d7b9"),
            Material.RED_TERRACOTTA
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(1538));
    static {
        IRON.register();
    }

    public static final RebarFluid BRONZE = new RebarFluid(
            pylonKey("bronze"),
            TextColor.fromHexString("#d4a796"),
            Material.BROWN_CONCRETE
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(950));
    static {
        BRONZE.register();
    }

    public static final RebarFluid GOLD = new RebarFluid(
            pylonKey("gold"),
            TextColor.fromHexString("#debc57"),
            Material.YELLOW_CONCRETE
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(1064));
    static {
        GOLD.register();
    }

    public static final RebarFluid STEEL = new RebarFluid(
            pylonKey("steel"),
            TextColor.fromHexString("#848484"),
            Material.GRAY_CONCRETE
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(1410));
    static {
        STEEL.register();
    }

    public static final RebarFluid PALLADIUM = new RebarFluid(
            pylonKey("palladium"),
            TextColor.fromHexString("#b1d4de"),
            Material.LIGHT_BLUE_TERRACOTTA
    ).addTag(FluidTemperature.HOT).addTag(new MeltingPoint(1555));
    static {
        PALLADIUM.register();
    }

    public static final RebarFluid SLURRY = new RebarFluid(
            pylonKey("slurry"),
            TextColor.fromHexString("#bcbcbc"),
            Material.LIGHT_GRAY_CONCRETE
    ).addTag(FluidTemperature.NORMAL);
    static {
        SLURRY.register();
    }

    public static final RebarFluid COAL_SLURRY = new Slurry(
            pylonKey("slurry_coal"),
            TextColor.fromHexString("#343434"),
            PylonItems.COAL_DUST
    ).addTag(FluidTemperature.NORMAL);
    static {
        COAL_SLURRY.register();
    }

    public static final RebarFluid CARBON_SLURRY = new Slurry(
            pylonKey("slurry_carbon"),
            TextColor.fromHexString("#464646"),
            PylonItems.CARBON
    ).addTag(FluidTemperature.NORMAL);
    static {
        CARBON_SLURRY.register();
    }

    public static final RebarFluid RAW_TIN_SLURRY = new Slurry(
            pylonKey("slurry_raw_tin"),
            TextColor.fromHexString("#387154"),
            PylonItems.CRUSHED_RAW_TIN
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_TIN_SLURRY.register();
    }

    public static final RebarFluid RAW_COPPER_SLURRY = new Slurry(
            pylonKey("slurry_raw_copper"),
            TextColor.fromHexString("#8d4717"),
            PylonItems.CRUSHED_RAW_COPPER
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_COPPER_SLURRY.register();
    }

    public static final RebarFluid RAW_IRON_SLURRY = new Slurry(
            pylonKey("slurry_raw_iron"),
            TextColor.fromHexString("#887f6d"),
            PylonItems.CRUSHED_RAW_IRON
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_IRON_SLURRY.register();
    }

    public static final RebarFluid RAW_GOLD_SLURRY = new Slurry(
            pylonKey("slurry_raw_gold"),
            TextColor.fromHexString("#a0873f"),
            PylonItems.CRUSHED_RAW_GOLD
    ).addTag(FluidTemperature.NORMAL);
    static {
        RAW_GOLD_SLURRY.register();
    }

    public static final RebarFluid REDSTONE_SLURRY = new Slurry(
            pylonKey("slurry_redstone"),
            TextColor.fromHexString("#841f1e"),
            new ItemStack(Material.REDSTONE)
    ).addTag(FluidTemperature.NORMAL);
    static {
        REDSTONE_SLURRY.register();
    }

    public static final RebarFluid SPONGE_IRON_SLURRY = new Slurry(
            pylonKey("slurry_sponge_iron"),
            TextColor.fromHexString("#847975"),
            PylonItems.SPONGE_IRON,
            500
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
