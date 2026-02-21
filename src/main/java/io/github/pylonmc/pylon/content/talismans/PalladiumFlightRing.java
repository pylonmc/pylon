package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonKeys;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class PalladiumFlightRing extends Talisman {
    public PalladiumFlightRing(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return PylonKeys.PALLADIUM_FLIGHT_RING_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.setFlying(true);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);

        var gm = player.getGameMode();
        if (gm != GameMode.CREATIVE && gm != GameMode.SPECTATOR) {
            player.setFlying(false);
        }
    }
}
