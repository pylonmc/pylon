package io.github.pylonmc.pylon.content.components;

import com.google.common.base.Preconditions;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class FluidInputHatch extends FluidHatch {
    public FluidInputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, true);
    }

    public FluidInputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public void removeFluid(double amount) {
        Preconditions.checkState(fluid != null);
        removeFluid(fluid, amount);
    }
}
