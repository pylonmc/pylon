package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author balugaq
 */
public class ChronicleResin extends RebarItem implements PotionCatalyst {
    private final int durationBoostSeconds = getSettings().getOrThrow("duration-boost-seconds", ConfigAdapter.INTEGER);
    private final double applySuccessRate = getSettings().getOrThrow("apply-success-rate", ConfigAdapter.DOUBLE);

    public ChronicleResin(final @NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean apply(final @NotNull Map<PotionEffectType, PotionEffect> effects) {
        // randomly choose one type
        PotionEffectType type = effects.keySet().stream().toList().get(ThreadLocalRandom.current().nextInt(effects.size()));
        PotionEffect effect = effects.get(type);
        if (effect.isInfinite()) {
            return false;
        }

        effects.put(type, effect.withDuration(effect.getDuration() + durationBoostSeconds * 20));
        return true;
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("duration-boost-seconds", UnitFormat.SECONDS.format(durationBoostSeconds)),
                RebarArgument.of("apply-success-rate", UnitFormat.PERCENT.format(applySuccessRate * 100).decimalPlaces(2))
        );
    }
}
