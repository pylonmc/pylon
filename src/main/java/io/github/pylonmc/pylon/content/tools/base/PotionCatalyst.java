package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.rebar.item.RebarItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author balugaq
 */
public abstract class PotionCatalyst extends RebarItem {
    public PotionCatalyst(@NotNull final ItemStack stack) {
        super(stack);
    }

    public abstract boolean apply(Map<PotionEffectType, PotionEffect> effects);
}
