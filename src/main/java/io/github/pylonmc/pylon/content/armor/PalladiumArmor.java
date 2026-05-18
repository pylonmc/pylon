package io.github.pylonmc.pylon.content.armor;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarArmor;
import io.github.pylonmc.rebar.item.base.RebarRepairable;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PalladiumArmor extends RebarItem implements RebarArmor, RebarRepairable {
    public PalladiumArmor(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<NamespacedKey> getRepairItems() {
        return List.of(PylonKeys.PALLADIUM_INGOT);
    }

    @Override
    public @NotNull Key getEquipmentType() {
        return PylonUtils.pylonKey("palladium");
    }
}
