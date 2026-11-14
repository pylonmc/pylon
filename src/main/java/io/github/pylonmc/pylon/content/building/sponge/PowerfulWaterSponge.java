package io.github.pylonmc.pylon.content.building.sponge;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author balugaq
 * @see PowerfulSponge
 * @see WetPowerfulWaterSponge
 */
public class PowerfulWaterSponge extends PowerfulSponge {
    private static final int CHECK_RANGE = ConfigSection.fromSettings(PylonKeys.POWERFUL_WATER_SPONGE)
            .getOrThrow("check-range", ConfigAdapter.INTEGER);
    private static final AbsorbHandler HANDLER = new AbsorbHandler()
            .handle(block -> block.setType(Material.AIR))
            .when(Material.WATER, Material.SEAGRASS, Material.TALL_SEAGRASS)

            .handle(block -> block.setType(Material.CAULDRON))
            .when(Material.WATER_CAULDRON)

            .handle(block -> {
                Waterlogged data = (Waterlogged) block.getBlockData();
                data.setWaterlogged(false);
                block.setBlockData(data);
            })
            .when(block -> block.getBlockData() instanceof Waterlogged)

            .handle(block -> {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.KELP));
                block.setType(Material.AIR);
            })
            .when(Material.KELP_PLANT, Material.KELP);

    public PowerfulWaterSponge(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public PowerfulWaterSponge(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public int checkRange() {
        return CHECK_RANGE;
    }

    @Override
    public @NotNull AbsorbHandler handler() {
        return HANDLER;
    }

    @Override
    public void updateSponge(@NotNull Block sponge) {
        super.updateSponge(sponge);
        BlockStorage.placeBlock(sponge, PylonKeys.WET_POWERFUL_WATER_SPONGE);
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
