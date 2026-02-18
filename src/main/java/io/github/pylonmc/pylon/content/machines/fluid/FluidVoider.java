package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FluidVoider extends RebarBlock implements RebarFluidBlock {

    public static class Item extends RebarItem {

        public final double voidRate = getSettings().getOrThrow("fluid-voided-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of(
                    "void_rate", UnitFormat.MILLIBUCKETS_PER_SECOND.format(voidRate).decimalPlaces(0)
            ));
        }
    }

    public final Material mainMaterial = getSettings().getOrThrow("main-material", ConfigAdapter.MATERIAL);
    public final double voidRate = getSettings().getOrThrow("fluid-voided-per-second", ConfigAdapter.DOUBLE);
    public final double mainDisplaySize = getSettings().getOrThrow("main-display-size", ConfigAdapter.DOUBLE);

    @SuppressWarnings("unused")
    public FluidVoider(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        createFluidPoint(FluidPointType.INPUT, BlockFace.UP, (float) (mainDisplaySize / 2.0));
        addEntity("main", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(mainMaterial)
                        .addCustomModelDataString(getKey() + ":main")
                        .build()
                )
                .transformation(new TransformBuilder()
                        .scale(mainDisplaySize)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public FluidVoider(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        return voidRate * RebarConfig.FLUID_TICK_INTERVAL / 20;
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        // do nothing lol
    }
}
