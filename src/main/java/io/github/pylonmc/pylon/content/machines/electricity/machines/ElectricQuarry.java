package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericQuarry;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNode;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class ElectricQuarry extends GenericQuarry implements SimpleElectricRebarBlock {

    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final int radius = getSettingOrThrow("radius", ConfigAdapter.INTEGER);
        public final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);
        public final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            int diameter = 2 * radius + 1;
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("mining-area", diameter + "x" + diameter),
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricQuarry(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createSimpleElectricPort(ElectricNode.Type.CONSUMER, getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricQuarry(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessing() || !isPowered()) {
            return;
        }

        progressProcess(tickInterval);
    }

    @Override
    protected boolean canBreakBlock() {
        return isPowered();
    }
}

