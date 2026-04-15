package io.github.pylonmc.pylon.content.machines.hydraulics;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.LiseletteCollector;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class LiseletteHydraulicPurifier extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        HydraulicPurifier,
        RebarTickingBlock {

    private static final Random RANDOM = new Random();

    public static final Vector3i INPUT_HATCH = new Vector3i(-1, -3, 0);
    public static final Vector3i OUTPUT_HATCH = new Vector3i(1, -3, 0);
    public static final List<Vector3i> COLLECTORS = List.of(
            new Vector3i(3, 0, 0),
            new Vector3i(2, 0, 2),
            new Vector3i(0, 0, 3),
            new Vector3i(-2, 0, 2),
            new Vector3i(-3, 0, 0),
            new Vector3i(-2, 0, -2),
            new Vector3i(0, 0, -3),
            new Vector3i(2, 0, -2)
    );

    public final double maxFluidPurifiedPerStrike = getSettings().getOrThrow("max-fluid-purified-per-strike", ConfigAdapter.DOUBLE);
    public final double strikeChance = getSettings().getOrThrow("strike-chance", ConfigAdapter.DOUBLE);
    public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int maxHeight = getSettings().getOrThrow("max-height", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public final double maxFluidPurifiedPerStrike = getSettings().getOrThrow("max-fluid-purified-per-strike", ConfigAdapter.DOUBLE);
        public final double strikeChance = getSettings().getOrThrow("strike-chance", ConfigAdapter.DOUBLE);
        public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
        public final int maxHeight = getSettings().getOrThrow("max-height", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("max-purification-speed", UnitFormat.MILLIBUCKETS_PER_SECOND.format(maxFluidPurifiedPerStrike * strikeChance * 20 / tickInterval).decimalPlaces(0)),
                    RebarArgument.of("purification-efficiency", UnitFormat.PERCENT.format(purificationEfficiency * 100)),
                    RebarArgument.of("max-height", maxHeight)
            );
        }
    }

    @SuppressWarnings("unused")
    public LiseletteHydraulicPurifier(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
    }

    @SuppressWarnings("unused")
    public LiseletteHydraulicPurifier(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, -1, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, -2, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, -3, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(1, 2, 0), new RebarMultiblockComponent(PylonKeys.COPPER_FRAMED_GLASS));
        components.put(new Vector3i(-1, 2, 0), new RebarMultiblockComponent(PylonKeys.COPPER_FRAMED_GLASS));

        for (Vector3i position : COLLECTORS) {
            components.put(position, new RebarMultiblockComponent(PylonKeys.LISELETTE_COLLECTOR));
        }

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH).setFluidType(PylonFluids.DIRTY_HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH).setFluidType(PylonFluids.HYDRAULIC_FLUID);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        if (getBlock().getY() > maxHeight) {
            return;
        }

        FluidInputHatch inputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH);
        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH);

        if (RANDOM.nextDouble() < strikeChance) {
            int collectorIndex = RANDOM.nextInt(COLLECTORS.size() - 1);
            Vector collector = Vector.fromJOML(COLLECTORS.get(collectorIndex));
            new ParticleBuilder(Particle.PORTAL)
                    .extra(0.5)
                    .count(100)
                    .location(getBlock().getLocation().toCenterLocation().add(collector))
                    .spawn();

            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                if (!new BlockPosition(getBlock()).getChunk().isLoaded()) {
                    return;
                }
                PylonUtils.drawParticleLine(
                        getBlock().getLocation().toCenterLocation(),
                        getBlock().getLocation().toCenterLocation().add(collector),
                        0.1,
                        location -> new ParticleBuilder(Particle.END_ROD)
                                .location(location)
                                .extra(0)
                                .spawn()
                );
                double speed = 1.0 - ((double) getBlock().getY() + 64) / (maxHeight + 64);
                double toPurify = Math.min(
                        speed * maxFluidPurifiedPerStrike,
                        Math.min(
                                inputHatch.fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                                outputHatch.fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID) / purificationEfficiency
                        )
                );
                inputHatch.removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, toPurify);
                outputHatch.addFluid(PylonFluids.HYDRAULIC_FLUID, toPurify * purificationEfficiency);
            }, 45);

            for (int i = 0; i < 15; i++) {
                int j = i;
                Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                    if (!new BlockPosition(getBlock()).getChunk().isLoaded()) {
                        return;
                    }
                    getMultiblockComponentOrThrow(LiseletteCollector.class, collector.toVector3i())
                            .getHeldEntityOrThrow(ItemDisplay.class, "shell")
                            .setBrightness(new Display.Brightness(j, 0));
                }, 45 + i);
            }
        }
    }
}
