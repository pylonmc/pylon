package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class EnchantingTalisman extends Talisman {
    private static final NamespacedKey ENCHANTING_TALISMAN_BONUS_KEY = pylonKey("enchanting_talisman_bonus");

    public static final NamespacedKey ENCHANTING_TALISMAN_KEY = pylonKey("enchanting_talisman");

    public final double bonusLevelChance = getSettingOrThrow("bonus-level-chance", ConfigAdapter.DOUBLE);

    public EnchantingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("bonus_level_chance", UnitFormat.PERCENT.format(bonusLevelChance * 100).decimalPlaces(2))
        );
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return ENCHANTING_TALISMAN_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(ENCHANTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE, bonusLevelChance);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(ENCHANTING_TALISMAN_BONUS_KEY);
    }

    public static class EnchantingListener implements Listener {
        @EventHandler
        public void onEnchant(EnchantItemEvent event) {
            Double bonusLevelChance = event.getEnchanter().getPersistentDataContainer().get(ENCHANTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE);
            if (bonusLevelChance == null) {
                return;
            }

            boolean triggered = false;
            for (Map.Entry<Enchantment, Integer> enchant : event.getEnchantsToAdd().entrySet()) {
                if (enchant.getValue() >= enchant.getKey().getMaxLevel() || Math.random() > bonusLevelChance) {
                    return;
                }

                enchant.setValue(enchant.getValue() + 1);
                triggered = true;
            }

            if (triggered) {
                PylonConfig.ENCHANTING_TALISMAN_TRIGGER_SOUND.playFrom(event.getEnchanter());
            }
        }
    }
}
