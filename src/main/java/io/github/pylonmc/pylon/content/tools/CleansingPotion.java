package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarSplashPotion;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class CleansingPotion extends RebarItem implements RebarSplashPotion {

    public CleansingPotion(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSplash(@NotNull PotionSplashEvent event, @NotNull EventPriority priority) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof ZombieVillager villager) {
                // Convert to regular villager
                villager.setConversionTime(0, true);
                villager.heal(requireNonNull(villager.getAttribute(Attribute.MAX_HEALTH)).getValue());
            }
        }
    }
}
