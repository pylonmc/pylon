package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.simple.Press;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;


public class HydraulicPressPiston extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarDirectionalBlock {

    public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("hydraulic-fluid-per-craft", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerCraft)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public HydraulicPressPiston(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        addEntity("press_piston_shaft", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.SPRUCE_LOG)
                        .addCustomModelDataString(getKey() + ":press_piston_shaft")
                )
                .transformation(getTransformation(0.0))
                .build(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
        );
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicPressPiston(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public void tick() {
        Press press = BlockStorage.getAs(Press.class, getBlock().getRelative(BlockFace.DOWN, 2));
        if (press == null
                || fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerCraft
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerCraft
                || !press.tryStartRecipe()) {
            return;
        }

        removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerCraft);
        addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerCraft);

        PylonUtils.animate(
                getPistonShaft(),
                Press.TIME_PER_ITEM_TICKS - Press.RETURN_TO_START_TIME_TICKS,
                getTransformation(-0.3)
        );
        Bukkit.getScheduler().runTaskLater(
                Pylon.getInstance(),
                () -> PylonUtils.animate(getPistonShaft(), Press.RETURN_TO_START_TIME_TICKS, getTransformation(0.0)),
                Press.TIME_PER_ITEM_TICKS - Press.RETURN_TO_START_TIME_TICKS
        );
    }

    private static @NotNull Matrix4f getTransformation(double yTranslation) {
        return new TransformBuilder()
                .translate(0, yTranslation, 0)
                .scale(0.3, 1.6, 0.3)
                .buildForItemDisplay();
    }

    public @Nullable ItemDisplay getPistonShaft() {
        return getHeldEntity(ItemDisplay.class, "press_piston_shaft");
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                ))
        ));
    }
}
