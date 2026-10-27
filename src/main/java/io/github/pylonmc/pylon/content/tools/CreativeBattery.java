package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.api.item.BatteryRebarItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeBattery extends BatteryRebarItem {
    public CreativeBattery(@NotNull ItemStack stack) {
        super(stack);
        getStack().setData(DataComponentTypes.DAMAGE, 0);
    }

    @Override
    public boolean isRechargeable() {
        return false;
    }

    @Override
    public double getCharge() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public void setCharge(double charge) {
    }

    @Override
    public double getCapacity() {
        return Double.POSITIVE_INFINITY;
    }
}
