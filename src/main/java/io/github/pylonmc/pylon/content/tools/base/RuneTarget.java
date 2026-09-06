package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.rebar.item.RebarItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link RebarItem} that controls which {@link Rune runes} can be applied to itself.
 * By default, this is determined solely by the {@link Rune} itself in {@link Rune#isRuneApplicable(Player, Item, Item)}
 *
 * @author balugaq, JustAHuman
 */
public interface RuneTarget {
    /**
     * Returns if a {@link Rune} can be applied to this item.
     *
     * @param player The player who dropped the rune
     * @param rune  The rune to be applied
     * @param runeItem The dropped rune item entity
     * @param targetItem The item to be applied to (the item entity this stack belongs to)
     * @return true if the rune can be applied to this item, false otherwise
     */
    boolean isRuneSupported(@NotNull Player player, @NotNull Rune rune, @NotNull Item runeItem, Item targetItem);
}
