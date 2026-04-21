package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
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
import java.util.concurrent.ThreadLocalRandom;

public class FarmingTalisman extends Talisman {
    public float extraCropChance = getSettings().getOrThrow("extra-crop-chance", ConfigAdapter.FLOAT);
    public static final NamespacedKey FARMING_TALISMAN_KEY = PylonUtils.pylonKey("farming_talisman");
    public static final NamespacedKey FARMING_TALISMAN_CHANCE_KEY = PylonUtils.pylonKey("farming_talisman_chance");

    public FarmingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return FARMING_TALISMAN_KEY;
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
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
            BlockState block = event.getBlockState();
            Location loc = block.getLocation();
            if (!Tag.CROPS.isTagged(block.getType()) || !event.getPlayer().getPersistentDataContainer().has(FARMING_TALISMAN_CHANCE_KEY)) {
                return;
            }
            List<Item> additionalDrops = new ArrayList<>();
            for (Item drop : event.getItems()) {
                if (ThreadLocalRandom.current().nextFloat() > event.getPlayer().getPersistentDataContainer().get(FARMING_TALISMAN_CHANCE_KEY, PersistentDataType.FLOAT)) {
                    continue;
                }
                additionalDrops.add(drop.getWorld().dropItem(drop.getLocation(), drop.getItemStack().clone()));
                loc.getWorld().playSound(PylonConfig.FARMING_TALISMAN_TRIGGER_SOUND.create(), loc.getX(), loc.getY(), loc.getZ());
            }
            event.getItems().addAll(additionalDrops);
        }
    }
}
