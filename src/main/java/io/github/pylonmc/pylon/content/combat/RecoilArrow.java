package io.github.pylonmc.pylon.content.combat;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.ArrowRebarItemHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class RecoilArrow extends RebarItem implements ArrowRebarItemHandler {

    public final double efficiency = getSettingOrThrow("efficiency", ConfigAdapter.DOUBLE);

    public RecoilArrow(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunched(@NotNull ProjectileLaunchEvent event, @NotNull EventPriority priority) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Entity shooter) {
            shooter.setVelocity(shooter.getVelocity().add(projectile.getVelocity().multiply(-efficiency)));
        }
    }

}