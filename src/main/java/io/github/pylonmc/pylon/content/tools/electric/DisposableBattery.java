package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.api.item.ConfigurableBatteryRebarItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisposableBattery extends ConfigurableBatteryRebarItem {
    public DisposableBattery(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    protected boolean startsCharged() {
        return true;
    }

    @Override
    public boolean isRechargeable() {
        return false;
    }
}
