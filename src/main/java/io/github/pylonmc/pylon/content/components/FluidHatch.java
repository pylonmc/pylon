package io.github.pylonmc.pylon.content.components;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankWithDisplayEntity;
import io.github.pylonmc.pylon.content.machines.fluid.multiblock.FluidTankCasingComponent;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.waila.Waila;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;


public abstract class FluidHatch extends RebarBlock implements
        FluidTankWithDisplayEntity,
        SimpleRebarMultiblock,
        DirectionalRebarBlock {

    public double wailaFluidCycleIntervalTicks = getSettingOrThrow("waila-fluid-cycle-interval-ticks", ConfigAdapter.DOUBLE);

    protected FluidHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createFluidDisplay(new Vector3i(0, 1, 0));
    }

    protected FluidHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        components.put(new Vector3i(0, 1, 0), FluidTankCasingComponent.INSTANCE);
        return components;
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        FluidTankCasing casing = BlockStorage.getAs(FluidTankCasing.class, getBlock().getRelative(BlockFace.UP));
        Preconditions.checkState(casing != null);
        Waila.addWailaOverride(casing.getBlock(), this);
        setCapacity(casing.capacity);
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        SimpleRebarMultiblock.super.onMultiblockUnformed(partUnloaded);
        Waila.removeWailaOverride(getBlock().getRelative(BlockFace.UP));
        setCapacity(0);
    }
}
