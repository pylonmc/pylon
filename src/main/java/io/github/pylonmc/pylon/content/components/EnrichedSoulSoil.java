package io.github.pylonmc.pylon.content.components;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public final class EnrichedSoulSoil extends RebarBlock implements RebarTickingBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public EnrichedSoulSoil(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public EnrichedSoulSoil(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (getBlock().getRelative(BlockFace.UP).getType() == Material.SOUL_FIRE) {
            new ParticleBuilder(Particle.SOUL_FIRE_FLAME)
                    .location(getBlock().getLocation().toCenterLocation())
                    .count(3)
                    .extra(0.1)
                    .spawn();
        }
    }
}