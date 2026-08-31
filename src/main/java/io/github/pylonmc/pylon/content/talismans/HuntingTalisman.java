package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.loot.LootTableResultBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class HuntingTalisman extends Talisman {
    public static final NamespacedKey HUNTING_TALISMAN_KEY = PylonUtils.pylonKey("hunting_talisman");
    public static final NamespacedKey HUNTING_TALISMAN_BONUS_KEY = PylonUtils.pylonKey("hunting_talisman_bonus");

    public final double chanceForExtraItem = getSettingOrThrow("chance-for-extra-item", ConfigAdapter.DOUBLE);

    public HuntingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
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
            Entity entity = event.getEntity();
            if (!(entity instanceof Lootable lootable) || !(event.getDamageSource().getCausingEntity() instanceof Player player)) {
                return;
            }

            Double chanceForExtraItem = player.getPersistentDataContainer().get(HUNTING_TALISMAN_BONUS_KEY, PersistentDataType.DOUBLE);
            if (chanceForExtraItem == null) {
                return;
            }

            LootTable lootTable = lootable.getLootTable();
            long lootTableSeed = lootable.getSeed();
            if (lootTable == null) {
                return;
            }

            boolean triggered = false;
            Collection<ItemStack> additionalDrops = LootTableResultBuilder.of(event)
                    .getRandomItems(entity.getWorld(), LootTableResultBuilder.ENTITY, lootTable, lootTableSeed);
            for (ItemStack additionalDrop : additionalDrops) {
                if (additionalDrop.hasData(DataComponentTypes.RARITY) && additionalDrop.getData(DataComponentTypes.RARITY).compareTo(ItemRarity.RARE) >= 0) {
                    continue;
                }

                if (Math.random() <= chanceForExtraItem) {
                    additionalDrop.setAmount(1);
                    event.getDrops().add(additionalDrop);
                    triggered = true;
                }
            }

            if (triggered) {
                PylonConfig.HUNTING_TALISMAN_TRIGGER_SOUND.playAt(entity);
            }
        }
    }
}
