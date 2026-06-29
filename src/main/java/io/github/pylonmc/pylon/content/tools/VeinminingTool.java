package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.BlockBreakRebarItemHandler;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class VeinminingTool extends RebarItem implements BlockBreakRebarItemHandler {
    private static final Set<UUID> VEIN_MINING = new HashSet<>();
    private static final Set<BlockBreakEvent> IGNORED_EVENTS = new HashSet<>();

    public VeinminingTool(@NotNull ItemStack stack) {
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
        vein.add(new BlockPosition(root));
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            tryVeinMine(player, root, root.getRelative(face), face, vein);
        }
        VEIN_MINING.remove(playerId);
    }

    public void tryVeinMine(Player player, Block root, Block block, BlockFace sourceFace, Set<BlockPosition> vein) {
        if (vein.size() >= getMaxVeinSize() || getStack().isEmpty()|| RebarUtils.isBroken(getStack())) {
            return;
        }

        BlockPosition position = new BlockPosition(block);
        if (!vein.add(position) || !isInVein(root, block) || (preventRebarBlocks() && BlockStorage.isRebarBlock(block))) {
            return;
        } else if (!player.breakBlock(block)) {
            vein.remove(position);
            return;
        }

        for (BlockFace face : getWeightedFaces(root, block)) {
            if (face != sourceFace.getOppositeFace()) {
                tryVeinMine(player, root, block.getRelative(face), face, vein);
            }
        }
    }

    private List<BlockFace> getWeightedFaces(Block root, Block block) {
        List<BlockFace> faces = new ArrayList<>(Arrays.asList(RebarUtils.IMMEDIATE_FACES));
        Location rootLocation = root.getLocation();
        faces.sort(Comparator.comparingDouble(face -> rootLocation.distanceSquared(block.getRelative(face).getLocation())));
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

    public abstract int getMaxVeinSize();
}
