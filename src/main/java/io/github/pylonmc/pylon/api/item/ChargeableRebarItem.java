package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
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

    public boolean hasBattery() {
        return getStack().getPersistentDataContainer().has(BATTERY_KEY);
    }

    public @Nullable BatteryRebarItem getBattery() {
        ItemStack item = this.getStack().getPersistentDataContainer().get(BATTERY_KEY, RebarSerializers.ITEM_STACK);
        return RebarItem.fromStack(item, BatteryRebarItem.class);
    }

    public void setBattery(@Nullable BatteryRebarItem battery) {
        ItemStack stack = getStack();
        if (battery != null) {
            ItemStack batteryStack = battery.getStack();
            stack.copyDataFrom(batteryStack, (type) -> type.equals(DataComponentTypes.DAMAGE) || type.equals(DataComponentTypes.MAX_DAMAGE));
            stack.editPersistentDataContainer(pdc -> pdc.set(BATTERY_KEY, RebarSerializers.ITEM_STACK, battery.getStack()));
        } else {
            stack.unsetData(DataComponentTypes.DAMAGE);
            stack.unsetData(DataComponentTypes.MAX_DAMAGE);
            stack.editPersistentDataContainer(pdc -> pdc.remove(BATTERY_KEY));
        }
    }

    public double getCharge() {
        BatteryRebarItem battery = getBattery();
        return battery == null ? 0 : battery.getCharge();
    }

    /**
     * Will no-op if no battery is inside
     */
    public void setCharge(double charge) {
        BatteryRebarItem battery = getBattery();
        if (battery == null) return;
        battery.setCharge(charge);
        setBattery(battery);
    }

    /**
     * Will no-op if no battery is inside
     */
    public void charge(double charge) {
        BatteryRebarItem battery = getBattery();
        if (battery == null) return;
        battery.charge(charge);
        setBattery(battery);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        List<RebarArgument> placeholders = new ArrayList<>();
        BatteryRebarItem battery = getBattery();
        ComponentLike charge = battery == null ?
                Component.translatable("pylon.message.no_battery") :
                UnitFormat.JOULES.format(battery.getCharge()).decimalPlaces(2).selectPrefixAndRescale();
        placeholders.add(RebarArgument.of("charge", charge));
        return placeholders;
    }
}
