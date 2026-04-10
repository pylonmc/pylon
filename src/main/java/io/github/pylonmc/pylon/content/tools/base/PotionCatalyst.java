package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.pylon.content.machines.simple.PotionAltar;
import io.github.pylonmc.pylon.content.tools.AscendantEmber;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.item.RebarItem;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A catalyst for modifying potion effects in {@link PotionAltar} result potion.
 *
 * @author balugaq
 * @see AscendantEmber
 */
public interface PotionCatalyst {
    /**
     * Applies special features to the potion effects.
     * @param effects the potion effects to be handled, linked to the result potion directly.
     * @return whether the effects were applied successfully.
     */
    boolean apply(@NotNull Map<PotionEffectType, PotionEffect> effects);

    /**
     * `apply-success-rate` (double type) is required to check the success rate of the catalyst.
     * @see RebarItem#getSettings()
     */
    @NotNull
    Config getSettings();
}
