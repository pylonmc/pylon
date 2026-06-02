package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.diesel.DieselRefuelable;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;

import java.util.List;

import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class DieselBooster extends RebarItem implements InteractRebarItemHandler, DieselRefuelable {

    public static final double DIESEL_CAPACITY = ConfigSection.fromSettings(PylonKeys.PORTABLE_FLUID_TANK_COPPER)
            .getOrThrow("capacity", ConfigAdapter.DOUBLE) * 2;
    
    public final double dieselPerBoost = getSettingOrThrow("diesel-per-boost", ConfigAdapter.DOUBLE);

    public DieselBooster(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("diesel-per-boost", UnitFormat.MILLIBUCKETS.format(dieselPerBoost)),
                RebarArgument.of("diesel", ProgressBar.fluidContents(
                        PylonFluids.BIODIESEL,
                        DIESEL_CAPACITY,
                        getDiesel()
                ))
        );
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick() || 
            event.useItemInHand() == Event.Result.DENY ||
            !player.isGliding() ||
            getDiesel() < dieselPerBoost
        ) {
            return;
        }
        
        player.launchProjectile(Firework.class, new Vector(), firework -> {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(4);
            firework.setFireworkMeta(meta);
            firework.setAttachedTo(player);
        });

        setDiesel(getDiesel() - dieselPerBoost);
    }

    @Override
    public double getDiesel() {
        return getStack().getPersistentDataContainer().get(PylonFluids.BIODIESEL.getKey(), RebarSerializers.DOUBLE);
    }

    @Override
    public void setDiesel(double amount) {
        getStack().editPersistentDataContainer(pdc -> {
            pdc.set(PylonFluids.BIODIESEL.getKey(), RebarSerializers.DOUBLE, amount);
        });
    }

    @Override
    public double getDieselCapacity() {
        return DIESEL_CAPACITY;
    }
}
