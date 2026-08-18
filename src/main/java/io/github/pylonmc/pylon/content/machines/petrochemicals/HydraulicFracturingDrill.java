package io.github.pylonmc.pylon.content.machines.petrochemicals;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.processor.RebarProcessor;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class HydraulicFracturingDrill extends RebarBlock implements
        SimpleRebarMultiblock,
        TickingRebarBlock,
        DirectionalRebarBlock {

    public static class Item extends RebarItem {

        public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
        public final int ticksToCreateFracture = getSettingOrThrow("ticks-to-create-fracture", ConfigAdapter.INTEGER);
        public final int steamPerFracture = getSettingOrThrow("steam-per-fracture", ConfigAdapter.INTEGER);
        public final int hydraulicFluidPerFracture = getSettingOrThrow("hydraulic-fluid-per-fracture", ConfigAdapter.INTEGER);
        public final int machineTicksPerSand = getSettingOrThrow("machine-ticks-per-sand", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("time-to-create-fracture", UnitFormat.formatDuration(Duration.ofSeconds(ticksToCreateFracture / 20))),
                    RebarArgument.of("steam-per-fracture", UnitFormat.MILLIBUCKETS.format(steamPerFracture)),
                    RebarArgument.of("hydraulic-fluid-per-fracture", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerFracture)),
                    RebarArgument.of("sand-per-fracture", ticksToCreateFracture / (machineTicksPerSand * tickInterval))
            );
        }
    }

    public static final NamespacedKey PROCESSOR = PylonUtils.pylonKey("processor");

    public static final Vector3i SAND_INPUT_HATCH = new Vector3i(0, -1, -1);
    public static final Vector3i HYDRAULIC_FLUID_INPUT_HATCH = new Vector3i(-1, -1, 0);
    public static final Vector3i STEAM_FLUID_INPUT_HATCH = new Vector3i(1, -1, 0);

    private static final Random RANDOM = new Random();

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int ticksToCreateFracture = getSettingOrThrow("ticks-to-create-fracture", ConfigAdapter.INTEGER);
    public final int steamPerFracture = getSettingOrThrow("steam-per-fracture", ConfigAdapter.INTEGER);
    public final int hydraulicFluidPerFracture = getSettingOrThrow("hydraulic-fluid-per-fracture", ConfigAdapter.INTEGER);
    public final int machineTicksPerSand = getSettingOrThrow("machine-ticks-per-sand", ConfigAdapter.INTEGER);

    public final double steamPerTick = (double) steamPerFracture / ticksToCreateFracture;
    public final double hydraulicFluidPerTick = (double) hydraulicFluidPerFracture / ticksToCreateFracture;

    public ItemStackBuilder beamStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":beam");

    public RebarProcessor processor;

    @SuppressWarnings("unused")
    public HydraulicFracturingDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);
        addEntity("beam-1", new ItemDisplayBuilder()
                .itemStack(beamStack)
                .transformation(new LineBuilder()
                        .from(1, 0.4, 0)
                        .to(0.1, 5.4, 0)
                        .thickness(0.3)
                        .build()
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
        addEntity("beam-2", new ItemDisplayBuilder()
                .itemStack(beamStack)
                .transformation(new LineBuilder()
                        .from(-1, 0.4, 0)
                        .to(-0.1, 5.4, 0)
                        .thickness(0.3)
                        .build()
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
        addEntity("beam-3", new ItemDisplayBuilder()
                .itemStack(beamStack)
                .transformation(new LineBuilder()
                        .from(0, 0.4, 1)
                        .to(0, 5.4, 0.1)
                        .thickness(0.3)
                        .build()
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
        addEntity("beam-4", new ItemDisplayBuilder()
                .itemStack(beamStack)
                .transformation(new LineBuilder()
                        .from(0, 0.4, -1)
                        .to(0, 5.4, -0.1)
                        .thickness(0.3)
                        .build()
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
        processor = new RebarProcessor();
        processor.start(ticksToCreateFracture);
    }

    @SuppressWarnings("unused")
    public HydraulicFracturingDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        processor = pdc.get(PROCESSOR, RebarSerializers.PROCESSOR);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(PROCESSOR, RebarSerializers.PROCESSOR, processor);
    }

    @Override
    public void postInitialise() {
        processor.onFinish(() -> {
            Block block = getMultiblockBlock(new Vector3i(0, -2, 0));
            if (BlockStorage.breakBlock(block) != null && BlockStorage.placeBlock(block, PylonKeys.HYDRAULIC_FRACTURE) != null) {
                Double maxYield = OilService.getOilYield(block);
                BlockStorage.getAs(HydraulicFracture.class, block).setYield(RANDOM.nextDouble() * (maxYield == null ? 0.0 : maxYield));
            }
        });
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(SAND_INPUT_HATCH, MultiblockComponent.of(PylonKeys.ITEM_INPUT_HATCH));
        components.put(HYDRAULIC_FLUID_INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(STEAM_FLUID_INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(0, -1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(1, -1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, -1, -1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -1, -1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, -1, 2), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -1, 2), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, -1, 2), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(0, 1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 1, 0), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, 1, -1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 1, 0), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(0, 0, -1), MultiblockComponent.of(PylonKeys.REINFORCED_GLASS_CASING));
        components.put(new Vector3i(0, 0, 1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(1, 0, 1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 0, -1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 0, 1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 0, -1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(1, 1, 1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 1, -1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, 1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, -1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(0, 0, 2), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE));
        components.put(new Vector3i(0, 1, 2), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE));

        components.put(new Vector3i(0, 1, 0), MultiblockComponent.of(PylonKeys.INJECTION_PIPE));
        components.put(new Vector3i(0, 2, 0), MultiblockComponent.of(PylonKeys.INJECTION_PIPE));
        components.put(new Vector3i(0, 3, 0), MultiblockComponent.of(PylonKeys.INJECTION_PIPE));
        components.put(new Vector3i(0, 4, 0), MultiblockComponent.of(PylonKeys.INJECTION_PIPE));
        components.put(new Vector3i(0, 5, 0), MultiblockComponent.of(PylonKeys.INJECTION_PIPE));
        components.put(new Vector3i(0, 6, 0), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(0, -1, 0), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(0, -2, 0), MultiblockComponent.of(PylonKeys.HYDRAULIC_FRACTURE_CAP));

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT_HATCH).setAllowedFluid(PylonFluids.HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidInputHatch.class, STEAM_FLUID_INPUT_HATCH).setAllowedFluid(PylonFluids.STEAM);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded() || !processor.isRunning()) {
            return;
        }

        FluidInputHatch hydraulicFluidInput = getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT_HATCH);
        FluidInputHatch steamInput = getMultiblockComponentOrThrow(FluidInputHatch.class, STEAM_FLUID_INPUT_HATCH);
        ItemInputHatch sandInput = getMultiblockComponentOrThrow(ItemInputHatch.class, SAND_INPUT_HATCH);
        ItemStack sand = sandInput.inventory.getItem(0);

        boolean shouldTakeSand = (processor.getElapsedTicks() / getTickInterval()) % machineTicksPerSand == 0;

        if (shouldTakeSand && !ItemTypeWrapper.of(Material.SAND).matches(sand)
                || hydraulicFluidInput.getFluidAmount() < hydraulicFluidPerTick
                || steamInput.getFluidAmount() < steamPerTick
        ) {
            return;
        }

        steamInput.removeFluid(steamPerTick * getTickInterval());
        hydraulicFluidInput.removeFluid(hydraulicFluidPerTick * getTickInterval());

        if (shouldTakeSand) {
            sandInput.inventory.setItemAmount(new MachineUpdateReason(), 0, sand.getAmount() - 1);
        }

        new ParticleBuilder(Particle.LARGE_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
                .offset(0.4, 0.2, 0.4)
                .count(10)
                .extra(0)
                .spawn();

        Vector flamePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0, 2.0, -2.0),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.FLAME)
                .location(getBlock().getLocation().toCenterLocation().add(flamePosition).add(0, -0.4, 0))
                .count(5)
                .extra(0)
                .spawn();
        new ParticleBuilder(Particle.SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(flamePosition).add(0, -0.2, 0))
                .count(20)
                .extra(0)
                .spawn();

        processor.tick(getTickInterval());
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (processor.isRunning()) {
            display.add(ProgressBar.recipeProgress(processor.getElapsedProportion()));
        }
        return display;
    }
}
