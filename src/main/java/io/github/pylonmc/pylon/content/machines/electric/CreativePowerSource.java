package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.util.NumberInputButton;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricProducerBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.MetricPrefix;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

public final class CreativePowerSource extends RebarBlock implements
        RebarElectricProducerBlock,
        RebarGuiBlock {

    @SuppressWarnings("unused")
    public CreativePowerSource(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public CreativePowerSource(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # v # p # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('v', NumberInputButton.builder()
                        .material(Material.REDSTONE)
                        .name(Component.translatable("pylon.gui.voltage"))
                        .increment(1)
                        .shiftIncrement(10)
                        .min(0)
                        .valueGetter(() -> (int) getVoltageProducing())
                        .valueSetter(this::setVoltageProducing)
                        .valueFormatter(v -> formatQuantity(UnitFormat.VOLTS, v))
                        .reopenWindow(this::openWindow)
                        .build())
                .addIngredient('p', NumberInputButton.builder()
                        .material(Material.NETHER_STAR)
                        .name(Component.translatable("pylon.gui.power"))
                        .increment(1)
                        .shiftIncrement(10)
                        .min(0)
                        .valueGetter(() -> (int) getPowerProducing())
                        .valueSetter(this::setPowerProducing)
                        .valueFormatter(p -> formatQuantity(UnitFormat.WATTS, p))
                        .reopenWindow(this::openWindow)
                        .build())
                .build();
    }

    private static ComponentLike formatQuantity(UnitFormat format, int quantity) {
        return format.format(quantity)
                .ignorePrefixes(MetricPrefix.DECI, MetricPrefix.DECA, MetricPrefix.HECTO)
                .abbreviate(true)
                .autoSelectPrefix()
                .decimalPlaces(0);
    }
}
