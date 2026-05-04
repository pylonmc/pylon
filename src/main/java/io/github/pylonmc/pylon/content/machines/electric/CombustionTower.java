package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class CombustionTower extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarSimpleMultiblock {

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double dieselUsage = getSettings().getOrThrow("diesel-usage", ConfigAdapter.DOUBLE);
    private final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
    private final double exhaustProduction = getSettings().getOrThrow("exhaust-production", ConfigAdapter.DOUBLE);
    private final double exhaustBuffer = getSettings().getOrThrow("exhaust-buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double dieselUsage = getSettings().getOrThrow("diesel-usage", ConfigAdapter.DOUBLE);
        private final double exhaustProduction = getSettings().getOrThrow("exhaust-production", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselUsage)),
                    RebarArgument.of("exhaust-production", UnitFormat.MILLIBUCKETS_PER_SECOND.format(exhaustProduction))
            );
        }
    }

    @SuppressWarnings("unused")
    public CombustionTower(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);

        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
        createFluidBuffer(PylonFluids.VERY_HOT_EXHAUST, exhaustBuffer, false, true);

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH);
    }

    @SuppressWarnings("unused")
    public CombustionTower(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        double dieselToConsume = Math.min(dieselUsage / getTicksPerSecond(), fluidAmount(PylonFluids.BIODIESEL));
        double exhaustToProduce = dieselToConsume * (exhaustProduction / dieselUsage);
        double actualExhaustProduced = Math.min(exhaustToProduce, fluidSpaceRemaining(PylonFluids.VERY_HOT_EXHAUST));
        double actualDieselConsumed = actualExhaustProduced * (dieselUsage / exhaustProduction);

        removeFluid(PylonFluids.BIODIESEL, actualDieselConsumed);
        addFluid(PylonFluids.VERY_HOT_EXHAUST, actualExhaustProduced);

        Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                .location(getBlock().getLocation().add(Vector.fromJOML(SMOKESTACK_POS)).toCenterLocation())
                .offset(0, 1, 0)
                .count(0)
                .extra(0.03)
                .spawn();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("diesel", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.BIODIESEL),
                        fluidCapacity(PylonFluids.BIODIESEL),
                        20,
                        TextColor.fromHexString("#eaa627")
                )),
                RebarArgument.of("exhaust", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.VERY_HOT_EXHAUST),
                        fluidCapacity(PylonFluids.VERY_HOT_EXHAUST),
                        20,
                        TextColor.fromHexString("#ff2b0f")
                ))
        ));
    }

    private static final Vector3i SMOKESTACK_POS = new Vector3i(0, 3, 0);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(SMOKESTACK_POS, new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));

        return components;
    }
}