package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNode;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class SteamEngine extends RebarBlock implements
        DirectionalRebarBlock,
        FluidBufferRebarBlock,
        SimpleElectricRebarBlock,
        TickingRebarBlock,
        SimpleRebarMultiblock {

    private final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double steamUsage = getSettingOrThrow("steam-usage", ConfigAdapter.DOUBLE);
    private final double steamCapacity = getSettingOrThrow("steam-capacity", ConfigAdapter.DOUBLE);
    private final double powerProduction = getSettingOrThrow("power-production", ConfigAdapter.DOUBLE);

    public static final class Item extends RebarItem {

        private final double steamUsage = getSettingOrThrow("steam-usage", ConfigAdapter.DOUBLE);
        private final double steamCapacity = getSettingOrThrow("steam-capacity", ConfigAdapter.DOUBLE);
        private final double powerProduction = getSettingOrThrow("power-production", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("steam-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamUsage)),
                    RebarArgument.of("steam-capacity", UnitFormat.MILLIBUCKETS.format(steamCapacity)),
                    RebarArgument.of("power-production", UnitFormat.WATTS.format(powerProduction))
            );
        }
    }

    @SuppressWarnings("unused")
    public SteamEngine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, context.getFacing());
        createFluidBuffer(PylonFluids.STEAM, steamCapacity, true, false);
        createSimpleElectricPort(ElectricNode.Type.PRODUCER, getFacing().getOppositeFace());
    }

    @SuppressWarnings("unused")
    public SteamEngine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(new Vector3i(0, 1, 0), MultiblockComponent.of(PylonKeys.SMOKESTACK_CAP));
    }

    @Override
    public void tick() {
        double adjustedSteamUsage = tickInterval / 20.0 * steamUsage;
        if (fluidAmount(PylonFluids.STEAM) < adjustedSteamUsage) {
            setPowerProduced(0);
            return;
        }
        removeFluid(PylonFluids.STEAM, adjustedSteamUsage);
        setPowerProduced(powerProduction);

        Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                .location(getBlock().getLocation().add(0, 1, 0).toCenterLocation())
                .offset(0, 1, 0)
                .count(0)
                .extra(0.03)
                .spawn();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContents(
                        PylonFluids.STEAM,
                        fluidCapacity(PylonFluids.STEAM),
                        fluidAmount(PylonFluids.STEAM)
                ))
                .add(Component.translatable("pylon.message.producing-power", RebarArgument.of("power", UnitFormat.WATTS.format(getPowerProduced()).decimalPlaces(1))));
    }
}
