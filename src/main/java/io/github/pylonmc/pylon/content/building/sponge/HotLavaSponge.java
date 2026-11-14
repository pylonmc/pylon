package io.github.pylonmc.pylon.content.building.sponge;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author balugaq
 * @see PowerfulSponge
 * @see LavaSponge
 */
public class HotLavaSponge extends PowerfulWaterSponge {
    private static final int CHECK_RANGE = ConfigSection.fromSettings(PylonKeys.HOT_LAVA_SPONGE)
            .getOrThrow("check-range", ConfigAdapter.INTEGER);
    private final Location PARTICLE_DISPLAY_LOCATION = getBlock().getLocation().clone().add(0.5, 0.5, 0.5);

    public HotLavaSponge(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public HotLavaSponge(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void spawnParticles() {
        new ParticleBuilder(Particle.FLAME)
                .location(PARTICLE_DISPLAY_LOCATION)
                .count(3)
                .spawn();
    }

    @Override
    public int checkRange() {
        return CHECK_RANGE;
    }

    @Override
    public void updateSponge(@NotNull Block sponge) {
        super.updateSponge(sponge);
        BlockStorage.placeBlock(sponge, PylonKeys.LAVA_SPONGE);
    }

    /**
     * @author balugaq
     */
    public static class Item extends RebarItem {
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("check-range", UnitFormat.BLOCKS.format(CHECK_RANGE).decimalPlaces(1))
            );
        }
    }
}
