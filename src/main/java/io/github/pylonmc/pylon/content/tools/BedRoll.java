package io.github.pylonmc.pylon.content.tools;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import kotlin.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class BedRoll extends RebarItem implements RebarBlockInteractor {
    public static final Map<UUID, Pair<Block, Block>> SLEEP_MAP = new HashMap<>();
    public static final HashSet<Location> BLOCKS = new HashSet<>();

    public BedRoll(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (!event.getAction().isRightClick()
            || clicked == null
            || event.useItemInHand() == Event.Result.DENY
            || player.isSneaking()) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        World world = player.getWorld();
        if (world.getEnvironment() == World.Environment.NETHER || world.getEnvironment() == World.Environment.THE_END) {
            player.getInventory().getItemInMainHand().subtract();
            world.createExplosion(clicked.getLocation(), 5f, true, true);
            return;
        }

        if (!world.isBedWorks()) {
            return; // we cannot sleep
        }

        UUID playerId = player.getUniqueId();
        if (SLEEP_MAP.containsKey(playerId)) {
            return; // maybe error message
        }

        Block baseBed = clicked.getRelative(BlockFace.UP);
        Block otherBed = baseBed.getRelative(player.getFacing());
        if (!baseBed.isEmpty() || !otherBed.isEmpty()) {
            return; // maybe error message
        }

        baseBed.setBlockData(
            Bukkit.createBlockData(Material.RED_BED, (bd) -> {
                Bed bed = (Bed) bd;
                bed.setPart(Bed.Part.FOOT);
            })
        );

        otherBed.setBlockData(
            Bukkit.createBlockData(Material.RED_BED, (bd) -> {
                Bed bed = (Bed) bd;
                bed.setPart(Bed.Part.HEAD);
            })
        );

        Location bedLocation = baseBed.getLocation();
        player.teleport(bedLocation);

        // put before just in case the event doesn't pick it up
        SLEEP_MAP.put(playerId, new Pair<>(baseBed, otherBed));
        BLOCKS.add(baseBed.getLocation());
        BLOCKS.add(otherBed.getLocation());
        if (!player.sleep(bedLocation, true)) {
            SLEEP_MAP.remove(playerId);
            BLOCKS.remove(baseBed.getLocation());
            BLOCKS.remove(otherBed.getLocation());
        }
    }

    public static class BedRollListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void changeRespawn(PlayerSetSpawnEvent event) {
            if (event.getCause() != PlayerSetSpawnEvent.Cause.BED) return;
            if (SLEEP_MAP.containsKey(event.getPlayer().getUniqueId())) event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void sleepCompleted(TimeSkipEvent event) {
            if (event.getSkipReason() != TimeSkipEvent.SkipReason.NIGHT_SKIP) return;
            SLEEP_MAP.forEach((k, v) -> {
                v.getFirst().setType(Material.AIR, false);
                v.getSecond().setType(Material.AIR, false);
            });
            SLEEP_MAP.clear();
            BLOCKS.clear();
        }

        @EventHandler(ignoreCancelled = true)
        public void preventBedRollBreak(BlockBreakEvent event) {
            if (BLOCKS.contains(event.getBlock().getLocation())) event.setCancelled(true);
        }
    }
}
