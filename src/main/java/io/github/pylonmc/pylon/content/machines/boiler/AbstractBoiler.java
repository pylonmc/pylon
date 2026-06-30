package io.github.pylonmc.pylon.content.machines.boiler;

import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;


public abstract class AbstractBoiler extends RebarBlock implements SimpleRebarMultiblock, TickingRebarBlock, DirectionalRebarBlock {

    public static final double WATER_BOILING_POINT = 100;

    public static final NamespacedKey TEMPERATURE = PylonUtils.pylonKey("temperature");

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int waterInput = getSettingOrThrow("water-input", ConfigAdapter.INTEGER);
    public final int steamOutput = getSettingOrThrow("steam-output", ConfigAdapter.INTEGER);
    public final double minTemperature = getSettingOrThrow("min-temperature", ConfigAdapter.DOUBLE);
    public final double maxTemperature = getSettingOrThrow("max-temperature", ConfigAdapter.DOUBLE);
    public final double heatingSpeed = getSettingOrThrow("heating-speed", ConfigAdapter.DOUBLE);
    public final double minCoolingSpeed = getSettingOrThrow("min-cooling-speed", ConfigAdapter.DOUBLE);
    public final double maxCoolingSpeed = getSettingOrThrow("max-cooling-speed", ConfigAdapter.DOUBLE);

    public double temperature;
    public double steamOutputLastUpdate;
    public double fuelBurntLastUpdate;

    protected AbstractBoiler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);
        temperature = minTemperature;
    }

    protected AbstractBoiler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        temperature = pdc.get(TEMPERATURE, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(TEMPERATURE, RebarSerializers.DOUBLE, temperature);
    }

    protected double boilingTemperatureProportion() {
        return Math.max(0.0, (temperature - WATER_BOILING_POINT) / (maxTemperature - WATER_BOILING_POINT));
    }

    protected @NotNull Color fireColor() {
        double temperatureProportion = (temperature - minTemperature) / (maxTemperature - minTemperature);
        return Color.fromRGB(
                (int) Math.min(255, 400 * temperatureProportion),
                (int) (80 * temperatureProportion),
                0
        );
    }

    protected void update(
            double minFuelConsumption,
            double maxFuelConsumption,
            Vector3i waterInputHatchPosition,
            Vector3i steamOutputHatchPosition
    ) {
        double deltaTime = getTickInterval() / 20.0;
        fuelBurntLastUpdate = 0.0;

        // Background cooling
        temperature -= minCoolingSpeed * deltaTime;
        temperature = Math.max(minTemperature, temperature);

        // Heat to try and counteract background cooling
        double maintainTemperatureFuel = minFuelConsumption * deltaTime;
        double maintainTemperatureProportion = tryConsumeFuel(maintainTemperatureFuel);
        fuelBurntLastUpdate += maintainTemperatureProportion * maintainTemperatureFuel;
        temperature += maintainTemperatureProportion * minCoolingSpeed * deltaTime;

        // Heat to try and reach max temperature
        double boilingTemperatureProportion = boilingTemperatureProportion();
        double heatToMaxTemperatureFuel = (1.0 - boilingTemperatureProportion) * (maxFuelConsumption - minFuelConsumption) * deltaTime;
        double heatToMaxTemperatureProportion = tryConsumeFuel(heatToMaxTemperatureFuel);
        fuelBurntLastUpdate += heatToMaxTemperatureProportion * heatToMaxTemperatureFuel;
        temperature += heatToMaxTemperatureProportion * heatingSpeed * (1.0 - boilingTemperatureProportion) * deltaTime;

        // K E T T L E
        FluidInputHatch waterHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, waterInputHatchPosition);
        FluidOutputHatch steamHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, steamOutputHatchPosition);

        double maxWaterProportion = waterHatch.getFluidAmount() / (waterInput * deltaTime);
        double maxSteamProportion = steamHatch.getFluidSpaceRemaining() / (steamOutput * deltaTime);
        double maxSteamConversionProportion = Math.min(boilingTemperatureProportion, Math.min(maxWaterProportion, maxSteamProportion));

        double waterAmount = maxSteamConversionProportion * waterInput * deltaTime;
        double steamAmount = maxSteamConversionProportion * steamOutput * deltaTime;

        waterHatch.removeFluid(waterAmount);
        steamHatch.addFluid(steamAmount);

        steamOutputLastUpdate = steamAmount;

        // Consume fuel proportional to how much steam we converted
        // If not enough fuel, reduce the temperature
        double steamConversionFuel = maxSteamConversionProportion * (maxFuelConsumption - minFuelConsumption) * deltaTime;
        double steamConversionFuelProportion = tryConsumeFuel(steamConversionFuel);
        fuelBurntLastUpdate += steamConversionFuelProportion * steamConversionFuel;

        temperature -= (1.0 - steamConversionFuelProportion) * (maxCoolingSpeed - minCoolingSpeed) * deltaTime;
    }

    /**
     * returns the proportion of [fuelToConsumeSeconds] which could actually be consumed
     */
    abstract double tryConsumeFuel(double fuelToConsumeSeconds);
}
