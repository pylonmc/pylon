package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HuntingTalisman extends Talisman {
    public final double chanceForExtraItem = getSettings().getOrThrow("chance-for-extra-item", ConfigAdapter.DOUBLE);
    public static final NamespacedKey HUNTING_TALISMAN_KEY = PylonUtils.pylonKey("hunting_talisman");
    public static final NamespacedKey HUNTING_TALISMAN_BONUS_KEY = PylonUtils.pylonKey("hunting_talisman_bonus");

    public HuntingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }


    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("bonus_item_chance", UnitFormat.PERCENT.format(chanceForExtraItem * 100).decimalPlaces(2))
        );
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return HUNTING_TALISMAN_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(HUNTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE, chanceForExtraItem);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(HUNTING_TALISMAN_BONUS_KEY);
    }

    public static final class HuntingTalismanListener implements Listener {

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (!(event.getDamageSource().getCausingEntity() instanceof Player player)) {
                return;
            }
            if (!player.getPersistentDataContainer().has(HUNTING_TALISMAN_BONUS_KEY)) {
                return;
            }
            if (event.getEntity().getType() == EntityType.PLAYER) {
                return;
            }
            @SuppressWarnings("DataFlowIssue")
            double chanceForExtraItem = player.getPersistentDataContainer().get(HUNTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE);
            for (ItemStack drop : event.getDrops()) {
                if (drop.getItemMeta().hasRarity() && drop.getItemMeta().getRarity().ordinal() >= ItemRarity.RARE.ordinal()) {
                    continue;
                }
                if (ThreadLocalRandom.current().nextDouble() > chanceForExtraItem) {
                    continue;
                }
                drop.setAmount(drop.getAmount() + 1);
                Entity source = event.getEntity();
                source.getWorld().playSound(PylonConfig.HUNTING_TALISMAN_TRIGGER_SOUND.create(), source);
            }
        }
    }
}
