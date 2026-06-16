package io.github.pylonmc.pylon.content.machines.petrochemicals;

import io.github.pylonmc.pylon.PylonConfig;
import org.bukkit.block.Block;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class OilService {

    private static final Map<UUID, SimplexNoiseGenerator> generators = new HashMap<>();

    public static @Nullable Double getOilYield(@NonNull Block block) {
        if (!PylonConfig.WORLDS_WITH_OIL.contains(block.getWorld().getName())) {
            return null;
        }

        SimplexNoiseGenerator generator = generators.computeIfAbsent(block.getWorld().getUID(), _ -> new SimplexNoiseGenerator(block.getWorld()));
        // noise value at this block normalized between 0 and 1
        double normalized = (1.0 + generator.noise(PylonConfig.OIL_SCALE * block.getX(), PylonConfig.OIL_SCALE * block.getZ())) / 2.0;
        return Math.max(0, normalized - PylonConfig.OIL_CUTOFF) / (1.0 - PylonConfig.OIL_CUTOFF);
    }
}
