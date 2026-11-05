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

public abstract class BatteryRebarItem extends RebarItem implements ChargeableRebarItem {

    private static final NamespacedKey CHARGE_KEY = pylonKey("charge");

    public BatteryRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Must be called by subclasses in their constructor. The reason this exists is that we need to create/update the durability bar
     * on creation, but can't in the superclass constructor because {@link BatteryRebarItem#getCapacity()} is abstract and may rely
     * on subclass resources, e.x. {@link ConfigurableBatteryRebarItem}.
     */
    protected void postCapacityConfiguration() {
        updateDurabilityBar(getCharge());
    }

    @Override
    public double getCharge() {
        return getStack().getPersistentDataContainer().getOrDefault(CHARGE_KEY, RebarSerializers.DOUBLE, 0.0);
    }

    @Override
    public void setCharge(double charge) {
        double clamped = Math.clamp(charge, 0.0, getCapacity());
        getStack().editPersistentDataContainer(pdc -> pdc.set(CHARGE_KEY, RebarSerializers.DOUBLE, clamped));
        updateDurabilityBar(clamped);
    }

    @Override
    public boolean isRechargeable() {
        return true;
    }

    private void updateDurabilityBar(double charge) {
        getStack().setData(DataComponentTypes.MAX_DAMAGE, 1001);
        getStack().setData(DataComponentTypes.DAMAGE, 1000 - (int) (charge / getCapacity() * 1000) + 1);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("charge", UnitFormat.JOULES.format(getCharge()).decimalPlaces(2).selectPrefixAndRescale()),
                RebarArgument.of("capacity", UnitFormat.JOULES.format(getCapacity()).decimalPlaces(2).selectPrefixAndRescale())
        );
    }
}
