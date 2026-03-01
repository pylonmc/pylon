package io.github.pylonmc.pylon.content.machines.experience;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LiquidXPBottle extends RebarItem {
    public final int experienceAmount = getSettings().getOrThrow("experience-amount", ConfigAdapter.INTEGER);

    public LiquidXPBottle(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("experience-amount", UnitFormat.EXPERIENCE.format(experienceAmount)));
    }

    public static class XPBottleListener implements Listener {
        @EventHandler
        public void onPlayerInteract(@NotNull ExpBottleEvent event) {
            RebarItem item = RebarItem.fromStack(event.getEntity().getItem());
            if (!(item instanceof LiquidXPBottle bottle)) {
                return;
            }
            event.setExperience(bottle.experienceAmount);
        }
    }
}
