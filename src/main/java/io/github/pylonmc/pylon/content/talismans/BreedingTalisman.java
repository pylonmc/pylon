package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BreedingTalisman extends Talisman {
    public final float adultChance = getSettingOrThrow("adult-chance", ConfigAdapter.FLOAT);
    public static final NamespacedKey BREEDING_TALISMAN_KEY = PylonUtils.pylonKey("breeding_talisman");
    public static final NamespacedKey BREEDING_TALISMAN_ADULT_CHANCE_KEY = PylonUtils.pylonKey("breeding_talisman_adult_chance");

    public BreedingTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("adult_chance", UnitFormat.PERCENT.format(adultChance * 100).decimalPlaces(2)));
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return BREEDING_TALISMAN_KEY;
    }

    @Override
    public void applyEffect(@NotNull Player player) {
        super.applyEffect(player);
        player.getPersistentDataContainer().set(BREEDING_TALISMAN_ADULT_CHANCE_KEY, PersistentDataType.FLOAT, adultChance);
    }

    @Override
    public void removeEffect(@NotNull Player player) {
        super.removeEffect(player);
        player.getPersistentDataContainer().remove(BREEDING_TALISMAN_ADULT_CHANCE_KEY);
    }

    public static final class BreedingTalismanListener implements Listener {
        @EventHandler
        public void onBreedEvent(EntityBreedEvent event) {
            if (event.getBreeder() == null || event.getBreeder().getType() != EntityType.PLAYER) {
                return;
            }
            Float adultChance = event.getBreeder().getPersistentDataContainer().get(BREEDING_TALISMAN_ADULT_CHANCE_KEY, PersistentDataType.FLOAT);
            if(adultChance == null){
                return;
            }
            if(!(event.getEntity() instanceof Animals child)){
                return;
            }
            if(ThreadLocalRandom.current().nextFloat() > adultChance){
                return;
            }
            child.setAdult();
        }
    }
}
