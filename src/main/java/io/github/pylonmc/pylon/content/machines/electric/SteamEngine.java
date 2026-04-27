package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class SteamEngine extends RebarBlock implements
        RebarDirectionalBlock,
        RebarFluidBufferBlock,
        RebarElectricBlock,
        RebarTickingBlock,
        RebarSimpleMultiblock {

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double steamUsage = getSettings().getOrThrow("steam-usage", ConfigAdapter.DOUBLE);
    private final double steamCapacity = getSettings().getOrThrow("steam-capacity", ConfigAdapter.DOUBLE);
    private final double powerProduction = getSettings().getOrThrow("power-production", ConfigAdapter.DOUBLE);
    private final double outputVoltage = getSettings().getOrThrow("output-voltage", ConfigAdapter.DOUBLE);

    public static final class Item extends RebarItem {

        private final double steamUsage = getSettings().getOrThrow("steam-usage", ConfigAdapter.DOUBLE);
        private final double steamCapacity = getSettings().getOrThrow("steam-capacity", ConfigAdapter.DOUBLE);
        private final double powerProduction = getSettings().getOrThrow("power-production", ConfigAdapter.DOUBLE);
        private final double outputVoltage = getSettings().getOrThrow("output-voltage", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("steam-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamUsage)),
                    RebarArgument.of("steam-capacity", UnitFormat.MILLIBUCKETS.format(steamCapacity)),
                    RebarArgument.of("power-production", UnitFormat.WATTS.format(powerProduction)),
                    RebarArgument.of("output-voltage", UnitFormat.VOLTS.format(outputVoltage))
            );
        }
    }

    private ElectricNode.Producer node;

    @SuppressWarnings("unused")
    public SteamEngine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, context.getFacing());
        createFluidBuffer(PylonFluids.STEAM, steamCapacity, true, false);
        addElectricPort(context.getFacing().getOppositeFace(), new ElectricNode.Producer("output", new BlockPosition(block), 0, 0));
    }

    @SuppressWarnings("unused")
    public SteamEngine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        node = (ElectricNode.Producer) getElectricNodeOrThrow("output");
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));
    }

    @Override
    public void tick() {
        double adjustedSteamUsage = tickInterval / 20.0 * steamUsage;
        if (fluidAmount(PylonFluids.STEAM) < adjustedSteamUsage) {
            node.setVoltage(0);
            node.setPower(0);
            return;
        }
        removeFluid(PylonFluids.STEAM, adjustedSteamUsage);
        node.setVoltage(outputVoltage);
        node.setPower(powerProduction);

        Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                .location(getBlock().getLocation().add(0, 1, 0).toCenterLocation())
                .offset(0, 1, 0)
                .count(0)
                .extra(0.03)
                .spawn();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.STEAM),
                        fluidCapacity(PylonFluids.STEAM),
                        20,
                        TextColor.fromHexString("#d8d8d8")
                )),
                RebarArgument.of("power", Component.translatable(
                        "pylon.waila.electric.producer",
                        RebarArgument.of("power", UnitFormat.WATTS.format(node.getPower())),
                        RebarArgument.of("voltage", UnitFormat.VOLTS.format(node.getVoltage())
                        ))
                )
        ));
    }
}
