package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class HydraulicGrindstoneTurner extends RebarBlock implements
        TickingRebarBlock,
        FluidBufferRebarBlock,
        DirectionalRebarBlock {

    public final int hydraulicFluidUsage = getSettingOrThrow("hydraulic-fluid-usage", ConfigAdapter.INTEGER);
    public final double buffer = getSettingOrThrow("buffer", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public final int hydraulicFluidUsage = getSettingOrThrow("hydraulic-fluid-usage", ConfigAdapter.INTEGER);
        public final double buffer = getSettingOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("hydraulic-fluid-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(hydraulicFluidUsage)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public HydraulicGrindstoneTurner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(Grindstone.CYCLE_DURATION_TICKS + 1);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicGrindstoneTurner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        Grindstone grindstone = BlockStorage.getAs(Grindstone.class, getBlock().getRelative(BlockFace.UP));
        if (grindstone == null || grindstone.isProcessingRecipe() || !grindstone.isFormedAndFullyLoaded()) {
            return;
        }

        GrindstoneRecipe nextRecipe = grindstone.getNextRecipe();
        if (nextRecipe == null) {
            return;
        }

        double hydraulicFluidUsed = hydraulicFluidUsage * nextRecipe.timeTicks() / 20.0;

        if (fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidUsed
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidUsed
                || !grindstone.tryStartRecipe(nextRecipe)
        ) {
            return;
        }

        removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidUsed);
        addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidUsed);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContents(
                        PylonFluids.HYDRAULIC_FLUID,
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID)
                ))
                .add(ProgressBar.fluidContents(
                        PylonFluids.DIRTY_HYDRAULIC_FLUID,
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID)
                ));
    }
}
