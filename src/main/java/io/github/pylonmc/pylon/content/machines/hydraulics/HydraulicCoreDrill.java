package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.content.machines.simple.CoreDrill;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class HydraulicCoreDrill extends CoreDrill {

    public final int hydraulicFluidUsage = getSettings().getOrThrow("hydraulic-fluid-usage", ConfigAdapter.INTEGER);
    public final double hydraulicFluidPerRotation = hydraulicFluidUsage * rotationDuration / 20.0;

    public static final Vector3i FLUID_INPUT_HATCH = new Vector3i(1, -2, 3);
    public static final Vector3i FLUID_OUTPUT_HATCH = new Vector3i(-1, -2, 3);
    public static final Vector3i ITEM_OUTPUT_HATCH = new Vector3i(0, -1, 3);

    public static class Item extends CoreDrill.Item {

        public final int hydraulicFluidUsage = getSettings().getOrThrow("hydraulic-fluid-usage", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            List<RebarArgument> placeholders = new ArrayList<>(super.getPlaceholders());
            placeholders.add(RebarArgument.of("hydraulic-fluid-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(hydraulicFluidUsage)));
            return placeholders;
        }
    }

    @SuppressWarnings("unused")
    public HydraulicCoreDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public HydraulicCoreDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 0, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, -1, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, -2, -1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(1, -1, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(1, -2, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -1, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -2, 0), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, -1, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));
        components.put(new Vector3i(0, -2, 1), new RebarMultiblockComponent(PylonKeys.IRON_SUPPORT_BEAM));

        components.put(new Vector3i(-1, -2, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -2, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, -2, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, -2, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(-1, -2, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, -2, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(0, -2, 3), new VanillaMultiblockComponent(Material.CAULDRON));
        components.put(FLUID_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(FLUID_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));

        components.put(ITEM_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.ITEM_OUTPUT_HATCH));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        FluidInputHatch inputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT_HATCH);
        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT_HATCH);
        ItemOutputHatch itemOutputHatch = getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUT_HATCH);

        if (inputHatch.getFluidAmount() < hydraulicFluidPerRotation
                || outputHatch.getFluidSpaceRemaining() < hydraulicFluidPerRotation
                || !itemOutputHatch.inventory.canHold(output)
        ) {
            return;
        }

        inputHatch.removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerRotation);
        outputHatch.addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerRotation);

        if (!isProcessing()) {
            startCycle();
        }

        super.tick();
    }

    @Override
    public void onProcessFinished() {
        getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUT_HATCH)
                .inventory
                .addItem(new MachineUpdateReason(), output);
    }

    @Override
    public void onMultiblockFormed() {
        super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUT_HATCH)
                .setAllowedFluids(PylonFluids.HYDRAULIC_FLUID);
    }
}
