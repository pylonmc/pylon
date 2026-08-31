package io.github.pylonmc.pylon.content.building;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.JumpRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.SneakRebarBlockHandler;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class Elevator extends RebarBlock implements SneakRebarBlockHandler, JumpRebarBlockHandler {

    public static class Item extends RebarItem {

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of(
                    "elevator_range",
                    UnitFormat.BLOCKS.format(getSettingOrThrow("range", ConfigAdapter.INTEGER))
            ));
        }
    }

    private final RandomizedSound useSound = getSettingOrThrow("use-sound", ConfigAdapter.RANDOMIZED_SOUND);
    private final int range = getSettingOrThrow("range", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public Elevator(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public Elevator(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    private @NotNull List<Elevator> getElevatorsInRange(boolean under) {
        BlockPosition position = new BlockPosition(getBlock());
        List<Elevator> elevators = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            position.addScalar(0, under ? -1 : 1, 0);
            Elevator elevator = BlockStorage.getAs(Elevator.class, position);
            if (elevator != null) {
                elevators.add(elevator);
            }
        }
        return elevators;
    }

    private void teleportPlayer(@NotNull Player player, boolean under) {
        List<Elevator> elevators = getElevatorsInRange(under);
        if (elevators.isEmpty()) {
            player.sendActionBar(Component.translatable("pylon.message.elevator.none_within_range." + (under ? "below" : "above")));
            return;
        }

        Elevator elevator = elevators.getFirst();
        Block elevatorBlock = elevator.getBlock();
        player.teleport(player.getLocation().add(0, (elevatorBlock.getY() - player.getY()) + 1, 0));
        useSound.play(elevatorBlock);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneakStart(@NotNull PlayerToggleSneakEvent event, @NotNull EventPriority priority) {
        teleportPlayer(event.getPlayer(), true);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJumpedOn(@NotNull PlayerJumpEvent event, @NotNull EventPriority priority) {
        teleportPlayer(event.getPlayer(), false);
    }
}
