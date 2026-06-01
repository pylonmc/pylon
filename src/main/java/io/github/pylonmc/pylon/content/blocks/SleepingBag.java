package io.github.pylonmc.pylon.content.blocks;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.BedRebarBlockHandler;
import io.github.pylonmc.rebar.event.PreRebarBlockPlaceEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class SleepingBag extends RebarBlock implements BedRebarBlockHandler {
    private final Player player;

    public SleepingBag(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        this.player = context.getPlayer();
    }

    public SleepingBag(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        this.player = null;
    }

    @Override
    public void postInitialise() {
        // At the time of the creation constructor only one half of the bed will be placed & the player won't have received
        // confirmation it has been placed yet, so we need to let it finish placing and then attempt to sleep, hence the tick delay
        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            Block bedHead = getBlock();
            if (bedHead.getBlockData() instanceof Bed bedData && bedData.getPart() != Bed.Part.HEAD) {
                bedHead = bedHead.getRelative(bedData.getFacing());
            }

            if (!player.sleep(bedHead.getLocation(), false)) {
                scheduleBreak();
            }
        });
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeaveBed(@NotNull PlayerBedLeaveEvent event, @NotNull EventPriority priority) {
        scheduleBreak();
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSetSpawn(@NotNull PlayerSetSpawnEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    private void scheduleBreak() {
        // We need to let the bed leave event finish processing before breaking the block
        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            if (player != null) {
                player.breakBlock(getBlock());
            }
        });
    }

    public static class PlaceListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void preRebarPlace(PreRebarBlockPlaceEvent event) {
            if (!event.getBlockSchema().isType(SleepingBag.class)) return;
            if (event.getContext().getPlayer() == null || event.getBlock().getWorld().isDayTime()) {
                event.setCancelled(true);
            }
        }

    }
}
