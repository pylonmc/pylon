package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.api.item.BatteryRebarItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeBattery extends BatteryRebarItem {
    public CreativeBattery(@NotNull ItemStack stack) {
        super(stack);
        postCapacityConfiguration();
    }

    @Override
    protected boolean startsCharged() {
        return true;
    }

    @Override
    public double getCapacity() {
        return 10e20;
    }
}
