package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.api.item.ConfigurableBatteryRebarItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DisposableBattery extends ConfigurableBatteryRebarItem {
    public DisposableBattery(@NotNull ItemStack stack) {
        super(stack);
        if (!getStack().getPersistentDataContainer().has(CHARGE_KEY)) {
            setCharge(getCapacity());
        }
    }

    @Override
    public boolean isRechargeable() {
        return false;
    }
}
