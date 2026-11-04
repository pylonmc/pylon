package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.WireRebarItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeWire extends RebarItem implements WireRebarItem {

    public CreativeWire(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public double getMaxPower() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public @NotNull Material getDisplayMaterial() {
        return Material.PURPLE_CONCRETE;
    }
}
