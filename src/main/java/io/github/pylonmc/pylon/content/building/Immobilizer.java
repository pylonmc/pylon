package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.PistonRebarBlockHandler;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Immobilizer extends RebarBlock implements PistonRebarBlockHandler, BlockBreakRebarBlockHandler {
    private static final Map<BlockPosition, Set<UUID>> FROZEN_PLAYERS = new HashMap<>();
    private static final Map<UUID, Long> FREEZE_TIMES = new HashMap<>();

    private final double radius = getSettingOrThrow("radius", ConfigAdapter.DOUBLE);
    private final int duration = getSettingOrThrow("duration", ConfigAdapter.INTEGER);
    private final long cooldown = getSettingOrThrow("cooldown", ConfigAdapter.INTEGER);

    private final int particleCount = getSettingOrThrow("particle.count", ConfigAdapter.INTEGER);
    private final double particleRadius = getSettingOrThrow("particle.radius", ConfigAdapter.DOUBLE);
    private final int particlePeriod = getSettingOrThrow("particle.period", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {
        private final double radius = getSettingOrThrow("radius", ConfigAdapter.DOUBLE);
        private final int duration = getSettingOrThrow("duration", ConfigAdapter.INTEGER);
        private final int cooldown = getSettingOrThrow("cooldown", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("duration", UnitFormat.formatDuration(Tick.of(duration))),
                    RebarArgument.of("radius", UnitFormat.BLOCKS.format(radius)),
                    RebarArgument.of("cooldown", UnitFormat.formatDuration(Tick.of(cooldown)))
            );
        }
    }

    @SuppressWarnings("unused")
    public Immobilizer(Block block, BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public Immobilizer(Block block, PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(@NotNull BlockPistonExtendEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);

        long now = Bukkit.getCurrentTick();
        BlockPosition position = new BlockPosition(getBlock());
        for (Player player : getBlock().getLocation().getNearbyPlayers(radius)) {
            UUID playerId = player.getUniqueId();
            long freezeTime = FREEZE_TIMES.getOrDefault(playerId, 0L);
            if (freezeTime != 0 && freezeTime + cooldown > now) {
                continue;
            }

            FREEZE_TIMES.put(playerId, now);
            FROZEN_PLAYERS.computeIfAbsent(position, _ -> new HashSet<>()).add(playerId);
            new ImmobilizeEffect(playerId, position, particleRadius, particleCount, duration / particlePeriod)
                    .runTaskTimer(Pylon.getInstance(),0, particlePeriod);
        }

        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), Immobilizer::checkFrozenPlayers, duration + 1);
    }

    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        FROZEN_PLAYERS.remove(new BlockPosition(getBlock()));
    }

    private static void checkFrozenPlayers() {
        long now = Bukkit.getCurrentTick();
        for (Map.Entry<BlockPosition, Set<UUID>> entry: FROZEN_PLAYERS.entrySet()) {
            Immobilizer immobilizer = BlockStorage.getAs(Immobilizer.class, entry.getKey());
            if (immobilizer == null) {
                continue;
            }

            entry.getValue().removeIf(playerId -> {
                long freezeTime = FREEZE_TIMES.getOrDefault(playerId, 0L);
                return freezeTime == 0 || now > freezeTime + immobilizer.duration;
            });
        }
    }

    private static class ImmobilizeEffect extends BukkitRunnable {
        private final UUID playerId;
        private final BlockPosition block;
        private final double particleRadius;
        private final int particleCount;

        private final int duration;
        private int tick = 0;

        public ImmobilizeEffect(UUID playerId, BlockPosition block, double particleRadius, int particleCount, int duration) {
            this.playerId = playerId;
            this.block = block;
            this.particleRadius = particleRadius;
            this.particleCount = particleCount;
            this.duration = duration;
        }

        @Override
        public void run() {
            // completed
            if (++tick >= duration) {
                cancel();
                return;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                cancel();
                return;
            }

            // block is gone
            Set<UUID> playerIds = FROZEN_PLAYERS.get(block);
            if (playerIds == null || !playerIds.contains(playerId)) {
                cancel();
                return;
            }

            player.spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation(), particleCount, particleRadius, particleRadius, particleRadius);
        }
    }

    public static class FreezeListener implements Listener {

        @EventHandler
        void onPlayerMove(PlayerMoveEvent event) {
            // There is some rubber-banding with this approach, but Player.setWalk/FlySpeed does not account for jumping
            if (!event.hasExplicitlyChangedPosition()) {
                return;
            }

            for (Set<UUID> playerIds : FROZEN_PLAYERS.values()) {
                if (playerIds.contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
