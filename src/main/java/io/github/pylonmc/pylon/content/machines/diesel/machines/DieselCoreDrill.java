package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.content.machines.simple.CoreDrill;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DieselCoreDrill extends CoreDrill {

    public final int dieselUsage = getSettings().getOrThrow("diesel-usage", ConfigAdapter.INTEGER);
    public final double dieselPerRotation = dieselUsage * rotationDuration / 20.0;

    public static class Item extends CoreDrill.Item {

        public final int dieselUsage = getSettings().getOrThrow("diesel-usage", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            List<RebarArgument> placeholders = new ArrayList<>(super.getPlaceholders());
            placeholders.add(RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselUsage)));
            return placeholders;
        }
    }

    @SuppressWarnings("unused")
    public DieselCoreDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public DieselCoreDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 1, 1), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));

        components.put(new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, 0, -1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(0, -1, 1), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, -1, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -1, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, -1, -1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(new Vector3i(0, -2, 1), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, -2, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, -2, 0), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(0, -2, -1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, -2, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, -2, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -2, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -2, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(0, -3, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(1, -3, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, -3, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -3, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -3, -1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(0, -3, -1), new RebarMultiblockComponent(PylonKeys.ITEM_OUTPUT_HATCH));
        components.put(new Vector3i(0, -3, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(1, -3, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(-1, -3, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(0, -3, 2), new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(1, -3, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, -3, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

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

        FluidInputHatch fluidInputHatch = getFluidInputHatch();
        ItemOutputHatch itemOutputHatch = getItemOutputHatch();
        Preconditions.checkState(fluidInputHatch != null && itemOutputHatch != null);

        if (fluidInputHatch.fluidAmount(PylonFluids.BIODIESEL) < dieselPerRotation || !itemOutputHatch.inventory.canHold(output)) {
            return;
        }

        fluidInputHatch.removeFluid(PylonFluids.BIODIESEL, dieselPerRotation);

        Vector smokePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0.0, 1.0, 1.0),
                getMultiblockDirection()
        ));
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .offset(0, 1, 0)
                .count(0)
                .extra(0.05)
                .spawn();

        super.tick();

        if (!isProcessing()) {
            startCycle();
        }
    }

    @Override
    public void onProcessFinished() {
        ItemOutputHatch hatch = getItemOutputHatch();
        Preconditions.checkState(hatch != null);
        hatch.inventory.addItem(new MachineUpdateReason(), output);
    }

    @Override
    public void onMultiblockFormed() {
        super.onMultiblockFormed();
        FluidInputHatch inputHatch = getFluidInputHatch();
        Preconditions.checkState(inputHatch != null);
        inputHatch.setFluidType(PylonFluids.BIODIESEL);
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        super.onMultiblockUnformed(partUnloaded);
        FluidInputHatch inputHatch = getFluidInputHatch();
        if (inputHatch != null) {
            inputHatch.setFluidType(null);
        }
    }

    public @Nullable FluidInputHatch getFluidInputHatch() {
        Vector relativeLocation = Vector.fromJOML(RebarUtils.rotateVectorToFace(new Vector3i(0, -3, 2), getFacing()));
        Location location = getBlock().getLocation().add(relativeLocation);
        return BlockStorage.getAs(FluidInputHatch.class, location);
    }

    public @Nullable ItemOutputHatch getItemOutputHatch() {
        Vector relativeLocation = Vector.fromJOML(RebarUtils.rotateVectorToFace(new Vector3i(0, -3, -1), getFacing()));
        Location location = getBlock().getLocation().add(relativeLocation);
        return BlockStorage.getAs(ItemOutputHatch.class, location);
    }
}
