package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarNoVanillaContainerBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class ManualHydraulicPurifier extends RebarBlock implements
        RebarDirectionalBlock,
        RebarFluidBufferBlock,
        HydraulicPurifier,
        RebarInteractBlock,
        RebarNoVanillaContainerBlock {

    public final double hydraulicFluidPerCycle = getSettings().getOrThrow("hydraulic-fluid-per-cycle", ConfigAdapter.DOUBLE);
    public final int cycleDuration = getSettings().getOrThrow("cycle-duration", ConfigAdapter.INTEGER);
    public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

    public boolean isProcessing;

    public static class Item extends RebarItem {

        public final double hydraulicFluidPerCycle = getSettings().getOrThrow("hydraulic-fluid-per-cycle", ConfigAdapter.DOUBLE);
        public final int cycleDuration = getSettings().getOrThrow("cycle-duration", ConfigAdapter.INTEGER);
        public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("hydraulic-fluid-per-cycle", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerCycle)),
                    RebarArgument.of("cycle-duration", UnitFormat.SECONDS.format(cycleDuration / 20.0)),
                    RebarArgument.of("purification-efficiency", UnitFormat.PERCENT.format(purificationEfficiency * 100)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public ManualHydraulicPurifier(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.45F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.45F);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, false, true);
        addEntity("handle", new ItemDisplayBuilder()
                .material(Material.GRAY_CONCRETE)
                .transformation(new TransformBuilder()
                        .scale(0.2, 0.8, 0.2)
                )
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.75, 0))
        );
    }

    @SuppressWarnings("unused")
    public ManualHydraulicPurifier(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick()
                || event.getHand() != EquipmentSlot.HAND
                || event.getPlayer().isSneaking()
        ) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        if (isProcessing
                || fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerCycle
                || fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerCycle
        ) {
            return;
        }

        isProcessing = true;

        PylonUtils.animate(
                getHeldEntityOrThrow(ItemDisplay.class, "handle"),
                cycleDuration - 2,
                new TransformBuilder()
                        .translate(0.0, -0.35, 0.0)
                        .scale(0.2, 0.8, 0.2)
                        .buildForItemDisplay()
        );

        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            PylonUtils.animate(
                    getHeldEntityOrThrow(ItemDisplay.class, "handle"),
                    2,
                    new TransformBuilder()
                            .scale(0.2, 0.8, 0.2)
                            .buildForItemDisplay()
            );
        }, cycleDuration - 2);

        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerCycle);
            addFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerCycle);
            isProcessing = false;
        }, cycleDuration);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                ))
        ));
    }
}
