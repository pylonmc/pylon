package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class BatteryRebarItem extends RebarItem {

    private static final NamespacedKey CHARGE_KEY = pylonKey("charge");

    public BatteryRebarItem(@NotNull ItemStack stack) {
        super(stack);
        updateDurabilityBar(getCharge());
    }

    public double getCharge() {
        return getStack().getPersistentDataContainer().getOrDefault(CHARGE_KEY, RebarSerializers.DOUBLE, 0.0);
    }

    public void setCharge(double charge) {
        getStack().editPersistentDataContainer(pdc -> pdc.set(CHARGE_KEY, RebarSerializers.DOUBLE, Math.clamp(charge, 0.0, getCapacity())));
        updateDurabilityBar(charge);
    }

    private void updateDurabilityBar(double charge) {
        getStack().setData(DataComponentTypes.MAX_DAMAGE, 1000);
        getStack().setData(DataComponentTypes.DAMAGE, 1000 - (int) (charge / getCapacity() * 1000));
    }

    public boolean isRechargeable() {
        return true;
    }

    public abstract double getCapacity();

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("charge", UnitFormat.JOULES.format(getCharge()).decimalPlaces(2).selectPrefixAndRescale()),
                RebarArgument.of("capacity", UnitFormat.JOULES.format(getCapacity()).decimalPlaces(2).selectPrefixAndRescale())
        );
    }
}
