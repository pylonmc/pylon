package io.github.pylonmc.pylon.content.machines.smelting;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.pylonmc.pylon.api.MeltingPoint;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import kotlin.Pair;

public final class SmelteryOutputHatch extends SmelteryComponent implements FluidRebarBlock, DirectionalRebarBlock {

    public final double flowRate = getSettingOrThrow("flow-rate", ConfigAdapter.DOUBLE);

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
        if (controller == null || controller.getFluids().isEmpty()) return List.of();

        List<Pair<RebarFluid, Double>> suppliedFluids = new ArrayList<>();
        for (Map.Entry<RebarFluid, Double> entry : controller.getFluids().entrySet()) {
            RebarFluid fluid = entry.getKey();
            if (fluid.hasTag(MeltingPoint.class) && fluid.getTag(MeltingPoint.class).temperature() <= controller.getTemperature()) {
                suppliedFluids.add(new Pair<>(fluid, Math.min(entry.getValue(), flowRate * RebarConfig.FLUID_TICK_INTERVAL / 20.0)));
            }
        }
        return suppliedFluids;
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        SmelteryController controller = getController();
        if (controller == null) return;
        controller.removeFluid(fluid, amount);
    }
}
