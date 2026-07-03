package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class FluidInputHatch extends FluidHatch {
    private static final NamespacedKey ALLOWED_FLUIDS_KEY = pylonKey("allowed_fluids");

    @Getter @Setter private List<RebarFluid> allowedFluids;

    public FluidInputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, true);
    }

    public FluidInputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        allowedFluids = pdc.get(ALLOWED_FLUIDS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID));
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, ALLOWED_FLUIDS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.REBAR_FLUID), allowedFluids);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return allowedFluids.contains(fluid);
    }

    @Override
    public void setFluidType(@Nullable RebarFluid fluid) {
        super.setFluidType(fluid);
        if (fluid != null && (allowedFluids == null || !allowedFluids.contains(fluid))) {
            throw new IllegalStateException("You should call FluidInputHatch#setAllowedFluid[s] to set the fluid type of an input hatch rather than calling setFluidType directly");
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);

        if (!isFormedAndFullyLoaded()) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_casing"));
        } else if (allowedFluids == null || allowedFluids.isEmpty()) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_fluid"));
        } else if (getFluidType() == null) {
            int fluidIndex = (int) (Bukkit.getCurrentTick() / wailaFluidCycleIntervalTicks) % allowedFluids.size();
            display.add(ProgressBar.fluidContentsWithName(
                    allowedFluids.get(fluidIndex),
                    getFluidCapacity(),
                    getFluidAmount()
            ));
        } else {
            display.add(ProgressBar.fluidContentsWithName(
                    getFluidType(),
                    getFluidCapacity(),
                    getFluidAmount()
            ));
        }

        return display;
    }

    public void setAllowedFluid(RebarFluid fluid) {
        allowedFluids = List.of(fluid);
    }
}
