package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.util.RandomizedSound;

import java.util.List;


public final class PylonConfig {

    private static final ConfigSection config = ConfigSection.copyResource(Pylon.getInstance(), "config.yml");
    public static final List<String> WORLDS_WITH_OIL = config.getOrThrow("worlds-with-oil", ConfigAdapter.LIST.from(ConfigAdapter.STRING));
    public static final double OIL_SCALE = config.getOrThrow("oil-scale", ConfigAdapter.DOUBLE);
    public static final double OIL_CUTOFF = config.getOrThrow("oil-cutoff", ConfigAdapter.DOUBLE);
    public static final double RUNE_CHECK_RANGE = config.getOrThrow("rune-check-range", ConfigAdapter.DOUBLE);
    public static final long DEFAULT_TALISMAN_TICK_INTERVAL = config.getOrThrow("default-talisman-tick-interval", ConfigAdapter.LONG);
    public static final RandomizedSound BARTERING_TALISMAN_TRIGGER_SOUND = config.getOrThrow("talismans.bartering-trigger-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public static final RandomizedSound ENCHANTING_TALISMAN_TRIGGER_SOUND = config.getOrThrow("talismans.enchanting-trigger-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public static final RandomizedSound FARMING_TALISMAN_TRIGGER_SOUND = config.getOrThrow("talismans.farming-trigger-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public static final RandomizedSound HUNTING_TALISMAN_TRIGGER_SOUND = config.getOrThrow("talismans.hunting-trigger-sound", ConfigAdapter.RANDOMIZED_SOUND);

    private PylonConfig() {}
}
