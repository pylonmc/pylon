package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.api.item.ChargeableRebarItem;
import io.github.pylonmc.pylon.util.ElectricBolt;
import io.github.pylonmc.rebar.item.interfaces.ProjectileRebarItemHandler;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ElectroBoom extends ChargeableRebarItem implements ProjectileRebarItemHandler {

    public ElectroBoom(@NotNull ItemStack stack) {
        super(stack);
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

        Set<LivingEntity> shooter = new HashSet<>();
        UUID ownerID = event.getEntity().getOwnerUniqueId();
        if (ownerID != null && Bukkit.getEntity(ownerID) instanceof LivingEntity livingEntity) {
            shooter.add(livingEntity);
        }

        ElectricBolt.spawn(location, 1000, shooter);
    }
}
