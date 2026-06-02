package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.CauldronRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.FluidTankRebarBlock;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;


public class PipedCauldron extends RebarBlock implements CauldronRebarBlockHandler, FluidTankRebarBlock {

    public PipedCauldron(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setCapacity(1000.0);
        createFluidPoint(FluidPointType.INPUT, context.getFacing());
        createFluidPoint(FluidPointType.OUTPUT, context.getFacing().getOppositeFace());
    }

    public PipedCauldron(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return fluid.equals(PylonFluids.WATER) || fluid.equals(PylonFluids.LAVA);
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        FluidTankRebarBlock.super.onFluidAdded(fluid, amount);
        updateCauldronLevel();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        FluidTankRebarBlock.super.onFluidRemoved(fluid, amount);
        updateCauldronLevel();
    }

    @Override
    public void onCauldronLevelChange(@NotNull CauldronLevelChangeEvent event, @NotNull EventPriority priority) {
        Material oldMaterial = event.getBlock().getType();
        Material newMaterial = event.getNewState().getBlockData().getMaterial();

        if (oldMaterial == Material.LAVA_CAULDRON) {
            // lava -> empty
            setFluid(0);
            return;
        }

        if (oldMaterial == Material.WATER_CAULDRON && newMaterial == Material.CAULDRON) {
            // 1/3 water -> empty
            setFluid(0);
            return;
        }

        if (oldMaterial == Material.WATER_CAULDRON && newMaterial == Material.WATER_CAULDRON) {
            // ?/3 water -> ?/3 water
            int oldLevel = ((Levelled) getBlock().getBlockData()).getLevel();
            int newLevel = ((Levelled) event.getNewState().getBlockData()).getLevel();
            int levelChange = newLevel - oldLevel;
            setFluid(getFluidAmount() + levelChange * 333.3333333333333333333333333333333333333333333333333333);
            return;
        }

        if (oldMaterial == Material.CAULDRON && newMaterial == Material.WATER_CAULDRON) {
            // empty -> ?/3 water
            int newLevel = ((Levelled) event.getNewState().getBlockData()).getLevel();
            setFluidType(PylonFluids.WATER);
            setFluid(newLevel * 333.3333333333333333333333333333333333333333333333333333);
            return;
        }

        if (oldMaterial == Material.CAULDRON && newMaterial == Material.LAVA_CAULDRON) {
            // empty -> lava
            setFluidType(PylonFluids.LAVA);
            setFluid(1000.0);
        }
    }

    private void updateCauldronLevel() {
        if (getFluidType() == null) {
            getBlock().setType(Material.CAULDRON);
            return;
        }

        if (PylonFluids.WATER.equals(getFluidType())) {
            int targetLevel = (int) Math.floor(getFluidAmount() / 333.3333333333333333333333333333333333333333333333333333); // lol
            if (targetLevel == 0) {
                getBlock().setType(Material.CAULDRON);
            } else {
                getBlock().setType(Material.WATER_CAULDRON);
                Levelled levelled = (Levelled) Material.WATER_CAULDRON.createBlockData();
                levelled.setLevel(targetLevel);
                getBlock().setBlockData(levelled);
            }
            return;
        }

        if (PylonFluids.LAVA.equals(getFluidType())) {
            if (getFluidAmount() < 999.999) {
                getBlock().setType(Material.CAULDRON);
            } else {
                getBlock().setType(Material.LAVA_CAULDRON);
            }
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("fluid", ProgressBar.fluidContentsWithName(getFluidType(), getFluidCapacity(), getFluidAmount()))
        ));
    }
}
