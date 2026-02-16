package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

import static java.lang.Math.pow;

public class ClimbingPick extends RebarItem implements RebarBlockInteractor {
    private static final NamespacedKey HOOKED_KEY = PylonUtils.pylonKey("climbing_pick_hooked");
    private final double jumpSpeed = getSettings().getOrThrow("jump-speed", ConfigAdapter.DOUBLE);
    private final double hookRange = getSettings().getOrThrow("hook-range", ConfigAdapter.DOUBLE);
    private final double hookRangeSquared = pow(hookRange, 2);

    public ClimbingPick(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick()
                || (event.getPlayer().isSneaking() && event.useInteractedBlock() == Event.Result.ALLOW)
                || event.getPlayer().getPersistentDataContainer().has(HOOKED_KEY)
                || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        double distSquared = event.getClickedBlock().getLocation().clone().subtract(event.getPlayer().getEyeLocation()).toVector().toVector3f().lengthSquared();
        if (distSquared < hookRangeSquared) {
            PlayerJumpListener listener = new PlayerJumpListener(event.getPlayer(), (float) jumpSpeed);
            Bukkit.getPluginManager().registerEvents(listener, Pylon.getInstance());
        }
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("jump-speed", UnitFormat.BLOCKS_PER_SECOND.format(jumpSpeed)),
                RebarArgument.of("hook-range", UnitFormat.BLOCKS.format(hookRange))
        );
    }

    public static class PlayerJumpListener implements Listener {
        private final Player player;
        private final float jumpSpeed;
        private final boolean isAllowedFlight;
        private final boolean hasGravity;

        public PlayerJumpListener(Player player, float jumpSpeed) {
            this.player = player;
            this.jumpSpeed = jumpSpeed;
            this.isAllowedFlight = player.getAllowFlight();
            this.hasGravity = player.hasGravity();
            player.setGravity(false);
            player.setAllowFlight(true);
            player.getPersistentDataContainer().set(HOOKED_KEY, PersistentDataType.BOOLEAN, true);
        }

        @EventHandler
        public void onMove(PlayerMoveEvent event) {
            if (event.getPlayer() == player && event.hasExplicitlyChangedPosition() && !event.getPlayer().isJumping()) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onStartFly(PlayerToggleFlightEvent event) {
            if (event.getPlayer() == player) {
                event.setCancelled(true);
                // Trigger a "jump"
                player.setVelocity(Vector.fromJOML(new Vector3f(0.0f, jumpSpeed, 0.0f)));
                dispose();
            }
        }

        @EventHandler
        public void onSneak(PlayerToggleSneakEvent event) {
            if (event.getPlayer() == player && event.isSneaking()) {
                dispose();
            }
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent event) {
            if (event.getPlayer() == player) {
                dispose();
            }
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin() == Pylon.getInstance()) {
                dispose();
            }
        }

        private void dispose() {
            player.setGravity(hasGravity);
            player.setAllowFlight(isAllowedFlight);
            player.getPersistentDataContainer().remove(HOOKED_KEY);
            PlayerMoveEvent.getHandlerList().unregister(this);
            PlayerToggleSneakEvent.getHandlerList().unregister(this);
            PlayerQuitEvent.getHandlerList().unregister(this);
            PlayerToggleFlightEvent.getHandlerList().unregister(this);
            PluginDisableEvent.getHandlerList().unregister(this);
        }
    }
}
