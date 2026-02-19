package io.github.pylonmc.pylon.content.building;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarJumpBlock;
import io.github.pylonmc.rebar.block.base.RebarSneakableBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class Elevator extends RebarBlock implements RebarSneakableBlock, RebarJumpBlock {

    public static class Item extends RebarItem {

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of(
                    "elevator_range",
                    UnitFormat.BLOCKS.format(getSettings().getOrThrow("range", ConfigAdapter.INTEGER))
            ));
        }
    }

    private final RandomizedSound useSound = getSettings().getOrThrow("use-sound", ConfigAdapter.RANDOMIZED_SOUND);

    @SuppressWarnings("unused")
    public Elevator(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public Elevator(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    private @NotNull List<RebarBlock> getElevatorsInRange(boolean under, @NotNull Location location) {
        int range = getSettings().getOrThrow("range", ConfigAdapter.INTEGER);
        int checkingLevel = 1;
        List<RebarBlock> blocks = new ArrayList<>();

        while (checkingLevel <= range) {
            location.add(0.0, under ? -1.0 : 1.0, 0.0);
            RebarBlock rebarBlock = BlockStorage.get(location.getBlock());
            if (rebarBlock instanceof Elevator) {
                blocks.add(rebarBlock);
            }
            checkingLevel++;
        }

        return blocks;
    }

    private double getDistance(@NotNull Location playerLocation, @NotNull Location elevatorLocation) {
        return elevatorLocation.y() - playerLocation.y();
    }

    private void teleportPlayer(@NotNull Player player, @NotNull Location location, boolean under) {
        List<RebarBlock> elevators = getElevatorsInRange(under, location);

        if (elevators.isEmpty()) {
            player.sendActionBar(Component.translatable("pylon.message.elevator.none_within_range." + (under ? "below" : "above")));
            return;
        }

        RebarBlock elevator = elevators.getFirst();
        Location elevatorLocation = elevator.getBlock().getLocation();
        double distance = getDistance(player.getLocation(), elevatorLocation);

        player.teleport(player.getLocation().add(0, distance + 1, 0));
        player.getWorld().playSound(useSound.create(), elevatorLocation.x(), elevatorLocation.y(), elevatorLocation.z());
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneakedOn(@NotNull PlayerToggleSneakEvent event, @NotNull EventPriority priority) {
        teleportPlayer(event.getPlayer(), getBlock().getLocation(), true);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJumpedOn(@NotNull PlayerJumpEvent event, @NotNull EventPriority priority) {
        teleportPlayer(event.getPlayer(), getBlock().getLocation(), false);
    }
}
