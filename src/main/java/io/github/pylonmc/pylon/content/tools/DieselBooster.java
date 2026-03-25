package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.diesel.DieselRefuelable;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public class DieselBooster extends RebarItem implements RebarInteractor, DieselRefuelable {

    public static final double DIESEL_CAPACITY = Settings.get(PylonKeys.PORTABLE_FLUID_TANK_STEEL).getOrThrow("capacity", ConfigAdapter.DOUBLE) * 2;
    
    public final double dieselPerBoost = getSettings().getOrThrow("diesel-per-boost", ConfigAdapter.DOUBLE);

    public DieselBooster(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("diesel-per-boost", UnitFormat.MILLIBUCKETS.format(dieselPerBoost)),
                RebarArgument.of("diesel", PylonUtils.createFluidAmountBar(
                        getDiesel(),
                        DIESEL_CAPACITY,
                        20,
                        TextColor.fromHexString("#eaa627")
                ))
        );
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick() || 
            event.useItemInHand() == Event.Result.DENY ||
            !player.isGliding() ||
            getDiesel() < dieselPerBoost
        ) {
            return;
        }
        
        Location loc = player.getLocation();
        Firework firework = player.getWorld().spawn(loc, Firework.class);
        firework.setShooter(player);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(2);
        firework.setFireworkMeta(meta);

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
