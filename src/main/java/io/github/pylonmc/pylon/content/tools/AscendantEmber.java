package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
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
public class AscendantEmber extends PotionCatalyst {
    private final int maxAmplifier = getSettings().getOrThrow("max-amplifier", ConfigAdapter.INTEGER);
    private final double durationShortenRate = getSettings().getOrThrow("duration-shorten-rate", ConfigAdapter.DOUBLE);
    private final double applySuccessRate = getSettings().getOrThrow("apply-success-rate", ConfigAdapter.DOUBLE);

    public AscendantEmber(final @NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean apply(final Map<PotionEffectType, PotionEffect> effects) {
        // randomly choose one type
        PotionEffectType type = effects.keySet().stream().toList().get(ThreadLocalRandom.current().nextInt(effects.size()));
        PotionEffect effect = effects.get(type);
        if (effect.getAmplifier() > maxAmplifier) {
            return false;
        }

        effects.put(type, effect.withAmplifier(effect.getAmplifier() + 1).withDuration((int) (effect.getDuration() * durationShortenRate)));
        return true;
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("max-amplifier", maxAmplifier),
                RebarArgument.of("duration-shorten-rate", UnitFormat.PERCENT.format(durationShortenRate * 100).decimalPlaces(2)),
                RebarArgument.of("apply-success-rate", UnitFormat.PERCENT.format(applySuccessRate * 100).decimalPlaces(2))
        );
    }
}
