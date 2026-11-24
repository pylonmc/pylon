package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.api.item.CannonProjectileRebarItem;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SodiumProjectile extends SodiumItem implements CannonProjectileRebarItem {

    public SodiumProjectile(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onHit(@NotNull Location location, @NotNull Damageable entity) {
        location.createExplosion(getExplosionPower(), true);
    }
}
