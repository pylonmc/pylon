package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.EnchantmentView;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EnchantingTalisman extends Talisman {
    public final double bonusLevelChance = getSettings().getOrThrow("bonus-level-chance", ConfigAdapter.DOUBLE);
    public static final NamespacedKey ENCHANTING_TALISMAN_KEY = PylonUtils.pylonKey("enchanting_talisman");
    private static final NamespacedKey ENCHANTING_TALISMAN_BONUS_KEY = PylonUtils.pylonKey("enchanting_talisman_bonus");
    private static final NamespacedKey ENCHANTING_ITEM_UUID_KEY = PylonUtils.pylonKey("enchanting_talisman_uuid");

    public EnchantingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
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
        // This handler handles changing the visually seen enchant options
        @EventHandler
        public void onPreEnchant(PrepareItemEnchantEvent event) {
            Double bonusLevelChance = event.getEnchanter().getPersistentDataContainer().get(ENCHANTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE);
            // check talisman is equipped
            if (bonusLevelChance == null) {
                return;
            }
            // assign the item a uuid if it doesn't already have one, otherwise use its existing one
            UUID itemId;
            String uuidStr = event.getItem().getItemMeta().getPersistentDataContainer().get(ENCHANTING_ITEM_UUID_KEY, PersistentDataType.STRING);
            if (uuidStr != null) {
                itemId = UUID.fromString(uuidStr);
            } else {
                itemId = UUID.randomUUID();
                event.getItem().editMeta(meta -> meta.getPersistentDataContainer().set(ENCHANTING_ITEM_UUID_KEY, PersistentDataType.STRING, itemId.toString()));
            }
            // Seed needs to not only be based off the enchantment seed but also the item, so the same enchantments are not always getting the buff
            for (EnchantmentOffer offer : event.getOffers()) {
                if (offer == null) continue;
                // generate random seed from etable seed, uuid, and the enchant name
                Random randGen = new Random(event.getView().getEnchantmentSeed()
                        ^ itemId.getLeastSignificantBits()
                        ^ itemId.getMostSignificantBits()
                        ^ offer.getEnchantment().displayName(0).hashCode()
                );
                if (randGen.nextDouble() > bonusLevelChance) {
                    return;
                }
                // Increase enchant level by 1, respecting max level
                offer.setEnchantmentLevel(Math.min(offer.getEnchantmentLevel() + 1, offer.getEnchantment().getMaxLevel()));
            }
        }

        // This handler handles changing the actual enchant applied when you select the previously modified offer by recreating the rng calculation
        @EventHandler
        public void onEnchant(EnchantItemEvent event) {
            Double bonusLevelChance = event.getEnchanter().getPersistentDataContainer().get(ENCHANTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE);
            // is talisman equipped
            if (bonusLevelChance == null) {
                return;
            }
            // Get ETable that is open
            if (!(event.getEnchanter().getOpenInventory() instanceof EnchantmentView etableView)) {
                return;
            }
            String itemIdStr = event.getItem().getPersistentDataContainer().get(ENCHANTING_ITEM_UUID_KEY, PersistentDataType.STRING);
            if (itemIdStr == null) {
                return;
            }
            UUID itemId = UUID.fromString(itemIdStr);
            Player player = event.getEnchanter();
            for (Enchantment enchant : event.getEnchantsToAdd().keySet()) {
                // regenerate the seed used to set the offers and check the rng again
                Random randGen = new Random(etableView.getEnchantmentSeed()
                        ^ itemId.getLeastSignificantBits()
                        ^ itemId.getMostSignificantBits()
                        ^ enchant.displayName(0).hashCode()
                );
                if (randGen.nextDouble() > bonusLevelChance) {
                    return;
                }
                // if the offer level was increased, then also increase the applied enchant
                event.getEnchantsToAdd().replace(enchant, Math.min(event.getEnchantsToAdd().get(enchant) + 1, enchant.getMaxLevel()));
                player.playSound(PylonConfig.ENCHANTING_TALISMAN_TRIGGER_SOUND.create(), player);
            }
        }
    }
}
