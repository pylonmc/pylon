package io.github.pylonmc.pylon.content.machines.chemistry;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
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

public class ElectrolysisModule extends RebarBlock implements
        DirectionalRebarBlock,
        SimpleElectricRebarBlock,
        ChemicalPlant.ConditionalModuleBlock {

    public static class Item extends RebarItem {

        @Getter
        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("power", UnitFormat.WATTS.format(powerUsage).decimalPlaces(2).selectPrefixAndRescale()));
        }
    }

    @SuppressWarnings("unused")
    public ElectrolysisModule(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());
        setRequiredPower(getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE));
    }

    @SuppressWarnings("unused")
    public ElectrolysisModule(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isActive() {
        return isPowered();
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