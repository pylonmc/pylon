package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarMultiblock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FluidTank extends RebarBlock
        implements RebarMultiblock, FluidTankWithDisplayEntity, RebarDirectionalBlock {

    private final int maxHeight = getSettings().getOrThrow("max-height", ConfigAdapter.INTEGER);

    private final List<FluidTankCasing> casings = new ArrayList<>();
    private final List<FluidTemperature> allowedTemperatures = new ArrayList<>();

    public static class Item extends RebarItem {

        private final int maxHeight = getSettings().getOrThrow("max-height", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("max-height", UnitFormat.BLOCKS.format(maxHeight))
            );
        }
    }

    @SuppressWarnings("unused")
    public FluidTank(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        addEntity("fluid", new ItemDisplayBuilder()
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
    }

    @SuppressWarnings("unused")
    public FluidTank(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Set<ChunkPosition> getChunksOccupied() {
        return Set.of(new ChunkPosition(getBlock()));
    }

    @Override
    public boolean checkFormed() {
        casings.forEach(FluidTankCasing::reset);
        casings.clear();
        for (int i = 0; i < maxHeight; i++) {
            FluidTankCasing casing = BlockStorage.getAs(
                    FluidTankCasing.class,
                    getBlock().getRelative(0, i + 1, 0)
            );

            FluidTankCasing casingType = casings.isEmpty() ? casing : casings.getFirst();
            if (casing == null || casing.getKey() != casingType.getKey()) {
                break;
            }

            casings.add(casing);
        }
        return !casings.isEmpty();
    }

    @Override
    public void onMultiblockFormed() {
        onMultiblockRefreshed();
    }

    @Override
    public void onMultiblockRefreshed() {
        FluidTankCasing casingType = casings.getFirst();
        allowedTemperatures.clear();
        allowedTemperatures.addAll(casingType.allowedTemperatures);
        setCapacity(casings.size() * casingType.capacity);
        setFluid(Math.min(getFluidCapacity(), getFluidAmount()));

        int height = casings.size();
        for (int i = 0; i < casings.size(); i++) {
            FluidTankCasing casing = casings.get(i);
            if (i == 0) {
                casing.setShape(height == 1 ? FluidTankCasing.Shape.SINGLE : FluidTankCasing.Shape.BOTTOM);
            } else if (i == casings.size() - 1) {
                casing.setShape(FluidTankCasing.Shape.TOP);
            } else {
                casing.setShape(FluidTankCasing.Shape.MIDDLE);
            }
            Waila.addWailaOverride(new BlockPosition(casing.getBlock()), this::getWaila);
            casing.tank = this;
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        casings.forEach(FluidTankCasing::reset);
        casings.clear();
        allowedTemperatures.clear();
        if (!partUnloaded) {
            setCapacity(0);
            setFluid(0);
            setFluidType(null);
        }
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        Vector offset = otherBlock.getLocation().toVector().subtract(getBlock().getLocation().toVector());
        return offset.getBlockX() == 0
                && offset.getBlockY() > 0
                && offset.getBlockY() <= maxHeight
                && offset.getBlockZ() == 0;
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return fluid.hasTag(FluidTemperature.class) && allowedTemperatures.contains(fluid.getTag(FluidTemperature.class));
    }

    @Override
    public Vector3d fluidDisplayScale() {
        return new Vector3d(0.9, casings.size() - 0.1, 0.9);
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                        getFluidAmount(),
                        getFluidCapacity(),
                        20,
                        TextColor.color(200, 255, 255)
                )),
                RebarArgument.of("fluid", getFluidType() == null
                        ? Component.translatable("pylon.fluid.none")
                        : getFluidType().getName()
                )
        ));
    }
}
