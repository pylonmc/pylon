package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class BatteryRebarItem extends RebarItem {

    private static final NamespacedKey CHARGE_KEY = pylonKey("charge");

    @Getter
    private final double capacity = getSettingOrThrow("capacity", ConfigAdapter.DOUBLE);

    public BatteryRebarItem(@NotNull ItemStack stack) {
        super(stack);
        stack.setData(DataComponentTypes.MAX_DAMAGE, 1000);
        stack.setData(DataComponentTypes.DAMAGE, 1000 - (int) (getCharge() / getCapacity() * 1000));
    }

    public double getCharge() {
        return getStack().getPersistentDataContainer().getOrDefault(CHARGE_KEY, RebarSerializers.DOUBLE, 0.0);
    }

    public void setCharge(double charge) {
        getStack().editPersistentDataContainer(pdc -> pdc.set(CHARGE_KEY, RebarSerializers.DOUBLE, Math.clamp(charge, 0.0, getCapacity())));
    }

    public boolean isRechargeable() {
        return true;
    }
}
