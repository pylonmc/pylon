package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.RepairableRebarItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BronzeTool extends RebarItem implements RepairableRebarItem {
    public BronzeTool(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<NamespacedKey> getRepairItems() {
        return List.of(PylonKeys.BRONZE_INGOT);
    }
}
