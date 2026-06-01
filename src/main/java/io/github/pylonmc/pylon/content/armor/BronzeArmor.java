package io.github.pylonmc.pylon.content.armor;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.ArmorRebarItem;
import io.github.pylonmc.rebar.item.interfaces.RepairableRebarItem;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BronzeArmor extends RebarItem implements ArmorRebarItem, RepairableRebarItem {
    public BronzeArmor(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<NamespacedKey> getRepairItems() {
        return List.of(PylonKeys.BRONZE_INGOT);
    }

    @Override
    public @NotNull Key getEquipmentType() {
        return PylonUtils.pylonKey("bronze");
    }
}
