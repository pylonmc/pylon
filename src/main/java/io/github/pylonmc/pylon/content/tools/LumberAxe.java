package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.*;

public class LumberAxe extends VeinminingTool {
    private static final List<Vector3i> OFFSETS = List.of(
            new Vector3i(1, 1, 1), new Vector3i(1, 1, 0), new Vector3i(1, 1, -1),
            new Vector3i(0, 1, 1), new Vector3i(0, 1, 0), new Vector3i(0, 1, -1),
            new Vector3i(-1, 1, 1), new Vector3i(-1, 1, 0), new Vector3i(-1, 1, -1),

            new Vector3i(1, 0, 1), new Vector3i(1, 0, 0), new Vector3i(1, 0, -1),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, 0), new Vector3i(0, 0, -1),
            new Vector3i(-1, 0, 1), new Vector3i(-1, 0, 0), new Vector3i(-1, 0, -1),

            new Vector3i(1, -1, 1), new Vector3i(1, -1, 0), new Vector3i(1, -1, -1),
            new Vector3i(0, -1, 1), new Vector3i(0, -1, 0), new Vector3i(0, -1, -1),
            new Vector3i(-1, -1, 1), new Vector3i(-1, -1, 0), new Vector3i(-1, -1, -1)
    );

    private final int maxVeinSize = getSettingOrThrow("max-vein-size", ConfigAdapter.INTEGER);

    public LumberAxe(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean canVeinmine(Block root) {
        return Tag.LOGS.isTagged(root.getType());
    }

    @Override
    public Collection<Vector3i> getVeinminingOffsets() {
        return OFFSETS;
    }

    @Override
    public int getMaxVeinSize() {
        return maxVeinSize;
    }
}
