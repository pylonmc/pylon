package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.content.machines.generic.AbstractGrindstone;
import io.github.pylonmc.rebar.block.base.RebarElectricConsumerBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class ElectricGrindstone extends AbstractGrindstone implements
        RebarElectricConsumerBlock {

    private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage)));
        }
    }

    @SuppressWarnings("unused")
    public ElectricGrindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricGrindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(tickInterval);
    }
}
