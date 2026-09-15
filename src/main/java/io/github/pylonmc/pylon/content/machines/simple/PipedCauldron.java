package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.CauldronRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.FluidTankRebarBlock;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PipedCauldron extends RebarBlock implements CauldronRebarBlockHandler, FluidTankRebarBlock {

    public static final double BOTTLE_FLUID_AMOUNT = 333.333333333333333333333333333333333333333333333333333;

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
        BlockData oldBlockData = getBlock().getBlockData();
        BlockData newBlockData = event.getNewState().getBlockData();

        Material oldMaterial = oldBlockData.getMaterial();
        Material newMaterial = newBlockData.getMaterial();

        if (newMaterial == Material.CAULDRON) {
            // water/powder snow/lava -> empty
            setFluid(0);
            return;
        }

        if (oldMaterial == Material.WATER_CAULDRON && newMaterial == Material.WATER_CAULDRON) {
            // ?/3 water -> ?/3 water
            int oldLevel = ((Levelled) oldBlockData).getLevel();
            int newLevel = ((Levelled) newBlockData).getLevel();
            int levelChange = newLevel - oldLevel;
            setFluid(getFluidAmount() + levelChange * BOTTLE_FLUID_AMOUNT);
            return;
        }

        if (oldMaterial == Material.POWDER_SNOW_CAULDRON && newMaterial == Material.POWDER_SNOW_CAULDRON) {
            // ?/3 powder snow -> ?/3 powder snow
            int oldLevel = ((Levelled) oldBlockData).getLevel();
            int newLevel = ((Levelled) newBlockData).getLevel();
            int levelChange = newLevel - oldLevel;
            setFluid(getFluidAmount() + levelChange * BOTTLE_FLUID_AMOUNT);
            return;
        }

        if (oldMaterial == Material.CAULDRON && newMaterial == Material.WATER_CAULDRON) {
            // empty -> ?/3 water
            int newLevel = ((Levelled) newBlockData).getLevel();
            setFluidType(PylonFluids.WATER);
            setFluid(newLevel * BOTTLE_FLUID_AMOUNT);
            return;
        }

        if (oldMaterial == Material.CAULDRON && newMaterial == Material.POWDER_SNOW_CAULDRON) {
            // empty -> ?/3 powder snow
            int newLevel = ((Levelled) newBlockData).getLevel();
            setFluidType(PylonFluids.POWDER_SNOW);
            setFluid(newLevel * BOTTLE_FLUID_AMOUNT);
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
            int targetLevel = (int) Math.floor(getFluidAmount() / BOTTLE_FLUID_AMOUNT);
            if (targetLevel == 0) {
                getBlock().setType(Material.CAULDRON);
            } else {
                Levelled levelled = (Levelled) Material.WATER_CAULDRON.createBlockData();
                levelled.setLevel(targetLevel);
                getBlock().setBlockData(levelled);
            }
            return;
        }

        if (PylonFluids.POWDER_SNOW.equals(getFluidType())) {
            int targetLevel = (int) Math.floor(getFluidAmount() / BOTTLE_FLUID_AMOUNT);
            if (targetLevel == 0) {
                getBlock().setType(Material.CAULDRON);
            } else {
                Levelled levelled = (Levelled) Material.POWDER_SNOW.createBlockData();
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
    public void setFluidType(@Nullable RebarFluid fluid) {
        FluidTankRebarBlock.super.setFluidType(fluid);
        updateCauldronLevel();
    }

    @Override
    public boolean setFluid(double amount) {
        boolean result = FluidTankRebarBlock.super.setFluid(amount);
        updateCauldronLevel();
        return result;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player).add(ProgressBar.fluidContentsWithName(getFluidType(), getFluidCapacity(), getFluidAmount()));
    }
}
