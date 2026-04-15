package io.github.pylonmc.pylon.content.machines.hydraulics;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class BurnerHydraulicPurifier extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarProcessor,
        RebarInteractBlock,
        HydraulicPurifier {

    public static final Vector3i ITEM_INPUT = new Vector3i(0, 0, 2);
    public static final Vector3i FLUID_INPUT = new Vector3i(1, 0, 1);
    public static final Vector3i FLUID_OUTPUT = new Vector3i(-1, 0, 1);

    public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
    public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double hydraulicFluidPerMachineTick = purificationSpeed * tickInterval / 20;

    private static final Random RANDOM = new Random();

    public static class Item extends RebarItem {

        public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
        public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("purification_speed", UnitFormat.MILLIBUCKETS_PER_SECOND.format(purificationSpeed)),
                    RebarArgument.of("purification_efficiency", UnitFormat.PERCENT.format(purificationEfficiency * 100)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public BurnerHydraulicPurifier(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
    }

    @SuppressWarnings("unused")
    public BurnerHydraulicPurifier(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(ITEM_INPUT, new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(FLUID_INPUT, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(FLUID_OUTPUT, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.COPPER_FRAMED_GLASS));
        components.put(new Vector3i(0, 1, 2), new RebarMultiblockComponent(PylonKeys.COPPER_FRAMED_GLASS));

        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, 2, 2), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(1, 2, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 2, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, 3, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, 3, 2), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(1, 3, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 3, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 0, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 1, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 1, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 1, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 1, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 2, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 2, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 2, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 2, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 3, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 3, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 3, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 3, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        Lightable lightable = (Lightable) getBlock().getBlockData();
        lightable.setLit(false);
        getBlock().setBlockData(lightable);

        if (getProcessTimeTicks() != null) {

            FluidInputHatch fluidInput = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT);
            FluidOutputHatch fluidOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT);
            double fluidToPurify = Math.min(
                    hydraulicFluidPerMachineTick,
                    Math.min(
                            fluidInput.fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                            fluidOutput.fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID)
                    )
            );
            if (fluidToPurify < hydraulicFluidPerMachineTick) {
                return;
            }

            lightable = (Lightable) getBlock().getBlockData();
            lightable.setLit(true);
            getBlock().setBlockData(lightable);

            fluidInput.removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, fluidToPurify);
            fluidOutput.addFluid(PylonFluids.HYDRAULIC_FLUID, fluidToPurify * purificationEfficiency);
            progressProcess(getTickInterval());

            for (int i = 0; i < 5; i++) {
                double x = RANDOM.nextDouble(-0.4, 0.4);
                double z = RANDOM.nextDouble(-0.4, 0.4);
                new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .location(getBlock().getLocation().toCenterLocation().add(getFacing().getOppositeFace().getDirection()).add(x, 0.6, z))
                        .offset(0.0, 1.5, 0.0)
                        .extra(0.03)
                        .count(0)
                        .spawn();
            }
        }

        if (getProcessTimeTicks() == null) {
            tryConsumeFuel();
        }
    }

    public void tryConsumeFuel() {
        ItemInputHatch inputHatch = getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUT);
        ItemStack stack = inputHatch.inventory.getItem(0);
        if (stack == null || stack.isEmpty()) {
            return;
        }

        ItemType itemType = stack.getType().asItemType();
        if (itemType == null) {
            return;
        }

        inputHatch.inventory.setItem(new MachineUpdateReason(), 0, stack.subtract());
        startProcess(itemType.getBurnDuration() / 10);
    }

    @Override
    public void onMultiblockFormed() {
        getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT)
                .setFluidType(PylonFluids.DIRTY_HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT)
                .setFluidType(PylonFluids.HYDRAULIC_FLUID);
        RebarSimpleMultiblock.super.onMultiblockFormed();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        if (getProcessTicksRemaining() == null) {
            return new WailaDisplay(getNameTranslationKey());
        }

        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("fuel-bar", PylonUtils.createBar(
                        (double) getProcessTicksRemaining() / getProcessTimeTicks(),
                        20,
                        TextColor.fromHexString("#f6a446")
                ))
        ));
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getAction().isRightClick()) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }
}
