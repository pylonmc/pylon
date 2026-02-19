package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author balugaq
 */
public abstract class Rune extends RebarItem {
    // These can be applied with runes
    public static final List<Class<?>> DEFAULT_APPLICABLES = List.of(
            RebarArrow.class,
            RebarBow.class,
            RebarBucket.class,
            RebarTool.class,
            RebarWeapon.class
    );

    public Rune(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Checks if the rune is applicable to the target item.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     * @return true if applicable, false otherwise
     */
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        RebarItem instance = RebarItem.fromStack(target);
        if (instance == null) {
            // Non-Rebar items are always applicable
            return true;
        }

        if (instance instanceof RuneApplicable checker && checker.applicableToTarget(event, rune)) {
            return true;
        }

        return DEFAULT_APPLICABLES.stream().anyMatch(clazz -> clazz.isInstance(instance));
    }

    /**
     * Handles contacting between an item and a rune.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     */
    public abstract void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target);

    public static class RuneListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        void onRuneDrop(@NotNull PlayerDropItemEvent event) {
            Player player = event.getPlayer();
            Item runeEntity = event.getItemDrop();
            ItemStack runeStack = runeEntity.getItemStack();
            RebarItem runeInstance = RebarItem.fromStack(runeStack);
            if (!(runeInstance instanceof Rune rune)) {
                return;
            }

            // Fix #155 - Fireproof rune only checks proximity at the moment it's dropped
            // Force run synchronously for entity handling
            Bukkit.getScheduler().runTaskTimer(Pylon.getInstance(), task -> {
                if (runeEntity.isDead() || !runeEntity.isValid()) {
                    task.cancel();
                    return;
                }

                if (!runeEntity.isOnGround()) {
                    return;
                }

                Collection<Item> nearbyEntities = player.getWorld().getNearbyEntitiesByType(Item.class, runeEntity.getLocation(), PylonConfig.RUNE_CHECK_RANGE, item -> rune.isApplicableToTarget(event, runeStack, item.getItemStack()));
                Item targetEntity = nearbyEntities
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (targetEntity == null) {
                    // No target, skip it.
                    return;
                }

                ItemStack target = targetEntity.getItemStack();

                // All actions are handled by devs
                rune.onContactItem(event, runeStack, target);
                runeEntity.setItemStack(runeStack);
                targetEntity.setItemStack(target);
            }, 1, 2);
        }
    }
}
