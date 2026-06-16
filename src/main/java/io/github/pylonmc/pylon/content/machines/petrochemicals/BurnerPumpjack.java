package io.github.pylonmc.pylon.content.machines.petrochemicals;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.PistonRebarBlockHandler;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class BurnerPumpjack extends RebarBlock implements PistonRebarBlockHandler {

    public BurnerPumpjack(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public BurnerPumpjack(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }
}
