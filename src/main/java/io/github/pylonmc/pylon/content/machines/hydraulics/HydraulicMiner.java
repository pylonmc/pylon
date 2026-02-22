package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.base.*;
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
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class HydraulicMiner extends Miner implements
        RebarTickingBlock,
        RebarDirectionalBlock,
        RebarInteractBlock,
        RebarLogisticBlock,
        RebarFluidBufferBlock,
        RebarVirtualInventoryBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
    public final int hydraulicFluidPerBlock = getSettings().getOrThrow("hydraulic-fluid-per-block", ConfigAdapter.INTEGER);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final int radius = getSettings().getOrThrow("radius", ConfigAdapter.INTEGER);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
        public final int hydraulicFluidPerBlock = getSettings().getOrThrow("hydraulic-fluid-per-block", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            int diameter = 2 * radius + 1;
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("mining-area", diameter + "x" + diameter + "x" + diameter),
                    RebarArgument.of("hydraulic-fluid-per-block", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerBlock)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    public ItemStackBuilder topStack = ItemStackBuilder.of(Material.YELLOW_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":top");

    public VirtualInventory toolInventory = new VirtualInventory(1);

    @SuppressWarnings("unused")
    public HydraulicMiner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

        addEntity("top1", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.199, 0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top2", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.2, 0.6)
                        .rotate(0, Math.PI / 4, 0))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);

        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicMiner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        super.postInitialise();
        updateMiner();
        createLogisticGroup("input", LogisticGroupType.INPUT, toolInventory);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
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
            event.getPlayer().give(tool);
            toolInventory.setItem(new MachineUpdateReason(), 0, null);
            stopProcess();
            return;
        }

        // insert new item
        ItemStack newStack = event.getItem();
        if (newStack != null) {
            toolInventory.setItem(new MachineUpdateReason(), 0, newStack.asOne());
            newStack.subtract();
            updateMiner();
        }
    }

    @Override
    public void tick() {
        if (isProcessing()) {
            progressProcess(tickInterval);
        }
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

    @Override
    public void onProcessFinished() {
        Block block = blockPositions.get(index).getBlock();
        List<ItemStack> drops = block.getDrops().stream().toList();
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || !new BlockBreakBlockEvent(block, getBlock(), drops).callEvent()
        ) {
            return;
        }

        block.breakNaturally();
        tool.setData(DataComponentTypes.DAMAGE, tool.getData(DataComponentTypes.DAMAGE) + 1);
        toolInventory.setItem(new MachineUpdateReason(), 0, tool);
        if (Objects.equals(tool.getData(DataComponentTypes.DAMAGE), tool.getData(DataComponentTypes.MAX_DAMAGE))) {
            toolInventory.setItem(new MachineUpdateReason(), 0, null);
        }
        removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerBlock);
        addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerBlock);
        updateMiner();
    }

    @Override
    protected @Nullable Integer getBreakTicks(@NotNull Block block) {
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerBlock
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidPerBlock
        ) {
            return null;
        }
        return (int) Math.round(RebarUtils.getBlockBreakTicks(tool, block) / speed);
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidAdded(fluid, amount);
        updateMiner();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidRemoved(fluid, amount);
        updateMiner();
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBufferBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "tool", toolInventory
        );
    }
}
