package io.github.pylonmc.pylon.content.machines.boiler;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;
import java.util.Random;


public abstract class AbstractSolidBoiler extends AbstractBoiler {

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

    public static final NamespacedKey FUEL_LEFT = PylonUtils.pylonKey("fuel_left");
    public static final NamespacedKey DURATION_OF_LAST_FUEL_BURNT = PylonUtils.pylonKey("fuel_left");

    public static final Random random = new Random();

    public ItemStackBuilder panelStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":panel");
    public ItemStackBuilder gratingStack = ItemStackBuilder.of(Material.COPPER_BARS)
            .addCustomModelDataString(getKey() + ":grating");

    public final double minFuelConsumption = getSettingOrThrow("min-fuel-consumption", ConfigAdapter.DOUBLE);
    public final double maxFuelConsumption = getSettingOrThrow("max-fuel-consumption", ConfigAdapter.DOUBLE);

    public double fuelLeft;
    public double durationOfLastFuelburnt;

    @SuppressWarnings("unused")
    protected AbstractSolidBoiler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    protected AbstractSolidBoiler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        fuelLeft = pdc.get(FUEL_LEFT, RebarSerializers.DOUBLE);
        durationOfLastFuelburnt = pdc.get(DURATION_OF_LAST_FUEL_BURNT, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        super.write(pdc);
        pdc.set(FUEL_LEFT, RebarSerializers.DOUBLE, fuelLeft);
        pdc.set(DURATION_OF_LAST_FUEL_BURNT, RebarSerializers.DOUBLE, durationOfLastFuelburnt);
    }

    @Override
    public void onMultiblockFormed() {
        super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, waterInputPosition())
                .setFluidType(PylonFluids.WATER);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, steamOutputPosition())
                .setFluidType(PylonFluids.STEAM);
    }

    @Override
    double tryConsumeFuel(double fuelToConsumeSeconds) {
        if (fuelToConsumeSeconds == 0) {
            return 1.0;
        }

        // Consume new piece of fuel if not enough fuel left
        if (fuelLeft < fuelToConsumeSeconds) {
            ItemInputHatch inputHatch = getMultiblockComponentOrThrow(ItemInputHatch.class, fuelInputPosition());

            ItemStack fuel = inputHatch.inventory.getItem(0);
            if (fuel != null && fuel.getType().isFuel()) {
                double duration = fuel.getType().asItemType().getBurnDuration() / 20.0;
                durationOfLastFuelburnt = duration;
                fuelLeft += duration;
                inputHatch.inventory.setItem(new MachineUpdateReason(), 0, fuel.subtract());
            }
        }

        double actualFuelConsumedSeconds = Math.min(fuelLeft, fuelToConsumeSeconds);
        fuelLeft -= actualFuelConsumedSeconds;
        return actualFuelConsumedSeconds / fuelToConsumeSeconds;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        double fuelBurnRate = fuelBurntLastUpdate / (getTickInterval() / 20.0);

        WailaDisplay display = WailaDisplay.of(this, player);
        display.add(new ProgressBar()
                .proportion(temperature / maxTemperature)
                .barColor(fireColor())
                .bars(40)
                .suffix(Component.text(" ").append(UnitFormat.CELSIUS.format(temperature).decimalPlaces(1)))
        );
        display.add(UnitFormat.PERCENT.format(100 * fuelBurnRate).decimalPlaces(2));
        display.add(UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamOutputLastUpdate).decimalPlaces(2));
        if (fuelLeft > 0 && fuelBurnRate != 0) {
            display.add(ProgressBar.fuelRemaining(durationOfLastFuelburnt / fuelBurnRate, fuelLeft / fuelBurnRate));
        }
        return display;
    }

    public abstract Vector3i waterInputPosition();
    public abstract Vector3i steamOutputPosition();
    public abstract Vector3i fuelInputPosition();
}
