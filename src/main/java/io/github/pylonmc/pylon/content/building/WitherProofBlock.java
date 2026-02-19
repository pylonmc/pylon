package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityChangedBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class WitherProofBlock extends RebarBlock implements RebarEntityChangedBlock {
    public WitherProofBlock(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public WitherProofBlock(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChanged(@NotNull EntityChangeBlockEvent event, @NotNull EventPriority priority) {
        if (event.getEntity() instanceof Wither) {
            event.setCancelled(true);
        }
    }
}
