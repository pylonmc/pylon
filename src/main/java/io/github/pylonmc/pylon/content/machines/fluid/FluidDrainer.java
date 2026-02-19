package io.github.pylonmc.pylon.content.machines.fluid;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarNoVanillaContainerBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FluidDrainer extends RebarBlock
        implements RebarFluidBufferBlock, RebarDirectionalBlock, RebarTickingBlock, RebarNoVanillaContainerBlock {

    public final Material material = getSettings().getOrThrow("material", ConfigAdapter.MATERIAL);
    public final RebarFluid fluid = getSettings().getOrThrow("fluid", ConfigAdapter.REBAR_FLUID);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final Block drainBlock;

    public static class Item extends RebarItem {

        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("drain_interval", UnitFormat.SECONDS.format(tickInterval / 20.0).decimalPlaces(1)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public FluidDrainer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, true);
        createFluidBuffer(fluid, buffer, false, true);
        Preconditions.checkState(getBlock().getBlockData() instanceof Directional);
        Directional directional = (Directional) getBlock().getBlockData();
        drainBlock = getBlock().getRelative(directional.getFacing());
    }

    @SuppressWarnings("unused")
    public FluidDrainer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
        Preconditions.checkState(getBlock().getBlockData() instanceof Directional);
        Directional directional = (Directional) getBlock().getBlockData();
        drainBlock = getBlock().getRelative(directional.getFacing());
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                        fluidAmount(fluid),
                        fluidCapacity(fluid),
                        20,
                        TextColor.color(200, 255, 255)
                ))
        ));
    }

    @Override
    public void tick() {
        if (fluidSpaceRemaining(fluid) >= 1000.0
                && drainBlock.getType() == material
                && drainBlock.getBlockData() instanceof Levelled levelled
                && levelled.getLevel() == 0 // 0 = source block (for some reason)
                && new BlockBreakBlockEvent(drainBlock, getBlock(), List.of()).callEvent()
        ) {
            drainBlock.setType(Material.AIR);
            addFluid(fluid, 1000.0);
        }
    }
}
