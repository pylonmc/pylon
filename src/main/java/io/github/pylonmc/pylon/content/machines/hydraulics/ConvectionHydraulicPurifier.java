package io.github.pylonmc.pylon.content.machines.hydraulics;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTank;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class ConvectionHydraulicPurifier extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        HydraulicPurifier {

    public static final Vector3i LEFT_INPUT = new Vector3i(2, 0, 0);
    public static final Vector3i RIGHT_INPUT = new Vector3i(-2, 0, 0);
    public static final Vector3i HYDRAULIC_FLUID_INPUT = new Vector3i(-1, 0, 1);
    public static final Vector3i HYDRAULIC_FLUID_OUTPUT = new Vector3i(1, 0, 1);

    public final double minFluid = getSettings().getOrThrow("min-fluid", ConfigAdapter.INTEGER);
    public final double basePurificationEfficiency = getSettings().getOrThrow("base-purification-efficiency", ConfigAdapter.DOUBLE);
    public final double maxPurificationEfficiency = getSettings().getOrThrow("max-purification-efficiency", ConfigAdapter.DOUBLE);
    public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
    public final int fluidAtMaxEfficiency = getSettings().getOrThrow("fluid-at-max-efficiency", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double lavaParticleChance = getSettings().getOrThrow("lava-particle-chance", ConfigAdapter.DOUBLE);

    private static final Random RANDOM = new Random();

    public static class Item extends RebarItem {

        public final double minFluid = getSettings().getOrThrow("min-fluid", ConfigAdapter.INTEGER);
        public final double basePurificationEfficiency = getSettings().getOrThrow("base-purification-efficiency", ConfigAdapter.DOUBLE);
        public final double maxPurificationEfficiency = getSettings().getOrThrow("max-purification-efficiency", ConfigAdapter.DOUBLE);
        public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
        public final int fluidAtMaxEfficiency = getSettings().getOrThrow("fluid-at-max-efficiency", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("min-fluid-amount", UnitFormat.MILLIBUCKETS.format(minFluid)),
                    RebarArgument.of("max-fluid-amount", UnitFormat.MILLIBUCKETS.format(fluidAtMaxEfficiency)),
                    RebarArgument.of("base-purification-efficiency", UnitFormat.PERCENT.format(100 * basePurificationEfficiency)),
                    RebarArgument.of("max-purification-efficiency", UnitFormat.PERCENT.format(100 * maxPurificationEfficiency)),
                    RebarArgument.of("purification-speed", UnitFormat.MILLIBUCKETS_PER_SECOND.format(purificationSpeed))
            );
        }
    }

    @SuppressWarnings("unused")
    public ConvectionHydraulicPurifier(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        addEntity("lava1", new ItemDisplayBuilder()
                .material(Material.ORANGE_CONCRETE)
                .brightness(0)
                .transformation(new TransformBuilder()
                        .translate(0.25, -0.12, 0.25)
                        .scale(0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("lava2", new ItemDisplayBuilder()
                .material(Material.ORANGE_CONCRETE)
                .brightness(0)
                .transformation(new TransformBuilder()
                        .translate(-0.25, -0.12, -0.25)
                        .scale(0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("water1", new ItemDisplayBuilder()
                .material(Material.BLUE_CONCRETE)
                .transformation(new TransformBuilder()
                        .translate(0.25, -0.12, -0.25)
                        .scale(0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("water2", new ItemDisplayBuilder()
                .material(Material.BLUE_CONCRETE)
                .transformation(new TransformBuilder()
                        .translate(-0.25, -0.12, 0.25)
                        .scale(0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
        );
    }

    @SuppressWarnings("unused")
    public ConvectionHydraulicPurifier(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, 1, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(1, 0, 1), new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(new Vector3i(-1, 0, 1), new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));

        components.put(new Vector3i(1, 0, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 0, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(2, 0, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(2, 0, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-2, 0, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-2, 0, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(1, 1, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 1, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(2, 1, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(2, 1, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 1, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 1, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(LEFT_INPUT, new RebarMultiblockComponent(PylonKeys.FLUID_TANK));
        components.put(RIGHT_INPUT, new RebarMultiblockComponent(PylonKeys.FLUID_TANK));
        components.put(HYDRAULIC_FLUID_INPUT, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(HYDRAULIC_FLUID_OUTPUT, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        if (!hasEnoughWaterAndLava()) {
            return;
        }

        FluidInputHatch hydraulicInput = getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT);
        FluidOutputHatch hydraulicOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, HYDRAULIC_FLUID_OUTPUT);

        double inputFluidAvailable = hydraulicInput.fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID);
        double outputSpaceRemaining = hydraulicOutput.fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID);
        double fluidToPurify = Math.min(purificationSpeed, Math.min(inputFluidAvailable, outputSpaceRemaining)) * getTickInterval() / 20;
        double efficiency = getEfficiency();

        int brightness = (int) Math.round(15 * (efficiency - basePurificationEfficiency) / (maxPurificationEfficiency - basePurificationEfficiency));
        getHeldEntityOrThrow(ItemDisplay.class, "lava1").setBrightness(new Display.Brightness(brightness, brightness));
        getHeldEntityOrThrow(ItemDisplay.class, "lava2").setBrightness(new Display.Brightness(brightness, brightness));
        Block lightBlock = getBlock().getRelative(BlockFace.UP);
        if (lightBlock.getBlockData() instanceof Light light) {
            light.setLevel(brightness);
            lightBlock.setBlockData(light);
        }

        if (RANDOM.nextDouble() < lavaParticleChance) {
            new ParticleBuilder(Particle.LAVA)
                    .location(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
                    .spawn();
        }

        hydraulicInput.removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, fluidToPurify);
        hydraulicOutput.addFluid(PylonFluids.HYDRAULIC_FLUID, efficiency * fluidToPurify);
    }

    @Override
    public void onMultiblockFormed() {
        FluidInputHatch hydraulicInput = getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT);
        FluidOutputHatch hydraulicOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, HYDRAULIC_FLUID_OUTPUT);
        hydraulicInput.setFluidType(PylonFluids.DIRTY_HYDRAULIC_FLUID);
        hydraulicOutput.setFluidType(PylonFluids.HYDRAULIC_FLUID);
        Block light = getBlock().getRelative(BlockFace.UP);
        if (light.getType().isAir()) {
            light.setType(Material.LIGHT);
        }
        RebarSimpleMultiblock.super.onMultiblockFormed();
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        Block light = getBlock().getRelative(BlockFace.UP);
        if (light.getType() == Material.LIGHT) {
            light.setType(Material.AIR);
        }
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        if (!isFormedAndFullyLoaded()) {
            return new WailaDisplay(getNameTranslationKey());
        }

        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("efficiency",
                        hasEnoughWaterAndLava()
                                ? Component.translatable("pylon.item.convection_hydraulic_purifier.efficiency").arguments(
                                RebarArgument.of("bar", PylonUtils.createBar(
                                        (getEfficiency() - basePurificationEfficiency) / (maxPurificationEfficiency - basePurificationEfficiency),
                                        20,
                                        TextColor.fromHexString("#ffffff")
                                )),
                                RebarArgument.of("efficiency", UnitFormat.PERCENT.format(100 * getEfficiency()).decimalPlaces(2))
                                )
                                : Component.empty()
                )
        ));
    }

    public boolean hasEnoughWaterAndLava() {
        FluidTank leftInput = getMultiblockComponentOrThrow(FluidTank.class, LEFT_INPUT);
        FluidTank rightInput = getMultiblockComponentOrThrow(FluidTank.class, RIGHT_INPUT);
        boolean leftLavaRightWater = PylonFluids.LAVA.equals(leftInput.getFluidType())
                && leftInput.getFluidAmount() > minFluid
                && PylonFluids.WATER.equals(rightInput.getFluidType())
                && rightInput.getFluidAmount() > minFluid;
        boolean leftWaterRightLava = PylonFluids.WATER.equals(leftInput.getFluidType())
                && leftInput.getFluidAmount() > minFluid
                && PylonFluids.LAVA.equals(rightInput.getFluidType())
                && rightInput.getFluidAmount() > minFluid;

        return leftLavaRightWater || leftWaterRightLava;
    }

    public double getEfficiency() {
        FluidTank leftInput = getMultiblockComponentOrThrow(FluidTank.class, LEFT_INPUT);
        FluidTank rightInput = getMultiblockComponentOrThrow(FluidTank.class, RIGHT_INPUT);
        double lowestFluidAmount = Math.min(leftInput.getFluidAmount(), rightInput.getFluidAmount());
        return Math.clamp(
                basePurificationEfficiency + (maxPurificationEfficiency - basePurificationEfficiency) * lowestFluidAmount / fluidAtMaxEfficiency,
                basePurificationEfficiency,
                maxPurificationEfficiency
        );
    }
}
