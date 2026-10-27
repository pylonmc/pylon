package io.github.pylonmc.pylon.api.item;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConfigurableBatteryRebarItem extends BatteryRebarItem {

    @Getter(onMethod_ = @Override)
    private final double capacity = getSettingOrThrow("capacity", ConfigAdapter.DOUBLE);

    public ConfigurableBatteryRebarItem(@NotNull ItemStack stack) {
        super(stack);
    }
}
