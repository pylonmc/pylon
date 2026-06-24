package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InventoryEffectRebarItem;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class Talisman extends RebarItem implements InventoryEffectRebarItem {
    protected Talisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onAddedToInventory(@NotNull Player player) {
        InventoryEffectRebarItem.super.onAddedToInventory(player);
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
        InventoryEffectRebarItem.super.onRemovedFromInventory(player);
        Integer currentTalismanLevel = player.getPersistentDataContainer().get(getTalismanKey(), PersistentDataType.INTEGER);
        if (currentTalismanLevel == null || getLevel() != currentTalismanLevel) {
            return;
        }

        // Check if there are any other talismans which will override this one
        // e.g. if the player just removed a health talisman 3, is there another health talisman to fall back to?
        Talisman bestTalisman = null;
        for (ItemStack stack : player.getInventory()) {
            if (fromStack(stack, Talisman.class) instanceof Talisman talisman) {
                if (talisman.getTalismanKey().equals(getTalismanKey()) && (bestTalisman == null || bestTalisman.getLevel() < talisman.getLevel())) {
                    bestTalisman = talisman;
                }
            }
        }

        if (bestTalisman != null && bestTalisman.getLevel() != getLevel()) {
            removeEffect(player);
            bestTalisman.applyEffect(player);
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
    public long getBaseTickInterval() {
        return PylonConfig.DEFAULT_TALISMAN_TICK_INTERVAL;
    }

    /**
     * Get the level of the talisman, this is used to determine which talismans should overwrite other ones.
     * <br>
     * By default, this is determined by a {@link RebarItem} {@link #getSettingOrThrow(String, ConfigAdapter) setting}
     */
    public int getLevel() {
        return getSettingOrThrow("level", ConfigAdapter.INTEGER);
    }

    /**
     * Get the generic key of the talisman, should be the same between all talismans of the same type, ie all health talisman levels have the same return value for this.
     */
    public abstract NamespacedKey getTalismanKey();
}
