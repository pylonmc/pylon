package io.github.pylonmc.pylon.content.combat;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarArrow;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class RecoilArrow extends RebarItem implements RebarArrow {

    public final double efficiency = getSettings().getOrThrow("efficiency", ConfigAdapter.DOUBLE);

    public RecoilArrow(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArrowShotFromBow(@NotNull EntityShootBowEvent event, @NotNull EventPriority priority) {
        event.getEntity().setVelocity(event.getEntity().getVelocity().add(event.getProjectile().getVelocity().multiply(-efficiency)));
    }
}