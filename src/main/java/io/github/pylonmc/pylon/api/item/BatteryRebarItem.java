package io.github.pylonmc.pylon.api.item;

import com.google.common.base.Preconditions;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class BatteryRebarItem extends RebarItem implements InteractRebarItemHandler {

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
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick()
                || event.getHand() != EquipmentSlot.HAND
                || event.useItemInHand() == Event.Result.DENY
                || event.hasBlock()) {
            return;
        }

        Player player = event.getPlayer();
        ChargeableRebarItem chargeable = RebarItem.fromStack(player.getInventory().getItemInOffHand(), ChargeableRebarItem.class);
        if (chargeable == null || chargeable.hasBattery()) return;

        chargeable.setBattery(RebarItem.fromStack(getStack().asOne(), BatteryRebarItem.class));
        getStack().subtract();
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("charge", UnitFormat.JOULES.format(getCharge()).decimalPlaces(2).selectPrefixAndRescale()),
                RebarArgument.of("capacity", UnitFormat.JOULES.format(getCapacity()).decimalPlaces(2).selectPrefixAndRescale())
        );
    }
}
