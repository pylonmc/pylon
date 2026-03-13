package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.recipes.SmelteryMeltingPoint;
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

import java.util.List;

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
    public @NotNull List<Pair<RebarFluid, Double>> getSuppliedFluids() {
        SmelteryController controller = getController();
        if (controller == null) return List.of();

        return controller.getFluids().entrySet().stream()
                .filter(entry -> SmelteryMeltingPoint.getMeltingPoint(entry.getKey()) <= controller.getTemperature())
                .map(entry -> new Pair<>(entry.getKey(), Math.min(entry.getValue(), flowRate * RebarConfig.FLUID_TICK_INTERVAL / 20.0)))
                .toList();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        SmelteryController controller = getController();
        if (controller == null) return;
        controller.removeFluid(fluid, amount);
    }
}
