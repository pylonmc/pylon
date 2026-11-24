package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.api.item.CannonProjectileRebarItem;
import io.github.pylonmc.rebar.item.RebarItem;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BasicCannonProjectileRebarItem extends RebarItem implements CannonProjectileRebarItem {

    public BasicCannonProjectileRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onHit(@NotNull Location location, @NotNull Damageable entity) {

    }
}
