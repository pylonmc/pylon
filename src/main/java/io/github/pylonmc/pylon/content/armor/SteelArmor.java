package io.github.pylonmc.pylon.content.armor;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarArmor;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SteelArmor extends RebarItem implements RebarArmor {
    public SteelArmor(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull Key getEquipmentType() {
        return PylonUtils.pylonKey("steel");
    }
}
