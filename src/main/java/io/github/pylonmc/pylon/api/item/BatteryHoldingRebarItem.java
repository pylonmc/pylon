package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.content.assembling.Screwdriver;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class BatteryHoldingRebarItem extends RebarItem implements ChargeableRebarItem {

    private static final NamespacedKey BATTERY_KEY = pylonKey("battery");

    public static final Component NO_BATTERY_MESSAGE = Component.translatable("pylon.message.no_battery");

    private @Nullable BatteryRebarItem battery = null; // cache field to prevent multiple deserializations

    public BatteryHoldingRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }

    public boolean hasBattery() {
        return battery != null || getStack().getPersistentDataContainer().has(BATTERY_KEY);
    }

    public @Nullable BatteryRebarItem getBattery() {
        if (battery == null) {
            ItemStack item = this.getStack().getPersistentDataContainer().get(BATTERY_KEY, RebarSerializers.ITEM_STACK);
            battery = RebarItem.fromStack(item, BatteryRebarItem.class);
        }
        return battery;
    }

    public void setBattery(@Nullable BatteryRebarItem battery) {
        this.battery = battery;
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

    @Override
    public double getCharge() {
        BatteryRebarItem battery = getBattery();
        return battery == null ? 0 : battery.getCharge();
    }

    /**
     * Will no-op if no battery is inside
     */
    @Override
    public void setCharge(double charge) {
        BatteryRebarItem battery = getBattery();
        if (battery == null) return;
        battery.setCharge(charge);
        setBattery(battery);
    }

    @Override
    public double getCapacity() {
        BatteryRebarItem battery = getBattery();
        return battery == null ? 0 : battery.getCapacity();
    }

    @Override
    public boolean isRechargeable() {
        BatteryRebarItem battery = getBattery();
        return battery != null && battery.isRechargeable();
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        List<RebarArgument> placeholders = new ArrayList<>();
        BatteryRebarItem battery = getBattery();
        ComponentLike charge = battery == null ?
                NO_BATTERY_MESSAGE :
                UnitFormat.JOULES.format(battery.getCharge()).decimalPlaces(2).selectPrefixAndRescale();
        placeholders.add(RebarArgument.of("charge", charge));
        return placeholders;
    }

    public static class CraftListener implements Listener {
        @EventHandler
        private void onPrepareCraft(PrepareItemCraftEvent event) {
            CraftingInventory inventory = event.getInventory();
            screwdriverRecipes.remove(inventory);
            BatteryHoldingRebarItem chargeable = null;
            int chargeableSlot = 0;
            BatteryRebarItem battery = null;
            Screwdriver screwdriver = null;
            int screwdriverSlot = 0;
            int items = 0;
            ItemStack[] matrix = inventory.getMatrix();
            for (int slot = 0; slot < matrix.length; slot++) {
                ItemStack item = matrix[slot];
                if (item != null) {
                    items++;
                }
                if (items > 2) return;

                BatteryHoldingRebarItem potentiallyChargeable = RebarItem.fromStack(item, BatteryHoldingRebarItem.class);
                if (potentiallyChargeable != null) {
                    if (chargeable != null) return; // two chargeables
                    chargeable = potentiallyChargeable;
                    chargeableSlot = slot;
                    continue;
                }

                BatteryRebarItem potentialBattery = RebarItem.fromStack(item, BatteryRebarItem.class);
                if (potentialBattery != null) {
                    if (battery != null) return; // two batteries
                    battery = potentialBattery;
                    continue;
                }

                Screwdriver potentialScrewdriver = RebarItem.fromStack(item, Screwdriver.class);
                if (potentialScrewdriver != null) {
                    if (screwdriver != null) return; // two screwdrivers
                    screwdriver = potentialScrewdriver;
                    screwdriverSlot = slot;
                }
            }
            if (chargeable == null || (battery != null && screwdriver != null)) return;

            if (battery != null) {
                if (chargeable.hasBattery()) return;
                BatteryHoldingRebarItem result = RebarItem.fromStack(chargeable.getStack().asOne(), BatteryHoldingRebarItem.class);
                result.setBattery(battery);
                inventory.setResult(result.getStack());
            } else if (screwdriver != null) {
                battery = chargeable.getBattery();
                if (battery == null) return;
                inventory.setResult(battery.getStack());
                screwdriverRecipes.put(inventory, new ScrewdriverRecipe(chargeable, chargeableSlot, screwdriver, screwdriverSlot));
            }
        }

        private record ScrewdriverRecipe(
                BatteryHoldingRebarItem chargeable,
                int chargeableSlot,
                Screwdriver screwdriver,
                int screwdriverSlot
        ) {
        }

        private final Map<CraftingInventory, ScrewdriverRecipe> screwdriverRecipes = new HashMap<>();

        @EventHandler
        private void onCraft(InventoryClickEvent event) {
            if (!(event.getInventory() instanceof CraftingInventory inventory) || event.getRawSlot() != 0) return;
            ScrewdriverRecipe recipe = screwdriverRecipes.remove(inventory);
            if (recipe == null) return;
            ItemStack result = inventory.getResult();
            if (result == null) return;

            ItemStack[] matrix = inventory.getMatrix();
            BatteryHoldingRebarItem chargeable = recipe.chargeable();
            chargeable.setBattery(null);
            matrix[recipe.chargeableSlot()] = chargeable.getStack();

            Screwdriver screwdriver = recipe.screwdriver();
            screwdriver.getStack().damage(1, event.getWhoClicked());
            matrix[recipe.screwdriverSlot()] = screwdriver.getStack();

            inventory.setMatrix(matrix);

            Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> event.getWhoClicked().setItemOnCursor(result));
        }
    }
}
