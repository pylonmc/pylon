package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.simple.MixingPot;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
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


public class HydraulicMixingAttachment extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarProcessor,
        RebarDirectionalBlock {

    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final int downAnimationTimeTicks = getSettings().getOrThrow("down-animation-time-ticks", ConfigAdapter.INTEGER);
    public final int upAnimationTimeTicks = getSettings().getOrThrow("up-animation-time-ticks", ConfigAdapter.INTEGER);
    public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
        public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0)),
                    RebarArgument.of("hydraulic-fluid-per-craft", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerCraft)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public HydraulicMixingAttachment(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);

        addEntity("mixing_attachment_shaft", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
                        .addCustomModelDataString(getKey() + ":mixing_attachment_shaft")
                )
                .transformation(getShaftTransformation(0.7))
                .build(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
        );

        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicMixingAttachment(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }


    @Override
    public void tick() {
        if (isProcessing()) {
            progressProcess(getTickInterval());
            return;
        }

        MixingPot mixingPot = BlockStorage.getAs(MixingPot.class, getBlock().getRelative(BlockFace.DOWN, 2));
        if (mixingPot == null || fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerCraft
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerCraft
                || !mixingPot.tryDoRecipe()) {
            return;
        }

        removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerCraft);
        addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerCraft);

        startProcess(cooldownTicks);

        PylonUtils.animate(getMixingAttachmentShaft(), downAnimationTimeTicks, getShaftTransformation(0.2));
        Bukkit.getScheduler().runTaskLater(
                Pylon.getInstance(),
                () -> PylonUtils.animate(getMixingAttachmentShaft(), upAnimationTimeTicks, getShaftTransformation(0.7)),
                downAnimationTimeTicks
        );
    }

    private static @NotNull Matrix4f getShaftTransformation(double yTranslation) {
        return new TransformBuilder()
                .translate(0, yTranslation, 0)
                .scale(0.2, 1.5, 0.2)
                .buildForItemDisplay();
    }

    public @Nullable ItemDisplay getMixingAttachmentShaft() {
        return getHeldEntity(ItemDisplay.class, "mixing_attachment_shaft");
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
