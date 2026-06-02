package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import java.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class GenericQuarry extends RebarBlock implements
        RebarMultiblock,
        ProcessorRebarBlock,
        TickingRebarBlock,
        GuiRebarBlock,
        VirtualInventoryRebarBlock,
        LogisticRebarBlock,
        DirectionalRebarBlock {

    public static final NamespacedKey INDEX_KEY = pylonKey("index");
    public static final NamespacedKey BLOCK_POSITIONS_KEY = pylonKey("block_positions");
    public static final NamespacedKey CHUNK_POSITIONS_KEY = pylonKey("chunk_positions");

    public final int radius = getSettingOrThrow("radius", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

    protected final List<BlockPosition> blockPositions;
    protected final Set<ChunkPosition> chunkPositions;
    public VirtualInventory toolInventory = new VirtualInventory(1);
    public VirtualInventory outputInventory = new VirtualInventory(3);
    protected int index;

    @SuppressWarnings("unused")
    public GenericQuarry(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());

        WorldBorder border = block.getWorld().getWorldBorder();
        index = 0;
        blockPositions = new ArrayList<>();
        chunkPositions = new HashSet<>();
        for (int y = block.getWorld().getMaxHeight() - block.getY(); y >= block.getWorld().getMinHeight() - block.getY(); y--) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block neighbour = getBlock().getRelative(x, y, z);
                    if (border.isInside(neighbour.getLocation())) {
                        BlockPosition blockPosition = new BlockPosition(neighbour);
                        blockPositions.add(blockPosition);
                        chunkPositions.add(blockPosition.getChunk());
                    }
                }
            }
        }
    }

    @SuppressWarnings({"DataFlowIssue", "unused"})
    public GenericQuarry(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        index = pdc.get(INDEX_KEY, RebarSerializers.INTEGER);
        blockPositions = pdc.get(BLOCK_POSITIONS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION));
        chunkPositions = pdc.get(CHUNK_POSITIONS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.CHUNK_POSITION));
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(INDEX_KEY, RebarSerializers.INTEGER, index);
        pdc.set(BLOCK_POSITIONS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.BLOCK_POSITION), blockPositions);
        pdc.set(CHUNK_POSITIONS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.CHUNK_POSITION), chunkPositions);
    }

    private boolean checkBlocks() {
        while (index < blockPositions.size() - 1) {
            BlockPosition position = blockPositions.get(index);
            if (!position.getChunk().isLoaded()) {
                index++;
                continue;
            }

            Integer breakTicks = getBreakTicks(position.getBlock());
            if (breakTicks == null) {
                index++;
                continue;
            }

            startProcess(breakTicks);
            return false;
        }
        index = 0;
        return true;
    }

    protected void updateQuarry() {
        if (checkBlocks()) {
            // Finished mining; do one more pass in case something has changed in the meantime
            checkBlocks();
        }
    }

    @Override
    public @NotNull Set<ChunkPosition> getChunksOccupied() {
        return chunkPositions;
    }

    @Override
    public boolean checkFormed() {
        return true;
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        Vector relative = getBlock().getLocation().subtract(otherBlock.getLocation()).toVector();
        return Math.abs(relative.getBlockX()) <= radius
                && Math.abs(relative.getBlockZ()) <= radius
                && Math.abs(relative.getBlockY()) <= radius;
    }

    @Override
    public void onMultiblockRefreshed() {
        updateQuarry();
    }

    @Override
    public void postInitialise() {
        super.postInitialise();
        createLogisticGroup("tool", LogisticGroupType.INPUT, toolInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        toolInventory.addPostUpdateHandler(event -> {
            stopProcess();
            updateQuarry();
        });
        outputInventory.addPostUpdateHandler(event -> updateQuarry());
        updateQuarry();
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # T # O O O # #",
                        "# # t # o o o # #",
                        "# # T # O O O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('t', toolInventory)
                .addIngredient('T', ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":tool")
                        .name(Component.translatable("pylon.gui.tool")))
                .addIngredient('o', outputInventory)
                .addIngredient('O', GuiItems.output())
                .build();
    }

    @Override
    public void onProcessFinished() {
        Block block = blockPositions.get(index).getBlock();
        ItemStack tool = toolInventory.getItem(0);
        List<ItemStack> drops = block.getDrops().stream().toList();
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || !new BlockBreakBlockEvent(block, getBlock(), drops).callEvent()
                || !outputInventory.canHold(drops)
        ) {
            return;
        }

        for (ItemStack drop : drops) {
            outputInventory.addItem(new MachineUpdateReason(), drop);
        }
        block.setType(Material.AIR);
        RebarUtils.damageItem(tool, 1, block.getWorld());
        toolInventory.setItem(new MachineUpdateReason(), 0, tool);
        onBreakBlock();
        updateQuarry();
    }

    protected void onBreakBlock() {
    }

    protected abstract boolean canBreakBlock();

    private @Nullable Integer getBreakTicks(@NotNull Block block) {
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || !outputInventory.canHold(block.getDrops().stream().toList())
                || !canBreakBlock()
        ) {
            return null;
        }
        return (int) Math.round(RebarUtils.getBlockBreakTicks(tool, block) / speed);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "tool", toolInventory,
                "output", outputInventory
        );
    }
}
