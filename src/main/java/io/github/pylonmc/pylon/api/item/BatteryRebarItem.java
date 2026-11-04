package io.github.pylonmc.pylon.api.item;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InventoryClickRebarItemHandler;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class BatteryRebarItem extends RebarItem implements InventoryClickRebarItemHandler {

    private static final NamespacedKey CHARGE_KEY = pylonKey("charge");

    public BatteryRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }

    public double getCharge() {
        return getStack().getPersistentDataContainer().getOrDefault(CHARGE_KEY, RebarSerializers.DOUBLE, 0.0);
    }

    public void setCharge(double charge) {
        double clamped = Math.clamp(charge, 0.0, getCapacity());
        getStack().editPersistentDataContainer(pdc -> pdc.set(CHARGE_KEY, RebarSerializers.DOUBLE, clamped));
        updateDurabilityBar(clamped);
    }

    public void charge(double charge) {
        Preconditions.checkArgument(charge >= 0, "charge must be >= 0");
        setCharge(getCharge() + charge);
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
    public void onClickedInCursor(@NotNull InventoryClickEvent event, @NotNull EventPriority priority) {
        ChargeableRebarItem chargeable = RebarItem.fromStack(event.getCurrentItem(), ChargeableRebarItem.class);
        if (chargeable == null || chargeable.hasBattery()) return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            chargeable.setBattery(RebarItem.fromStack(getStack().asOne(), BatteryRebarItem.class));
            getStack().subtract();
        }, 1);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("charge", UnitFormat.JOULES.format(getCharge()).decimalPlaces(2).selectPrefixAndRescale()),
                RebarArgument.of("capacity", UnitFormat.JOULES.format(getCapacity()).decimalPlaces(2).selectPrefixAndRescale())
        );
    }
}
