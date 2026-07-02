package io.github.pylonmc.pylon.content.machines.boiler;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.api.FlammableTag;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public abstract class AbstractFluidFuelBoiler extends AbstractBoiler {

    public static class Item extends RebarItem {

        public final int waterInput = getSettingOrThrow("water-input", ConfigAdapter.INTEGER);
        public final int steamOutput = getSettingOrThrow("steam-output", ConfigAdapter.INTEGER);
        public final double minFuelConsumption = getSettingOrThrow("min-fuel-consumption", ConfigAdapter.DOUBLE);
        public final double maxFuelConsumption = getSettingOrThrow("max-fuel-consumption", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("water-input", UnitFormat.MILLIBUCKETS_PER_SECOND.format(waterInput)),
                    RebarArgument.of("steam-output", UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamOutput)),
                    RebarArgument.of("min-fuel-consumption", Math.round(100 * minFuelConsumption)),
                    RebarArgument.of("max-fuel-consumption", UnitFormat.PERCENT.format(100 * maxFuelConsumption).decimalPlaces(0))
            );
        }
    }

    public static final Random random = new Random();

    @SuppressWarnings("unused")
    protected AbstractFluidFuelBoiler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    protected AbstractFluidFuelBoiler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onMultiblockFormed() {
        super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, waterInputPosition())
                .setAllowedFluid(PylonFluids.WATER);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, steamOutputPosition())
                .setFluidType(PylonFluids.STEAM);
        getMultiblockComponentOrThrow(FluidInputHatch.class, fuelInputPosition())
                .setAllowedFluids(getFlammableFluids());
    }

    @Override
    double tryConsumeFuel(double fuelToConsumeSeconds) {
        if (fuelToConsumeSeconds == 0) {
            return 1.0;
        }

        double fuelLeft = getFuelLeft();
        double actualFuelConsumedSeconds = Math.min(fuelLeft, fuelToConsumeSeconds);

        FluidInputHatch fuelInput = getMultiblockComponentOrThrow(FluidInputHatch.class, fuelInputPosition());
        if (fuelLeft != 0.0) {
            Preconditions.checkState(fuelInput.getFluidType() != null);
            Preconditions.checkState(fuelInput.getFluidType().hasTag(FlammableTag.class));
            double secondsPerMillibucket = fuelInput.getFluidType().getTag(FlammableTag.class).secondsPerBucket() / 1000.0;
            fuelInput.removeFluid(actualFuelConsumedSeconds / secondsPerMillibucket);
        }

        fuelLeft -= actualFuelConsumedSeconds;
        return actualFuelConsumedSeconds / fuelToConsumeSeconds;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        if (!isFormedAndFullyLoaded()) {
            return WailaDisplay.of(this, player);
        }

        double fuelBurnRate = fuelBurntLastUpdate / (getTickInterval() / 20.0);

        WailaDisplay display = WailaDisplay.of(this, player);
        display.add(new ProgressBar()
                .proportion(temperature / maxTemperature)
                .barColor(fireColor())
                .bars(40)
                .suffix(Component.text(" ").append(UnitFormat.CELSIUS.format(temperature).decimalPlaces(1)))
        );
        display.add(UnitFormat.PERCENT.format(100 * fuelBurnRate).decimalPlaces(2));
        display.add(UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamOutputLastUpdate).decimalPlaces(2).asComponent()
                .append(Component.text(" "))
                .append(PylonFluids.STEAM.getName())
        );
        double maxFuelLeft = getMaxFuelLeft();
        if (maxFuelLeft != 0) {
            RebarFluid fuel = getMultiblockComponentOrThrow(FluidInputHatch.class, fuelInputPosition()).getFluidType();
            double fluidBurnRate = fuelBurnRate * 1000.0 / fuel.getTag(FlammableTag.class).secondsPerBucket();
            display.add(UnitFormat.MILLIBUCKETS_PER_SECOND.format(fluidBurnRate).decimalPlaces(2).asComponent()
                    .append(Component.text(" "))
                    .append(fuel.getName())
            );
        }
        return display;
    }

    public double getMaxFuelLeft() {
        FluidInputHatch fuelInput = getMultiblockComponentOrThrow(FluidInputHatch.class, fuelInputPosition());
        return fuelInput.getFluidType() == null
                ? 0
                : fuelInput.getFluidType().getTag(FlammableTag.class).secondsPerBucket() * fuelInput.getFluidCapacity();
    }

    public double getFuelLeft() {
        FluidInputHatch fuelInput = getMultiblockComponentOrThrow(FluidInputHatch.class, fuelInputPosition());
        return fuelInput.getFluidType() == null
                ? 0
                : fuelInput.getFluidType().getTag(FlammableTag.class).secondsPerBucket() * fuelInput.getFluidAmount() / 1000.0;
    }

    public static @NonNull List<RebarFluid> getFlammableFluids() {
        List<RebarFluid> fluids = new ArrayList<>();
        for (RebarFluid fluid : RebarRegistry.FLUIDS) {
            if (fluid.hasTag(FlammableTag.class)) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }

    public abstract Vector3i waterInputPosition();
    public abstract Vector3i steamOutputPosition();
    public abstract Vector3i fuelInputPosition();
}
