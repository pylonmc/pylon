package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SolarPurificationTower extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarTickingBlock,
        RebarDirectionalBlock,
        RebarFluidBufferBlock {

    public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.DOUBLE);
    public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
    public final double rainSpeedFraction = getSettings().getOrThrow("rain-speed-fraction", ConfigAdapter.DOUBLE);
    public final int lensLayers = getSettings().getOrThrow("lens-layers", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.DOUBLE);
        public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
        public final double rainSpeedFraction = getSettings().getOrThrow("rain-speed-fraction", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("rain_speed_percentage", UnitFormat.PERCENT.format(rainSpeedFraction * 100)),
                    RebarArgument.of("purification_speed", UnitFormat.MILLIBUCKETS_PER_SECOND.format(purificationSpeed)),
                    RebarArgument.of("purification_efficiency", UnitFormat.PERCENT.format(purificationEfficiency * 100)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public SolarPurificationTower(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public SolarPurificationTower(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 3, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 4, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_CAP));

        for (int j = 1; j < lensLayers + 1; j++) {
            for (int i = 0; i < 1 + 4*j; i++) {
                components.put(new Vector3i(2*j, 1, i - 2*j), new RebarMultiblockComponent(PylonKeys.SOLAR_LENS));
                components.put(new Vector3i(-2*j, 1, i - 2*j), new RebarMultiblockComponent(PylonKeys.SOLAR_LENS));
                components.put(new Vector3i(i - 2*j, 1, 2*j), new RebarMultiblockComponent(PylonKeys.SOLAR_LENS));
                components.put(new Vector3i(i - 2*j, 1, -2*j), new RebarMultiblockComponent(PylonKeys.SOLAR_LENS));
            }
        }

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded() || !getBlock().getWorld().isDayTime()) {
            return;
        }

        double multiplier = getBlock().getWorld().isClearWeather() ? 1.0 : rainSpeedFraction;
        double toPurify = Math.min(
                // maximum amount of dirty hydraulic fluid that can be purified this tick
                purificationSpeed * multiplier * getTickInterval() / 20.0,
                Math.min(
                        // amount of dirty hydraulic fluid available
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        // how much dirty hydraulic fluid can be converted without overflowing the hydraulic fluid buffer
                        fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID) / purificationEfficiency
                )
        );

        removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, toPurify);
        addFluid(PylonFluids.HYDRAULIC_FLUID, toPurify * purificationEfficiency);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                ))
        ));
    }
}
