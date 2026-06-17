package io.github.pylonmc.pylon.content.machines.electricity;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import java.util.Comparator;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Multimeter extends RebarItem implements InteractRebarItemHandler {
    public Multimeter(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Location playerLocation = event.getPlayer().getLocation();
        ElectricityPylon pylon = (ElectricityPylon) BlockStorage.getByType(ElectricityPylon.class).stream()
                .min(Comparator.comparing(p -> p.getBlock().getLocation().toCenterLocation().distanceSquared(playerLocation)))
                .filter(p -> p.getBlock().getLocation().toCenterLocation().distanceSquared(playerLocation) < 64 * 64)
                .orElse(null);
        if (pylon == null) return;
        throw new UnsupportedOperationException("Multimeter interaction not implemented yet");
    }
}
