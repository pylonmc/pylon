package io.github.pylonmc.pylon.content.machines.hydraulics;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.RebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.*;

public class HydraulicBreaker extends RebarBlock implements
        FluidBufferRebarBlock,
        DirectionalRebarBlock,
        TickingRebarBlock,
        RebarMultiblock,
        VirtualInventoryRebarBlock,
        LogisticRebarBlock,
        ProcessorRebarBlock,
        InteractRebarBlockHandler
{

    public final double hydraulicFluidPerBlock = getSettingOrThrow("hydraulic-fluid-per-block", ConfigAdapter.DOUBLE);
    public final double buffer = getSettingOrThrow("buffer", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double hydraulicFluidPerBlock = getSettingOrThrow("hydraulic-fluid-per-block", ConfigAdapter.DOUBLE);
        public final double buffer = getSettingOrThrow("buffer", ConfigAdapter.DOUBLE);
        public final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("hydraulic-fluid-per-block", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerBlock)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    public ItemStackBuilder topStack = ItemStackBuilder.of(Material.BLUE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":top");
    public ItemStackBuilder drillStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":drill");

    public VirtualInventory toolInventory = new VirtualInventory(1);

    @SuppressWarnings("unused")
    public HydraulicBreaker(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, BlockFace.EAST, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.WEST, context, false);
        setFacing(context.getFacing().getOppositeFace());
        addEntity("top", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.7, 0.2, 0.7))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(drillStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0.5)
                        .scale(0.6, 0.6, 0.2)
                        .rotate(0, 0, Math.PI / 4))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicBreaker(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        tryStartDrilling();
        createLogisticGroup("tool", LogisticGroupType.INPUT, toolInventory);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == Event.Result.DENY
        ) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        // drop old item
        ItemStack tool = toolInventory.getItem(0);
        if (tool != null) {
            getBlock().getWorld().dropItem(
                    getBlock().getLocation().toCenterLocation().add(0, 0.25, 0),
                    tool
            );
            toolInventory.setItem(new MachineUpdateReason(), 0, null);
            stopProcess();
            return;
        }

        // insert new item
        ItemStack newStack = event.getItem();
        if (newStack != null) {
            toolInventory.setItem(new MachineUpdateReason(), 0, newStack.clone());
            newStack.setAmount(0);
            tryStartDrilling();
        }
    }

    @Override
    public void tick() {
        if (!isProcessing()) {
            return;
        }

        progressProcess(tickInterval);
        Block drilling = getBlock().getRelative(getFacing());
        new ParticleBuilder(Particle.ITEM)
                .count(5)
                .extra(0.05)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.6, 0))
                .data(ItemStack.of(drilling.getType()))
                .spawn();
    }

    public void tryStartDrilling() {
        if (isProcessing()) {
            return;
        }

        Block toDrill = getBlock().getRelative(getFacing());
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(toDrill, tool)
                || fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerBlock
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerBlock
        ) {
            return;
        }

        startProcess((int) Math.round(RebarUtils.getBlockBreakTicks(tool, toDrill) / speed));
    }

    @Override
    public void onProcessFinished() {
        Block toDrill = getBlock().getRelative(getFacing());
        if (!toDrill.getWorld().getWorldBorder().isInside(toDrill.getLocation())) {
            return;
        }

        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(toDrill, tool)
                || !new BlockBreakBlockEvent(toDrill, getBlock(), new ArrayList<>()).callEvent()
        ) {
            return;
        }

        toDrill.breakNaturally();
        RebarUtils.damageItem(tool, 1, toDrill.getWorld());
        toolInventory.setItem(new MachineUpdateReason(), 0, tool);
        removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerBlock);
        addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerBlock);
    }

    @Override
    public @NotNull Set<ChunkPosition> getChunksOccupied() {
        return Set.of(new ChunkPosition(getBlock().getRelative(getFacing()).getChunk()));
    }

    @Override
    public boolean checkFormed() {
        return true;
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        return getBlock().getRelative(getFacing()).equals(otherBlock);
    }

    @Override
    public void onMultiblockRefreshed() {
        if (isProcessing()) {
            stopProcess();
        }
        tryStartDrilling();
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidAdded(fluid, amount);
        tryStartDrilling();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidRemoved(fluid, amount);
        tryStartDrilling();
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
                ))
                .add(isProcessing()
                        ? ProgressBar.timeRemaining(getProcessTimeSeconds(), getProcessSecondsRemaining())
                        : Component.translatable("pylon.message.idle")
                );
    }

    @Override
    public void onBlockBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        FluidBufferRebarBlock.super.onBlockBreak(drops, context);
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("tool", toolInventory);
    }
}
