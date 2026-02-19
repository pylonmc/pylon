package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class ManualCoreDrillLever extends RebarBlock implements RebarInteractBlock {

    private BukkitTask leverResetTask;

    @SuppressWarnings("unused")
    public ManualCoreDrillLever(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public ManualCoreDrillLever(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.getPlayer().isSneaking() || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        if (!(getBlock().getBlockData() instanceof Switch blockData)) {
            throw new IllegalStateException("Block data is not switch");
        }

        ManualCoreDrill drill = BlockStorage.getAs(
                ManualCoreDrill.class,
                getBlock().getRelative(blockData.getFacing().getOppositeFace())
        );
        if (drill == null || drill.isProcessing()) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            if (blockData.isPowered()) {
                event.setUseInteractedBlock(Event.Result.DENY);
            }
            return;
        }

        drill.cycle();

        if (leverResetTask != null) {
            leverResetTask.cancel();
        }

        scheduleBlockTextureItemRefresh();
        leverResetTask = Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            if (getBlock().getBlockData() instanceof Switch switchData) {
                switchData.setPowered(false);
                getBlock().setBlockData(switchData);
                refreshBlockTextureItem();
            }
        }, (long) drill.getRotationDuration() * drill.getRotationsPerCycle());
    }
}
