package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.api.item.BatteryHoldingRebarItem;
import io.github.pylonmc.pylon.util.ElectricBolt;
import io.github.pylonmc.rebar.item.interfaces.ProjectileRebarItemHandler;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ElectroBoom extends BatteryHoldingRebarItem implements ProjectileRebarItemHandler {

    public ElectroBoom(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onProjectileLaunched(@NotNull ProjectileLaunchEvent event, @NotNull EventPriority priority) {
        if (hasBattery()) return;
        event.setCancelled(true);
        UUID ownerID = event.getEntity().getOwnerUniqueId();
        if (ownerID != null && Bukkit.getEntity(ownerID) instanceof Player player) {
            player.sendMessage(NO_BATTERY_MESSAGE);
        }
    }

    @Override
    public void onProjectileHit(@NotNull ProjectileHitEvent event, @NotNull EventPriority priority) {
        Location location;
        if (event.getHitEntity() != null) {
            location = event.getHitEntity().getLocation();
        } else if (event.getHitBlock() != null) {
            location = event.getHitBlock().getLocation().toCenterLocation().add(event.getHitBlockFace().getDirection());
        } else {
            return;
        }

        LivingEntity shooter;
        UUID ownerID = event.getEntity().getOwnerUniqueId();
        if (ownerID != null && Bukkit.getEntity(ownerID) instanceof LivingEntity livingEntity) {
            shooter = livingEntity;
        } else {
            shooter = null;
        }

        int count = 1;
        while (ThreadLocalRandom.current().nextBoolean()) {
            count++;
        }

        ElectricBolt.spawn(location, count, Math.cbrt(getCharge()), shooter, e -> !Objects.equals(e, shooter));
    }
}
