package io.github.pylonmc.pylon.content.electricity;

import io.github.pylonmc.pylon.util.NumberInputButton;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.MetricPrefix;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class CreativePowerSource extends RebarBlock implements
        RebarElectricBlock.Producer,
        RebarGuiBlock {

    private static final NamespacedKey VOLTAGE_KEY = pylonKey("voltage");
    private static final NamespacedKey POWER_KEY = pylonKey("power");

    private int voltage;
    private int power;

    @SuppressWarnings("unused")
    public CreativePowerSource(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        voltage = 0;
        power = 0;

        createElectricNode(block.getLocation().toCenterLocation(), ElectricNode.Type.PRODUCER);
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public CreativePowerSource(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        voltage = pdc.get(VOLTAGE_KEY, RebarSerializers.INTEGER);
        power = pdc.get(POWER_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(VOLTAGE_KEY, RebarSerializers.INTEGER, voltage);
        pdc.set(POWER_KEY, RebarSerializers.INTEGER, power);
    }

    @Override
    public double getVoltage() {
        return voltage;
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
                        .valueGetter(() -> voltage)
                        .valueSetter(value -> voltage = value)
                        .valueFormatter(v -> formatQuantity(UnitFormat.VOLTS, v))
                        .reopenWindow(this::openWindow)
                        .build())
                .addIngredient('p', NumberInputButton.builder()
                        .material(Material.NETHER_STAR)
                        .name(Component.translatable("pylon.gui.power"))
                        .increment(1)
                        .shiftIncrement(10)
                        .min(0)
                        .valueGetter(() -> power)
                        .valueSetter(value -> power = value)
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
