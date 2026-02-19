package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public final class SmelteryInputHatch extends SmelteryComponent implements RebarFluidBlock, RebarDirectionalBlock {
    public final double flowRate = getSettings().getOrThrow("flow-rate", ConfigAdapter.DOUBLE);

    @SuppressWarnings("unused")
    public SmelteryInputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacingVertical());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, true);
    }

    @SuppressWarnings("unused")
    public SmelteryInputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        SmelteryController controller = getController();
        if (controller == null || !fluid.hasTag(FluidTemperature.class)) return 0.0;
        return Math.min(controller.getCapacity() - controller.getTotalFluid(), flowRate * RebarConfig.FLUID_TICK_INTERVAL / 20);
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        SmelteryController controller = getController();
        if (controller == null) return;
        controller.addFluid(fluid, amount);
    }
}
