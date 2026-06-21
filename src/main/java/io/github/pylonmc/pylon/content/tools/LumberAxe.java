package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;


@SuppressWarnings("UnstableApiUsage")
public class LumberAxe extends VeinminingTool {
    private final int maxVeinSize = getSettingOrThrow("max-vein-size", ConfigAdapter.INTEGER);

    public LumberAxe(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean canVeinmine(Block root) {
        return Tag.LOGS.isTagged(root.getType());
    }

    @Override
    public int getMaxVeinSize() {
        return maxVeinSize;
    }
}
