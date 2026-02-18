package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import kotlin.Pair;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class SmelteryOutputHatch extends SmelteryComponent implements RebarFluidBlock, RebarDirectionalBlock {

    public final double flowRate = getSettings().getOrThrow("flow-rate", ConfigAdapter.DOUBLE);

    @SuppressWarnings("unused")
    public SmelteryOutputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacingVertical());
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.NORTH, context, true);
    }

    @SuppressWarnings("unused")
    public SmelteryOutputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<RebarFluid, Double> getSuppliedFluids() {
        SmelteryController controller = getController();
        if (controller == null) return Map.of();

        Pair<RebarFluid, Double> supplied = controller.getBottomFluid();
        return supplied == null
                ? Map.of()
                : Map.of(supplied.getFirst(), Math.min(supplied.getSecond(), flowRate * RebarConfig.FLUID_TICK_INTERVAL / 20.0));
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        SmelteryController controller = getController();
        if (controller == null) return;
        controller.removeFluid(fluid, amount);
    }

}
