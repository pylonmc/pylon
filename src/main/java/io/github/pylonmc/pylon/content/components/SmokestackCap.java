package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFlowerPot;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class SmokestackCap extends RebarBlock implements RebarFlowerPot {

    public SmokestackCap(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public SmokestackCap(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onFlowerPotManipulated(@NotNull PlayerFlowerPotManipulateEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }
}
