package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.VanillaFurnaceFuel;
import io.github.pylonmc.rebar.item.interfaces.FurnaceBurnRebarItemHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CharcoalBlock extends RebarItem implements VanillaFurnaceFuel, FurnaceBurnRebarItemHandler {
    public final int fuelBurnTimeTicks = getSettingOrThrow("fuel-burn-time-ticks", ConfigAdapter.INTEGER);

    public CharcoalBlock(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onItemBurnByFurnace(@NotNull FurnaceBurnEvent event, @NotNull EventPriority priority) {
        event.setBurnTime(fuelBurnTimeTicks);
    }
}
