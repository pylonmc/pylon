package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInventoryEffectItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class Talisman extends RebarItem implements RebarInventoryEffectItem {
    public final int level = getSettings().getOrThrow("level", ConfigAdapter.INTEGER);

    public Talisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onAddedToInventory(@NotNull Player player) {
        RebarInventoryEffectItem.super.onAddedToInventory(player);
        Integer currentTalismanLevel = player.getPersistentDataContainer().get(getTalismanKey(), PersistentDataType.INTEGER);
        if (currentTalismanLevel == null) {
            applyEffect(player);
        } else if (currentTalismanLevel < getLevel()) {
            removeEffect(player);
            applyEffect(player);
        }
    }

    @Override
    public void onRemovedFromInventory(@NotNull Player player) {
        RebarInventoryEffectItem.super.onRemovedFromInventory(player);
        Integer currentTalismanLevel = player.getPersistentDataContainer().get(getTalismanKey(), PersistentDataType.INTEGER);
        if (currentTalismanLevel == null)
            return; // really shouldn't happen, but in this case less likely to crash by not calling removeEffect
        if (currentTalismanLevel == getLevel()) {
            removeEffect(player);
        }
    }

    /**
     * The implementation of this method MUST call super.applyEffect
     *
     * @param player The player who the effect is being applied to
     */
    @MustBeInvokedByOverriders
    public void applyEffect(@NotNull Player player) {
        player.getPersistentDataContainer().set(getTalismanKey(), PersistentDataType.INTEGER, getLevel());
    }

    /**
     * The implementation of this method MUST call super.removeEffect
     *
     * @param player The player who the effect is being removed from
     */
    @MustBeInvokedByOverriders
    public void removeEffect(@NotNull Player player) {
        player.getPersistentDataContainer().remove(getTalismanKey());
    }

    @Override
    public long getTickInterval() {
        return PylonConfig.DEFAULT_TALISMAN_TICK_INTERVAL;
    }

    /**
     * Get the level of the talisman, this is used to determine which talismans should overwrite other ones
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the generic key of the talisman, should be the same between all talismans of the same type, ie all health talisman levels have the same return value for this.
     */
    public abstract NamespacedKey getTalismanKey();
}
