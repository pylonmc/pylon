package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class SodiumBlock extends RebarBlock implements TickingRebarBlock {

    @Getter
    private final float explosionPower = getSettingOrThrow("explosion-power", ConfigAdapter.FLOAT);

    @Getter
    private final double explosionChancePerSecond = getSettingOrThrow("explosion-chance-per-second", ConfigAdapter.DOUBLE);

    @SuppressWarnings("unused")
    public SodiumBlock(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
    }

    @SuppressWarnings("unused")
    public SodiumBlock(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        double probability = 1 - Math.pow(1 - explosionChancePerSecond, getTickInterval() / 20.0);
        if (ThreadLocalRandom.current().nextDouble() > probability) return;
        if (!(RebarUtils.isRainingAt(new BlockPosition(getBlock()).getRelative(BlockFace.UP)) || hasWaterOnSides())) return;
        BlockStorage.breakBlock(this, new BlockBreakContext.PluginBreak(getBlock(), false, true));
        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> getBlock().getWorld().createExplosion(getBlock().getLocation().toCenterLocation(), explosionPower, true, true));
    }

    private boolean hasWaterOnSides() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block relative = getBlock().getRelative(x, y, z);
                    if (relative.getType() == Material.WATER || (relative.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged())) return true;
                }
            }
        }
        return false;
    }
}