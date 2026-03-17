package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import kotlin.Pair;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class WaterPump extends RebarBlock implements RebarFluidBlock {

    public final double waterPerSecond = getSettings().getOrThrow("water-per-second", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double waterPerSecond = getSettings().getOrThrow("water-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of(
                    "water_per_second", UnitFormat.MILLIBUCKETS_PER_SECOND.format(waterPerSecond)
            ));
        }
    }

    @SuppressWarnings("unused")
    public WaterPump(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.UP);
    }

    @SuppressWarnings("unused")
    public WaterPump(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull List<Pair<RebarFluid, Double>> getSuppliedFluids() {
        Block below = getBlock().getRelative(BlockFace.DOWN);
        Material type = below.getType();
        if (type == Material.WATER || type == Material.WATER_CAULDRON || type == Material.BUBBLE_COLUMN) {
            return List.of(new Pair<>(PylonFluids.WATER, waterPerSecond * RebarConfig.FLUID_TICK_INTERVAL / 20.0));
        }

        BlockData data = below.getBlockData();
        if (data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
            return List.of(new Pair<>(PylonFluids.WATER, waterPerSecond * RebarConfig.FLUID_TICK_INTERVAL / 20.0));
        }

        return List.of();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {}
}