package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.ConsumeRebarItemHandler;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HealingConsumable extends RebarItem implements ConsumeRebarItemHandler {
    private final double consumeSeconds = getSettingOrThrow("consume-seconds", ConfigAdapter.DOUBLE);
    private final double healAmount = getSettingOrThrow("heal-amount", ConfigAdapter.DOUBLE);
    public HealingConsumable(@NotNull ItemStack stack){ super(stack); }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("consume-seconds", UnitFormat.SECONDS.format(consumeSeconds)),
                RebarArgument.of("heal-amount", UnitFormat.HEARTS.format(healAmount))
        );
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsumed(@NotNull PlayerItemConsumeEvent event, @NotNull EventPriority priority) {
        event.getPlayer().heal(healAmount);
    }
}
