package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
            for (ItemStack drop : getExtraDrops(event.getEntity(), event.getDrops())) {
                if (drop.getItemMeta().hasRarity() && drop.getItemMeta().getRarity().ordinal() >= ItemRarity.RARE.ordinal()) {
                    continue;
                }
                if (ThreadLocalRandom.current().nextDouble() > chanceForExtraItem) {
                    continue;
                }
                event.getDrops().add(drop.asOne());
            }
        }

        private List<ItemStack> getExtraDrops(LivingEntity entity, List<ItemStack> drops) {
            if (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ARMOR_STAND) {
                return List.of();
            }

            List<ItemStack> items = new ArrayList<>(drops);

            // No item duplications from horse-like entities' chest
            if (entity instanceof ChestedHorse horse && horse.isCarryingChest()) {
                // The carrying chest is not included in getStorageContents()
                items.remove(ItemStack.of(Material.CHEST));

                for (ItemStack stack : horse.getInventory().getStorageContents()) {
                    items.remove(stack);
                }
            }

            if (!(entity instanceof Ravager)) {
                // Only ravager drops saddle when killed
                items.remove(ItemStack.of(Material.SADDLE));
            }

            if (entity instanceof AbstractHorse horse) {
                // saddle has been removed above
                var inventory = horse.getInventory();

                if (inventory instanceof LlamaInventory llamaInventory) {
                    items.remove(llamaInventory.getDecor());
                } else {
                    ItemStack horseArmor = inventory.getItem(1);
                    if (horseArmor != null) {
                        // Horse armor
                        items.remove(horseArmor);
                    }
                }
            }

            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (entity.canUseEquipmentSlot(slot)) {
                        // see EquipmentSlot
                        items.remove(equipment.getItem(slot));
                    }
                }
            }

            return items;
        }
    }
}
