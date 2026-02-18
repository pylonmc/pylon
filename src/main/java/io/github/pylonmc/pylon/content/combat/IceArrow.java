package io.github.pylonmc.pylon.content.combat;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarArrow;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class IceArrow extends RebarItem implements RebarArrow {
    private final int freezeDuration = getSettings().getOrThrow("freeze-duration", ConfigAdapter.INTEGER);
    private final double freezeSpeed = getSettings().getOrThrow("freeze-speed", ConfigAdapter.DOUBLE);

    public IceArrow(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onArrowDamage(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            // Has to be run every tick or effect will flicker in and out from game resetting it since the player isn't in a powdered snow block
            new DamageOverTimeRunnable((LivingEntity) event.getEntity(), freezeDuration, Bukkit.getCurrentTick(), (float) freezeSpeed).runTaskTimer(Pylon.getInstance(), 0, 1);
        }
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("freeze-duration", freezeDuration));
    }

    private static class DamageOverTimeRunnable extends BukkitRunnable {
        private final LivingEntity applyTo;
        private final int freezeDuration;
        private final int startTick;
        private final float freezeSpeed;

        public DamageOverTimeRunnable(LivingEntity applyTo, int freezeDuration, int startTick, float freezeSpeed) {
            this.applyTo = applyTo;
            this.freezeDuration = freezeDuration;
            this.startTick = startTick;
            this.freezeSpeed = freezeSpeed;
        }

        @Override
        public void run() {
            if (Bukkit.getCurrentTick() - startTick > freezeDuration) {
                applyTo.setFreezeTicks(0);
                this.cancel();
            }
            // This is the only thing I could find that works to apply this effect, it's not the greatest but too bad.
            applyTo.setFreezeTicks(Math.round((Bukkit.getCurrentTick() - startTick) * freezeSpeed) + applyTo.getMaxFreezeTicks());
        }
    }
}
