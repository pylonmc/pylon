package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class PalladiumCondenser extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarProcessor,
        RebarDirectionalBlock,
        RebarTickingBlock {

    public final int shimmerDustPerCycle = getSettings().getOrThrow("shimmer-dust-per-cycle", ConfigAdapter.INTEGER);
    public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.INTEGER);
    public final double hydraulicFluidPerSecond = getSettings().getOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER);
    public final int machineTicksPerCycle = getSettings().getOrThrow("machine-ticks-per-cycle", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public static final Vector3i SHIMMER_DUST_INPUT_HATCH = new Vector3i(1, 0, 0);
    public static final Vector3i PALLADIUM_DUST_OUTPUT_HATCH = new Vector3i(-1, 0, 0);
    public static final Vector3i DIRTY_HYDRAULIC_FLUID_OUTPUT_HATCH = new Vector3i(-2, 0, 2);
    public static final Vector3i HYDRAULIC_FLUID_INPUT_HATCH = new Vector3i(2, 0, 2);
    public static final Vector3i BIODIESEL_INPUT_HATCH = new Vector3i(0, 0, 4);

    private static final List<Vector3i> COLLIMATOR_PILLAR_LOCATIONS = List.of(
            new Vector3i(1, 2, 0),
            new Vector3i(-1, 2, 0),
            new Vector3i(1, 2, 4),
            new Vector3i(-1, 2, 4),

            new Vector3i(2, 2, 1),
            new Vector3i(-2, 2, 1),
            new Vector3i(2, 2, 3),
            new Vector3i(-2, 2, 3)
    );

    private final Random random = new Random();

    public static class Item extends RebarItem {

        public final int shimmerDustPerCycle = getSettings().getOrThrow("shimmer-dust-per-cycle", ConfigAdapter.INTEGER);
        public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.INTEGER);
        public final double hydraulicFluidPerSecond = getSettings().getOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER);
        public final int machineTicksPerCycle = getSettings().getOrThrow("machine-ticks-per-cycle", ConfigAdapter.INTEGER);
        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);


        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("cycle-duration", UnitFormat.formatDuration(
                            Duration.ofSeconds((long) machineTicksPerCycle * tickInterval / 20), false
                    )),
                    RebarArgument.of("shimmer-dust-per-cycle", shimmerDustPerCycle),
                    RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselPerSecond)),
                    RebarArgument.of("hydraulic-fluid-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(hydraulicFluidPerSecond))
            );
        }
    }

    public PalladiumCondenser(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(getFacing());
        setTickInterval(tickInterval);
    }

    public PalladiumCondenser(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(SHIMMER_DUST_INPUT_HATCH, MultiblockComponent.of(PylonKeys.ITEM_INPUT_HATCH));
        components.put(PALLADIUM_DUST_OUTPUT_HATCH, MultiblockComponent.of(PylonKeys.ITEM_OUTPUT_HATCH));

        components.put(new Vector3i(-2, 0, 1), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));
        components.put(DIRTY_HYDRAULIC_FLUID_OUTPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(new Vector3i(-2, 0, 3), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(2, 0, 1), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));
        components.put(HYDRAULIC_FLUID_INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(2, 0, 3), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(-1, 0, 4), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));
        components.put(BIODIESEL_INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(1, 0, 4), MultiblockComponent.of(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(1, 0, 1), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 1), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 0, 3), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 3), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(2, 0, 0), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 0, 0), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(2, 0, 4), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 0, 4), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(1, 1, 1), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 1, 1), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 1, 3), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 1, 3), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(2, 1, 0), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 1, 0), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(2, 1, 4), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 1, 4), MultiblockComponent.of(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(1, 1, 0), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 1, 0), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 1, 4), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 1, 4), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(2, 1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-2, 1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(2, 1, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-2, 1, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        for (Vector3i vector : COLLIMATOR_PILLAR_LOCATIONS) {
            components.put(vector, MultiblockComponent.of(PylonKeys.COLLIMATOR_PILLAR));
        }

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        if (!isProcessing()) {
            startCycle();
            return;
        }

        FluidInputHatch biodieselInputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, BIODIESEL_INPUT_HATCH);
        FluidInputHatch hydraulicFluidInputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT_HATCH);
        FluidOutputHatch dirtyHydraulicFluidOutputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, DIRTY_HYDRAULIC_FLUID_OUTPUT_HATCH);
        ItemOutputHatch palladiumDustOutputHatch = getMultiblockComponentOrThrow(ItemOutputHatch.class, PALLADIUM_DUST_OUTPUT_HATCH);

        if (biodieselInputHatch.fluidAmount(PylonFluids.BIODIESEL) < dieselPerSecond * getTickInterval() / 20
                || hydraulicFluidInputHatch.fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerSecond * getTickInterval() / 20
                || dirtyHydraulicFluidOutputHatch.fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerSecond * getTickInterval() / 20
                || !palladiumDustOutputHatch.inventory.canHold(PylonItems.PALLADIUM_DUST)
        ) {
            return;
        }

        biodieselInputHatch.removeFluid(PylonFluids.BIODIESEL, dieselPerSecond * getTickInterval() / 20);
        hydraulicFluidInputHatch.removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerSecond * getTickInterval() / 20);
        dirtyHydraulicFluidOutputHatch.addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerSecond * getTickInterval() / 20);

        progressProcess(getTickInterval());

        new ParticleBuilder(Particle.ELECTRIC_SPARK)
                .count(100)
                .extra(0)
                .offset(0.4, 0.4, 0.4)
                .location(getMultiblockBlock(new Vector3i(0, 0, 2)).getLocation().toCenterLocation().add(0, 0.5, 0))
                .spawn();

        for (Vector3i vector : COLLIMATOR_PILLAR_LOCATIONS) {
            if (random.nextInt(100) < 5) {
                new ParticleBuilder(Particle.END_ROD)
                        .count(100)
                        .extra(0.005)
                        .offset(0.025, 8, 0.025)
                        .location(getMultiblockBlock(vector).getLocation().toCenterLocation().add(0, 8, 0))
                        .spawn();
            }
        }

        if (!isProcessing()) {
            startCycle();
        }
    }

    public void startCycle() {
        ItemInputHatch shimmerDustInputHatch = getMultiblockComponentOrThrow(ItemInputHatch.class, SHIMMER_DUST_INPUT_HATCH);
        ItemStack stack = shimmerDustInputHatch.inventory.getItem(0);
        if (stack != null && PylonItems.SHIMMER_DUST_2.equals(stack.asOne()) && stack.getAmount() >= shimmerDustPerCycle) {
            shimmerDustInputHatch.inventory.setItemAmount(new MachineUpdateReason(), 0, stack.getAmount() - shimmerDustPerCycle);
            startProcess(getTickInterval() * machineTicksPerCycle);
        }
    }

    @Override
    public void onProcessFinished() {
        getMultiblockComponentOrThrow(ItemOutputHatch.class, PALLADIUM_DUST_OUTPUT_HATCH)
                .inventory
                .addItem(new MachineUpdateReason(), PylonItems.PALLADIUM_DUST);
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .count(200)
                .extra(0.03)
                .offset(0.2, 0.2, 0.2)
                .location(getMultiblockBlock(new Vector3i(0, 0, 2)).getLocation().toCenterLocation().add(0, 0.5, 0))
                .spawn();
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, BIODIESEL_INPUT_HATCH)
                .setFluidType(PylonFluids.BIODIESEL);
        getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT_HATCH)
                .setFluidType(PylonFluids.HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, DIRTY_HYDRAULIC_FLUID_OUTPUT_HATCH)
                .setFluidType(PylonFluids.DIRTY_HYDRAULIC_FLUID);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        String wailaFormat = "pylon.item." + getKey().getKey() + ".waila_format";
        Integer timeLeft = getProcessTicksRemaining();
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("duration-if-any",
                        timeLeft == null
                                ? Component.empty()
                                : Component.translatable(wailaFormat).arguments(
                                RebarArgument.of("duration", PylonUtils.createProgressBar(
                                        ((double) getProcessTimeTicks() - (double) getProcessTicksRemaining()) / (double) getProcessTimeTicks(),
                                        20,
                                        NamedTextColor.WHITE
                                ))
                        )
                )
        ));
    }
}
