package io.github.pylonmc.pylon.content.talismans;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class ExperienceTalisman extends Talisman {
    public static final NamespacedKey XP_TALISMAN_KEY = pylonKey("xp_talisman");
    public static final NamespacedKey XP_MULTIPLIER_KEY = pylonKey("xp_talisman_multiplier");
    public final float xpMultiplier = getSettings().getOrThrow("xp-multiplier", ConfigAdapter.FLOAT);

    public ExperienceTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return XP_TALISMAN_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(XP_MULTIPLIER_KEY, PersistentDataType.FLOAT, xpMultiplier);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(XP_MULTIPLIER_KEY);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("xp-boost", UnitFormat.PERCENT.format((xpMultiplier - 1) * 100).decimalPlaces(0)));
    }

    public static class XPTalismanListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerGainXP(PlayerPickupExperienceEvent event){
            Float xpMultiplier = event.getPlayer().getPersistentDataContainer().get(XP_MULTIPLIER_KEY, PersistentDataType.FLOAT);
            if (xpMultiplier == null){
                return;
            }
            event.getExperienceOrb().setExperience(
                    Math.round(event.getExperienceOrb().getExperience() * xpMultiplier)
            );
            Player player = event.getPlayer();
            player.playSound(PylonConfig.EXPERIENCE_TALISMAN_TRIGGER_SOUND.create(), player);
        }
    }
}
