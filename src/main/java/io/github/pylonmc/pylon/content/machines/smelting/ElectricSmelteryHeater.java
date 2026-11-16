package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.NoVanillaInventoryRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ElectricSmelteryHeater extends SmelteryComponent implements
        SimpleElectricRebarBlock,
        DirectionalRebarBlock,
        TickingRebarBlock,
        NoVanillaInventoryRebarBlock {

    @Getter
    private final double temperature = getSettingOrThrow("temperature", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        @Getter
        private final double temperature = getSettingOrThrow("temperature", ConfigAdapter.DOUBLE);

        @Getter
        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature)),
                    RebarArgument.of("power", UnitFormat.WATTS.format(powerUsage).decimalPlaces(2).selectPrefixAndRescale())
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricSmelteryHeater(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
        setFacing(context.getFacing());

        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());
        setRequiredPower(getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE));
    }

    @SuppressWarnings("unused")
    public ElectricSmelteryHeater(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        SmelteryController controller = getController();
        if (controller == null || !controller.isRunning() || !isPowered()) return;
        controller.heatAsymptotically(temperature);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (!isPowered()) {
            display.add(Component.translatable(
                    "pylon.message.no_power",
                    RebarArgument.of("power", UnitFormat.WATTS.format(getRequiredPower()).decimalPlaces(2).selectPrefixAndRescale())
            ));
        }
        return display;
    }
}
