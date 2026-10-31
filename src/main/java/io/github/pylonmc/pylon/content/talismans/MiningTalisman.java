package io.github.pylonmc.pylon.content.talismans;

import com.destroystokyo.paper.MaterialTags;
import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class MiningTalisman extends Talisman {
    public static final NamespacedKey MINING_TALISMAN_KEY = PylonUtils.pylonKey("mining_talisman");
    public static final NamespacedKey MINING_TALISMAN_CHANCE_KEY = PylonUtils.pylonKey("mining_talisman_chance");

    public float extraOreChance = getSettingOrThrow("extra-ore-chance", ConfigAdapter.FLOAT);

    public MiningTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return MINING_TALISMAN_KEY;
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("extra_ore_chance", UnitFormat.PERCENT.format(extraOreChance * 100).decimalPlaces(2)));
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(MINING_TALISMAN_CHANCE_KEY, PersistentDataType.FLOAT, extraOreChance);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(MINING_TALISMAN_CHANCE_KEY);
    }

    public static class MiningTalismanListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockDrop(BlockDropItemEvent event) {
            Player player = event.getPlayer();

            Float extraOreChance = player.getPersistentDataContainer().get(MINING_TALISMAN_CHANCE_KEY, PersistentDataType.FLOAT);
            if (extraOreChance == null) {
                return;
            }

            Material material = event.getBlock().getType();
            if (!MaterialTags.ORES.isTagged(material)) {
                return;
            }

            if (BlockStorage.get(event.getBlock()) != null) {
                return;
            }

            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }

            ItemStack tool = player.getInventory().getItemInMainHand();
            ItemEnchantments enchantments = tool.getData(DataComponentTypes.ENCHANTMENTS);
            if (enchantments != null && enchantments.enchantments().containsKey(Enchantment.SILK_TOUCH)) {
                return;
            }

            if (Math.random() >= extraOreChance) {
                return;
            }

            Collection<ItemStack> drops = event.getBlock().getDrops(tool);
            if (drops.isEmpty()) {
                return;
            }

            for (ItemStack drop : drops) {
                event.getItems().add(
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop.clone()));
            }

            PylonConfig.MINING_TALISMAN_TRIGGER_SOUND.play(event.getBlock());
        }
    }
}
