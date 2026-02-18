package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.util.RandomizedSound;
import org.jetbrains.annotations.NotNull;

public record WateringSettings(
        int horizontalRange,
        int verticalRange,
        double cropChance,
        double sugarCaneChance,
        double cactusChance,
        double saplingChance,
        double particleChance,
        @NotNull RandomizedSound sound
) {
    public static WateringSettings fromConfig(ConfigSection config) {
        return new WateringSettings(
                config.getOrThrow("range.horizontal", ConfigAdapter.INTEGER),
                config.getOrThrow("range.vertical", ConfigAdapter.INTEGER),
                config.getOrThrow("chances.crops", ConfigAdapter.DOUBLE),
                config.getOrThrow("chances.sugar-cane", ConfigAdapter.DOUBLE),
                config.getOrThrow("chances.cactus", ConfigAdapter.DOUBLE),
                config.getOrThrow("chances.sapling", ConfigAdapter.DOUBLE),
                config.getOrThrow("particle-chance", ConfigAdapter.DOUBLE),
                config.getOrThrow("sound", ConfigAdapter.RANDOMIZED_SOUND)
        );
    }
}
