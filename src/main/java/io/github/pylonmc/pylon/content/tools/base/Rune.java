package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.DropRebarItemHandler;
import io.github.pylonmc.rebar.item.research.Research;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collection;

/**
 * A {@link RebarItem} that can be {@link #onRuneApply(Player, Item, Item) applied} to
 * {@link #isRuneApplicable(Player, Item, Item) applicable} {@link Item items} when dropped on the ground.
 *
 * @author balugaq, JustAHuman
 */
public abstract class Rune extends RebarItem implements DropRebarItemHandler {
    public Rune(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Returns if the rune is applicable to a dropped item.
     * @see RuneTarget
     */
    public abstract boolean isRuneApplicable(@NotNull Player player, @NotNull Item runeItem, @NotNull Item item);

    /**
     * Called when a dropped {@link Rune} is applied to an applicable item nearby,
     * <br>
     * Note: If the rune should be consumed on use, you must do that yourself
     *
     * @param player     The player who dropped the rune
     * @param runeItem   The rune dropped item entity, amount may be > 1
     * @param targetItem The item to be applied to, amount may be > 1
     * @see #isRuneApplicable(Player, Item, Item)
     */
    public abstract void onRuneApply(@NotNull Player player, @NotNull Item runeItem, @NotNull Item targetItem);

    @Override
    @OverridingMethodsMustInvokeSuper
    @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(@NotNull PlayerDropItemEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        Item runeItem = event.getItemDrop();
        if (!Research.canPlayerUse(player, this, true)) {
            return;
        }

        Bukkit.getScheduler().runTaskTimer(Pylon.getInstance(), task -> {
            if (!player.isValid() || !runeItem.isValid()) {
                task.cancel();
                return;
            }

            if (!runeItem.isOnGround()) {
                return;
            }

            Collection<Item> nearbyItems = runeItem.getWorld().getNearbyEntitiesByType(Item.class, runeItem.getLocation(), PylonConfig.RUNE_CHECK_RANGE, targetItem -> {
                if (runeItem == targetItem || !isRuneApplicable(player, runeItem, targetItem)) {
                    return false;
                }

                ItemStack targetItemStack = targetItem.getItemStack();
                return !(fromStack(targetItemStack, RuneTarget.class) instanceof RuneTarget runeTarget) || runeTarget.isRuneSupported(player, this, runeItem, targetItem);
            });
            if (nearbyItems.isEmpty()) {
                return;
            }

            onRuneApply(player, runeItem, nearbyItems.iterator().next());
            task.cancel();
        }, 20, 20);
    }
}
