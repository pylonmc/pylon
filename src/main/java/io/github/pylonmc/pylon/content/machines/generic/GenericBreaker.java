package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public abstract class GenericBreaker extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarEntityHolderBlock,
        RebarTickingBlock,
        RebarMultiblock,
        RebarLogisticBlock,
        RebarProcessor {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

    private final ItemStackBuilder toolStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":tool")
            .name(Component.translatable("pylon.gui.tool"));

    protected final VirtualInventory toolInventory = new VirtualInventory(1);
    protected final VirtualInventory outputInventory = new VirtualInventory(1);

    public GenericBreaker(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing().getOppositeFace());
        setTickInterval(tickInterval);
    }

    public GenericBreaker(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("tool", LogisticGroupType.INPUT, toolInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        toolInventory.addPostUpdateHandler(event -> tryStartDrilling());
        outputInventory.addPostUpdateHandler(event -> tryStartDrilling());
        tryStartDrilling();
    }

    public void tryStartDrilling() {
        if (isProcessing()) {
            return;
        }

        Block toDrill = getBlock().getRelative(getFacing());
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null || !canStartDrilling(tool, toDrill)) {
            return;
        }

        startProcess((int) Math.round(RebarUtils.getBlockBreakTicks(tool, toDrill) / speed));
    }

    protected boolean canStartDrilling(ItemStack tool, Block block) {
        return !isProcessing()
                && block.getWorld().getWorldBorder().isInside(block.getLocation())
                && PylonUtils.shouldBreakBlockUsingTool(getBlock().getRelative(getFacing()), tool)
                && outputInventory.canHold(List.copyOf(block.getDrops()));
    }

    @Override
    public void onProcessFinished() {
        Block toDrill = getBlock().getRelative(getFacing());
        ItemStack tool = toolInventory.getItem(0);
        List<ItemStack> drops = toDrill.getDrops().stream().toList();
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(toDrill, tool)
                || !outputInventory.canHold(drops)
                || !new BlockBreakBlockEvent(toDrill, getBlock(), new ArrayList<>()).callEvent()
        ) {
            return;
        }

        toDrill.setType(Material.AIR);
        for (ItemStack drop : drops) {
            outputInventory.addItem(new MachineUpdateReason(), drop);
        }

        RebarUtils.damageItem(tool, 1, toDrill.getWorld());
        toolInventory.setItem(new MachineUpdateReason(), 0, tool);
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
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # T # O # # #",
                        "# # # t # o # # #",
                        "# # # T # O # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('t', toolInventory)
                .addIngredient('T', toolStack)
                .addIngredient('o', outputInventory)
                .addIngredient('O', GuiItems.output())
                .build();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "tool", toolInventory,
                "output", outputInventory
        );
    }
}
