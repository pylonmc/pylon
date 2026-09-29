package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class FluidOutputHatch extends FluidHatch {
    public FluidOutputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.NORTH, context, true);
    }

    public FluidOutputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true; // only had output point so doesn't really matter
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);

        if (!isFormedAndFullyLoaded()) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_casing"));
        } else if (getFluidType() == null) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_fluid"));
        } else {
            display.add(ProgressBar.fluidContentsWithName(
                    getFluidType(),
                    getFluidCapacity(),
                    getFluidAmount()
            ));
        }

        return display;
    }

    @Override
    public boolean setFluid(double amount) {
        // Hack to prevent fluid type from being reset when fluid gets to zero
        RebarFluid fluid = getFluidType();
        boolean result = super.setFluid(amount);
        setFluidType(fluid);
        return result;
    }
}
