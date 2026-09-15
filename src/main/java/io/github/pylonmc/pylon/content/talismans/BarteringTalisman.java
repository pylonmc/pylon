package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class BarteringTalisman extends Talisman {
    public static final NamespacedKey BARTERING_TALISMAN_KEY = pylonKey("bartering_talisman");
    public static final NamespacedKey BARTERING_TALISMAN_NO_CONSUME_KEY = pylonKey("bartering_talisman_no_consume_chance");

    public final float chanceToNotConsumeInput = getSettingOrThrow("chance-to-not-consume-input", ConfigAdapter.FLOAT);

    public BarteringTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("chance_to_not_consume_input", UnitFormat.PERCENT.format(chanceToNotConsumeInput * 100).decimalPlaces(2))
        );
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return BARTERING_TALISMAN_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(BARTERING_TALISMAN_NO_CONSUME_KEY, PersistentDataType.FLOAT, chanceToNotConsumeInput);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(BARTERING_TALISMAN_NO_CONSUME_KEY);
    }

    public static final class BarteringTalismanListener implements Listener {
        private final Map<UUID, UUID> barteringPlayerCache = new HashMap<>();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarter(PiglinBarterEvent event) {
            Piglin piglin = event.getEntity();
            UUID lastBartererId = barteringPlayerCache.remove(piglin.getUniqueId());
            Player lastBarterer = lastBartererId != null ? Bukkit.getPlayer(lastBartererId) : null;
            Player player;
            if (lastBarterer != null && lastBarterer.getWorld() == piglin.getWorld() && lastBarterer.getLocation().distanceSquared(piglin.getLocation()) < 15 * 15) {
                player = lastBarterer;
            } else {
                Collection<Player> nearbyPlayers = piglin.getWorld().getNearbyPlayers(piglin.getLocation(), 15, 5, 15);
                if (nearbyPlayers.isEmpty()) {
                    return;
                }
                player = nearbyPlayers.iterator().next();
            }
            barteringPlayerCache.put(piglin.getUniqueId(), player.getUniqueId());

            Float chance = player.getPersistentDataContainer().get(BARTERING_TALISMAN_NO_CONSUME_KEY, PersistentDataType.FLOAT);
            if (chance == null || ThreadLocalRandom.current().nextFloat() > chance) {
                return;
            }

            piglin.getWorld().dropItemNaturally(piglin.getLocation(), event.getInput());
            PylonConfig.BARTERING_TALISMAN_TRIGGER_SOUND.playFrom(piglin);
        }
    }
}
