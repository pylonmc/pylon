package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericBrickMolder;
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

public class ElectricBrickMolder extends GenericBrickMolder implements RebarElectricConsumerBlock {

    private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
        private final int ticksPerMoldingCycle = getSettings().getOrThrow("ticks-per-molding-cycle", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("molding-cycles", UnitFormat.CYCLES_PER_SECOND.format(20 / (ticksPerMoldingCycle * tickInterval))),
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricBrickMolder(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public ElectricBrickMolder(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        super.postInitialise();
        setRequiredPower(powerUsage);
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(tickInterval);
    }
}
