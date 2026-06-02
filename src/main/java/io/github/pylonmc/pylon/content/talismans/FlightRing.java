package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.item.interfaces.JoinRebarItemHandler;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FlightRing extends Talisman implements JoinRebarItemHandler {
    private static final Set<UUID> cannotFlyPlayers = new HashSet<>();
    public FlightRing(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return PylonKeys.FLIGHT_RING;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        if (!player.getAllowFlight()) {
            cannotFlyPlayers.add(player.getUniqueId());
            player.setAllowFlight(true);
        }
        player.setFlying(true);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);

        var gm = player.getGameMode();
        if (gm != GameMode.CREATIVE && gm != GameMode.SPECTATOR) {
            if (cannotFlyPlayers.contains(player.getUniqueId())) {
                player.setAllowFlight(false);
                cannotFlyPlayers.remove(player.getUniqueId());
            }
        }
    }

    @Override
    public void onJoin(@NotNull PlayerJoinEvent event, @NotNull EventPriority priority) {
        if (!event.getPlayer().getPersistentDataContainer().has(PylonKeys.FLIGHT_RING)) {
            return;
        }

        applyEffect(event.getPlayer());
    }
}
