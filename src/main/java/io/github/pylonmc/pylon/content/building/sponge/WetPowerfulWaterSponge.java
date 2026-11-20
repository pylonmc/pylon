package io.github.pylonmc.pylon.content.building.sponge;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

/**
 * This sponge is "used" and does NOT have any special abilities.
 *
 * @author balugaq
 * @see PowerfulSponge
 * @see PowerfulWaterSponge
 */
public class WetPowerfulWaterSponge extends RebarBlock {
    public WetPowerfulWaterSponge(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        Location location = block.getLocation();

        // Dry out the sponge in the nether
        if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                BlockStorage.breakBlock(location, new BlockBreakContext.PluginBreak(block, false));
                BlockStorage.placeBlock(location, PylonKeys.POWERFUL_WATER_SPONGE);
            }, 1);
        }
    }

    public WetPowerfulWaterSponge(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
}
