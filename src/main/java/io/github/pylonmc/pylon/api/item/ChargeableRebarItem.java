package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class ChargeableRebarItem extends RebarItem {

    private static final NamespacedKey BATTERY_KEY = pylonKey("battery");

    public ChargeableRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }

    public @Nullable BatteryRebarItem getBattery() {
        ItemStack item = getStack().getPersistentDataContainer().get(BATTERY_KEY, RebarSerializers.ITEM_STACK);
        return RebarItem.fromStack(item, BatteryRebarItem.class);
    }

    public void setBattery(@Nullable BatteryRebarItem battery) {
        getStack().editPersistentDataContainer(pdc -> {
            if (battery != null) {
                pdc.set(BATTERY_KEY, RebarSerializers.ITEM_STACK, battery.getStack());
            } else {
                pdc.remove(BATTERY_KEY);
            }
        });
    }

    public double getCharge() {
        BatteryRebarItem battery = getBattery();
        return battery == null ? 0 : battery.getCharge();
    }

    /**
     * @throws NullPointerException if there is no battery inside
     */
    public void setCharge(double charge) {
        BatteryRebarItem battery = Objects.requireNonNull(getBattery(), "There is no battery inside");
        battery.setCharge(charge);
        setBattery(battery);
    }
}
