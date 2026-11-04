package io.github.pylonmc.pylon.content.assembling;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.api.item.BatteryRebarItem;
import io.github.pylonmc.pylon.api.item.ChargeableRebarItem;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.BlockInteractRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.InventoryClickRebarItemHandler;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class Screwdriver extends RebarItem implements BlockInteractRebarItemHandler, InventoryClickRebarItemHandler {

    public final String toolType = getSetting("tool-type", ConfigAdapter.STRING);
    public final int cooldownTicks = getSettingOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);

    public Screwdriver(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0))
        );
    }

    @Override
    @MultiHandler(priorities = {EventPriority.NORMAL, EventPriority.MONITOR})
    public void onInteractWithBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isLeftClick()
                || event.getHand() != EquipmentSlot.HAND
                || event.useItemInHand() == Event.Result.DENY
                || !(BlockStorage.get(event.getClickedBlock()) instanceof AssemblyTable assemblyTable)) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        } else if (event.getPlayer().hasCooldown(getStack())) {
            return;
        }

        if (assemblyTable.useTool(toolType, event.getPlayer())) {
            getStack().damage(1, event.getPlayer());
            event.getPlayer().setCooldown(PylonKeys.SCREWDRIVER, cooldownTicks);
        }
    }

    @Override
    public void onClickedInCursor(@NotNull InventoryClickEvent event, @NotNull EventPriority priority) {
        ChargeableRebarItem chargeable = RebarItem.fromStack(event.getCurrentItem(), ChargeableRebarItem.class);
        if (chargeable == null) return;

        BatteryRebarItem battery = chargeable.getBattery();
        if (battery == null) return;
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            chargeable.setBattery(null);
            getStack().damage(1, event.getWhoClicked());
            RebarUtils.addToInventoryOrDrop(event.getWhoClicked(), battery.getStack());
        }, 1);
    }

    @Override
    public boolean respectCooldown() {
        return false;
    }
}
