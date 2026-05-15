package io.github.pylonmc.pylon.content;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFire;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class TestFireBlock extends RebarBlock implements RebarFire {
    public TestFireBlock(@NotNull final Block block, @NotNull final BlockCreateContext context) {
        super(block, context);
    }

    public TestFireBlock(@NotNull final Block block, @NotNull final PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onDamageEntity(@NotNull final EntityDamageEvent event, @NotNull final EventPriority priority) {
        Pylon.getInstance().getLogger().info("Fire " + location() +  " called onDamageEntity");
    }

    @Override
    public void onFireSpawn(@NotNull final Block fire) {
        Pylon.getInstance().getLogger().info("Fire " + location() +  " called onFireSpawn");
    }

    @Override
    public boolean onFireEatBlock(@NotNull final Block block) {
        Pylon.getInstance().getLogger().info("Fire " + location() +  " called onFireEatBlock");
        return RebarFire.super.onFireEatBlock(block);
    }

    @Override
    public void onFireSpread(@NotNull final BlockSpreadEvent event, @NotNull final EventPriority priority) {
        Pylon.getInstance().getLogger().info("Fire " + location() +  " called onFireSpread");
    }

    @Override
    public void onIgniteBlock(@NotNull final BlockIgniteEvent event, @NotNull final EventPriority priority) {
        Pylon.getInstance().getLogger().info("Fire " + location() +  " called onIgniteBlock");
    }

    @NotNull
    private String location() {
        Location loc = getBlock().getLocation();
        return "[" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "]";
    }
}
