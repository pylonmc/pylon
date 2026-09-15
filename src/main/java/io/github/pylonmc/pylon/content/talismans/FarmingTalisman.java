package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FarmingTalisman extends Talisman {
    public static final NamespacedKey FARMING_TALISMAN_KEY = PylonUtils.pylonKey("farming_talisman");
    public static final NamespacedKey FARMING_TALISMAN_CHANCE_KEY = PylonUtils.pylonKey("farming_talisman_chance");

    public float extraCropChance = getSettingOrThrow("extra-crop-chance", ConfigAdapter.FLOAT);

    public FarmingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return FARMING_TALISMAN_KEY;
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("extra_crop_chance", UnitFormat.PERCENT.format(extraCropChance * 100).decimalPlaces(2)));
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(FARMING_TALISMAN_CHANCE_KEY, PersistentDataType.FLOAT, extraCropChance);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(FARMING_TALISMAN_CHANCE_KEY);
    }

    public static class FarmingTalismanListener implements Listener {
        @EventHandler
        public void onBlockBreak(BlockDropItemEvent event) {
            Float duplicateDropChance = event.getPlayer().getPersistentDataContainer().get(FARMING_TALISMAN_CHANCE_KEY, PersistentDataType.FLOAT);
            if (duplicateDropChance == null) {
                return;
            }

            BlockState block = event.getBlockState();
            BlockData data = block.getBlockData();
            if (!(data instanceof Ageable ageable)
                    || ageable.getAge() != ageable.getMaximumAge()
                    || !Tag.CROPS.isTagged(data.getMaterial())) {
                return;
            }

            List<Item> additionalDrops = new ArrayList<>();
            for (Item drop : event.getItems()) {
                if (Math.random() > duplicateDropChance) {
                    continue;
                }
                additionalDrops.add(drop.getWorld().dropItemNaturally(drop.getLocation(), drop.getItemStack().clone()));
            }

            if (!additionalDrops.isEmpty()) {
                event.getItems().addAll(additionalDrops);
                PylonConfig.FARMING_TALISMAN_TRIGGER_SOUND.play(event.getBlock());
            }
        }
    }
}
