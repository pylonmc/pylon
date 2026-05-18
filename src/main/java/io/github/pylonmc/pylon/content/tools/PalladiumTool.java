package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarRepairable;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PalladiumTool extends RebarItem implements RebarRepairable {
    public PalladiumTool(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<NamespacedKey> getRepairItems() {
        return List.of(PylonKeys.PALLADIUM_INGOT);
    }
}
