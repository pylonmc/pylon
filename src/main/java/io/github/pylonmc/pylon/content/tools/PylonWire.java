package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.WireRebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PylonWire extends RebarItem implements WireRebarItem {

    private final double maxPower = getSettingOrThrow("max-power", ConfigAdapter.DOUBLE);

    public PylonWire(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("max-power", UnitFormat.WATTS.format(maxPower)));
    }

    @Override
    public double getMaxPower() {
        return maxPower;
    }

    @Override
    public @NotNull Material getDisplayMaterial() {
        return Material.COPPER_BLOCK;
    }
}
