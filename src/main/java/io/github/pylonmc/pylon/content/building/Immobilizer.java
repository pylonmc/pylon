package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarBreakHandler;
import io.github.pylonmc.rebar.block.base.RebarPiston;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Immobilizer extends RebarBlock implements RebarPiston, RebarBreakHandler {
    public static HashMap<BlockPosition, Set<Player>> frozenPlayers = new HashMap<>();
    private final NamespacedKey cooldownKey = pylonKey("immobilizer_cooldown_millis");
    private final double radius = getSettings().getOrThrow("radius", ConfigAdapter.DOUBLE);
    private final int duration = getSettings().getOrThrow("duration", ConfigAdapter.INTEGER);
    private final long cooldownMillis = getSettings().getOrThrow("cooldown", ConfigAdapter.INTEGER) * 50L;
    private final int particleCount = getSettings().getOrThrow("particle.count", ConfigAdapter.INTEGER);
    private final double particleRadius = getSettings().getOrThrow("particle.radius", ConfigAdapter.DOUBLE);
    private final int particlePeriod = getSettings().getOrThrow("particle.period", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {
        private final double radius = getSettings().getOrThrow("radius", ConfigAdapter.DOUBLE);
        private final int duration = getSettings().getOrThrow("duration", ConfigAdapter.INTEGER);
        private final int cooldown = getSettings().getOrThrow("cooldown", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("duration", UnitFormat.formatDuration(Duration.ofSeconds(duration / 20))),
                    RebarArgument.of("radius", UnitFormat.BLOCKS.format(radius)),
                    RebarArgument.of("cooldown", UnitFormat.formatDuration(Duration.ofMillis(cooldown * 50L)))
            );
        }
    }

    public Immobilizer(Block block, BlockCreateContext context) {
        super(block);
    }

    public Immobilizer(Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void onExtend(@NotNull BlockPistonExtendEvent event) {
        event.setCancelled(true);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        BlockPosition position = new BlockPosition(event.getBlock());

        for (Player player : event.getBlock().getLocation().getNearbyPlayers(radius)) {
            PersistentDataContainer playerPdc = player.getPersistentDataContainer();

            long now = System.currentTimeMillis();
            if (playerPdc.has(cooldownKey, PersistentDataType.LONG)) {
                long lastUsed = playerPdc.get(cooldownKey, PersistentDataType.LONG);
                if (now - lastUsed < cooldownMillis) {
                    continue;
                }
            }

            Set<Player> players = frozenPlayers.getOrDefault(position, new HashSet<>());
            players.add(player);
            frozenPlayers.put(position, players);

            int times = duration / particlePeriod;
            new PlayerVFX(player, this, times).runTaskTimer(Pylon.getInstance(),0, particlePeriod);

            playerPdc.set(cooldownKey, PersistentDataType.LONG, now);
        }

        scheduler.runTaskLater(Pylon.getInstance(), () -> frozenPlayers.clear(), duration);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        BlockPosition position = new BlockPosition(context.getBlock());
        frozenPlayers.remove(position);
    }

    public static class PlayerVFX extends BukkitRunnable {
        private final Player player;
        private final Immobilizer block;
        private final BlockPosition pos;
        private final int duration;
        private int tick = 0;

        public PlayerVFX(Player player, Immobilizer block, int duration) {
            this.player = player;
            this.block = block;
            this.pos = new BlockPosition(block.getBlock());
            this.duration = duration;
        }

        @Override
        public void run() {
            // completed
            if (++tick >= duration) {
                cancel();
                return;
            }

            // block is gone
            if (frozenPlayers.get(pos) == null) {
                cancel();
                return;
            }

            player.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), block.particleCount,
                    block.particleRadius, block.particleRadius, block.particleRadius);
        }
    }

    public static class FreezeListener implements Listener {

        @EventHandler
        void onPlayerMove(PlayerMoveEvent event) {
            // There is some rubber-banding with this approach, but Player.setWalk/FlySpeed does not account for jumping
            if (!event.hasExplicitlyChangedPosition()) {
                return;
            }

            for (Set<Player> playerSet : frozenPlayers.values()) {
                if (playerSet.contains(event.getPlayer())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
