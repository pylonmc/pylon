package io.github.pylonmc.pylon.content.machines.experience;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBottle;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LiquidXPBottle extends RebarItem implements RebarBottle {
    public final int experienceAmount = getSettings().getOrThrow("experience-amount", ConfigAdapter.INTEGER);

    public LiquidXPBottle(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("experience-amount", UnitFormat.EXPERIENCE.format(experienceAmount)));
    }

    @Override
    public void onBottleBreak(@NotNull ExpBottleEvent event) {
        event.setExperience(experienceAmount);
    }
}
