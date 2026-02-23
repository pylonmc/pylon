package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.VanillaCookingFuel;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CharcoalBlock extends RebarItem implements VanillaCookingFuel {
    public static final int FUEL_BURN_TIME_TICKS = Settings.get(PylonKeys.CHARCOAL_BLOCK).getOrThrow("fuel-burn-time-ticks", ConfigAdapter.INTEGER);
    public CharcoalBlock(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onBurntAsFuel(@NotNull FurnaceBurnEvent event, @NotNull EventPriority priority) {
        event.setBurnTime(FUEL_BURN_TIME_TICKS);
    }
}
