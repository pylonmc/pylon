package io.github.pylonmc.pylon.api;

import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public record EffectTag(PotionEffect effect) implements RebarFluidTag {

    private static final double AREA_EFFECT_LIMIT = 20;

    public void spawnAreaEffectClouds(@NotNull Location center, double fluidAmount) {
        double radius = AREA_EFFECT_LIMIT * (1 - Math.exp(-0.06 * (fluidAmount / 1000)));
        spawnCloud(center, radius);
        for (int d = 1; d < radius; d++) {
            double newRadius = Math.sqrt(radius * radius - d * d);
            spawnCloud(center.clone().add(0, d, 0), newRadius);
            spawnCloud(center.clone().add(0, -d, 0), newRadius);
        }
    }

    private void spawnCloud(Location location, double radius) {
        location.getWorld().spawn(location, AreaEffectCloud.class, cloud -> {
            cloud.addCustomEffect(effect, true);
            cloud.setRadius((float) radius);
            cloud.setRadiusPerTick(-1f / (2 * 20));
            cloud.setWaitTime(5);
        });
    }

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable(
                "pylon.fluid.tag.effect",
                RebarArgument.of("effect", Component.translatable(effect.getType().translationKey())),
                RebarArgument.of("maybe-strength", effect.getAmplifier() == 0 ? Component.empty() : Component.text(" " + romanNumeral(effect.getAmplifier() + 1)))
        );
    }

    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private static String romanNumeral(int num) {
        if (num < 1 || num > 3999) {
            throw new IllegalArgumentException("Number must be between 1 and 3999");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (num >= VALUES[i]) {
                num -= VALUES[i];
                sb.append(SYMBOLS[i]);
            }
        }
        return sb.toString();
    }
}
