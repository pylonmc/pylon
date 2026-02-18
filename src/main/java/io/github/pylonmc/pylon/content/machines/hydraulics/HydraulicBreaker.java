package io.github.pylonmc.pylon.content.machines.hydraulics;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.*;


public class HydraulicBreaker extends RebarBlock implements
        RebarFluidBufferBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarMultiblock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarLogisticBlock,
        RebarProcessor {

    public final double hydraulicFluidPerBlock = getSettings().getOrThrow("hydraulic-fluid-per-block", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double hydraulicFluidPerBlock = getSettings().getOrThrow("hydraulic-fluid-per-block", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

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

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
        ) {
            return;
        }

        event.setCancelled(true);

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
        new ParticleBuilder(Particle.BLOCK)
                .count(5)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.6, 0))
                .data(drilling.getBlockData())
                .spawn();
    }

    @Override
    public @NotNull Gui createGui() {
        // Not actually used, just provided for easy inventory serialization
        return Gui.builder()
                .setStructure("# # # # x # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', toolInventory)
                .build();
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
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(toDrill, tool)
                || !new BlockBreakBlockEvent(toDrill, getBlock(), new ArrayList<>()).callEvent()
        ) {
            return;
        }

        toDrill.breakNaturally();
        tool.setData(DataComponentTypes.DAMAGE, tool.getData(DataComponentTypes.DAMAGE) + 1);
        toolInventory.setItem(new MachineUpdateReason(), 0, tool);
        if (Objects.equals(tool.getData(DataComponentTypes.DAMAGE), tool.getData(DataComponentTypes.MAX_DAMAGE))) {
            toolInventory.setItem(new MachineUpdateReason(), 0, null);
        }
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
            return;
        }
        tryStartDrilling();
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidAdded(fluid, amount);
        tryStartDrilling();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidRemoved(fluid, amount);
        tryStartDrilling();
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
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBufferBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("tool", toolInventory);
    }
}
