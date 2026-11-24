package io.github.pylonmc.pylon.api.item;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.jetbrains.annotations.NotNull;

public interface CannonProjectileRebarItem {
    void onHit(@NotNull Location location, @NotNull Damageable entity);
}
