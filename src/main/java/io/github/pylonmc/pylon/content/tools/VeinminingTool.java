package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.BlockBreakRebarItemHandler;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class VeinminingTool extends RebarItem implements BlockBreakRebarItemHandler {
    private static final Set<UUID> VEIN_MINING = new HashSet<>();
    private static final Set<BlockBreakEvent> IGNORED_EVENTS = new HashSet<>();

    protected VeinminingTool(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    @MultiHandler(priorities = { EventPriority.LOWEST, EventPriority.MONITOR }, ignoreCancelled = true)
    public void onBreakBlock(@NotNull BlockBreakEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (VEIN_MINING.contains(playerId) || (disableWhileSneaking() && player.isSneaking())) {
            return;
        }

        Block root = event.getBlock();
        if (priority == EventPriority.LOWEST && (!canVeinmine(root) || (preventRebarBlocks() && BlockStorage.isRebarBlock(root)))) {
            IGNORED_EVENTS.add(event);
            return;
        } else if (IGNORED_EVENTS.remove(event)) {
            return;
        }

        VEIN_MINING.add(playerId);
        Set<BlockPosition> vein = new HashSet<>();
        Location rootLocation = root.getLocation();
        vein.add(new BlockPosition(root));
        for (Vector3i offset : getVeinminingOffsets()) {
            tryVeinMine(player, root, rootLocation, root.getRelative(offset.x, offset.y, offset.z), vein);
        }
        VEIN_MINING.remove(playerId);
    }

    public void tryVeinMine(Player player, Block root, Location rootLocation, Block block, Set<BlockPosition> vein) {
        if (cannotVeinMine(player, vein)) {
            return;
        }

        BlockPosition position = new BlockPosition(block);
        if (!isInVein(root, block) || (preventRebarBlocks() && BlockStorage.isRebarBlock(block)) || !vein.add(position)) {
            return;
        } else if (!player.breakBlock(block)) {
            vein.remove(position);
            return;
        } else if (cannotVeinMine(player, vein)) {
            return;
        }

        for (Vector3i offset : getWeightedOffsets(rootLocation)) {
            tryVeinMine(player, root, rootLocation, block.getRelative(offset.x, offset.y, offset.z), vein);
        }
    }

    private boolean cannotVeinMine(Player player, Set<BlockPosition> vein) {
        return vein.size() >= getMaxVeinSize() || getStack().isEmpty() || RebarUtils.hasOneDurabilityLeft(getStack()) || !getStack().equals(player.getInventory().getItemInMainHand());
    }

    private List<Vector3i> getWeightedOffsets(Location rootLocation) {
        List<Vector3i> faces = new ArrayList<>(getVeinminingOffsets());
        faces.sort(Comparator.comparingDouble(offset -> rootLocation.distanceSquared(rootLocation.clone().add(offset.x, offset.y, offset.z))));
        return faces;
    }

    public boolean disableWhileSneaking() {
        return true;
    }

    public abstract boolean canVeinmine(Block root);
    public boolean isInVein(Block root, Block block) {
        return root.getType() == block.getType();
    }

    public boolean preventRebarBlocks() {
        return true;
    }

    public abstract Collection<Vector3i> getVeinminingOffsets();
    public abstract int getMaxVeinSize();
}
