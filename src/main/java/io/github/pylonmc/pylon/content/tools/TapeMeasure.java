package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class TapeMeasure extends RebarItem implements RebarBlockInteractor {
    public static final Component INCOMPATIBLE_WORLDS = Component.translatable("pylon.message.tape_measure.incompatible_worlds");
    public static final Component INCOMPLETE_DATA = Component.translatable("pylon.message.tape_measure.incomplete_data");

    public static final NamespacedKey POSITION_ONE_KEY = pylonKey("position_one");
    public static final NamespacedKey POSITION_TWO_KEY = pylonKey("position_two");

    public TapeMeasure(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        boolean isRmb = event.getAction().isRightClick();
        if (player.isSneaking()) {
            if (isRmb) {
                var pdc = getStack().getPersistentDataContainer();
                var pos1 = pdc.get(POSITION_ONE_KEY, RebarSerializers.LOCATION);
                var pos2 = pdc.get(POSITION_TWO_KEY, RebarSerializers.LOCATION);

                if (pos1 == null || pos2 == null) {
                    player.sendMessage(INCOMPLETE_DATA);
                    return;
                }

                if (!pos1.getWorld().equals(pos2.getWorld())) {
                    player.sendMessage(INCOMPATIBLE_WORLDS);
                    return;
                }

                double distance = pos1.distance(pos2);
                player.sendMessage(Component.translatable("pylon.message.tape_measure.success", RebarArgument.of("distance", distance)));
            }

            return;
        }

        if (clicked == null) {
            return;
        }

        Location blockLoc = clicked.getLocation();
        if (isRmb) {
            getStack().editPersistentDataContainer((pdc) -> {
               pdc.set(POSITION_ONE_KEY, RebarSerializers.LOCATION, blockLoc);
            });
            player.sendMessage(
                Component.translatable(
                    "pylon.message.tape_measure.postion_set",
                    RebarArgument.of("index", 1),
                    RebarArgument.of("location", toPrintable(blockLoc))
                )
            );
        } else {
            getStack().editPersistentDataContainer((pdc) -> {
                pdc.set(POSITION_TWO_KEY, RebarSerializers.LOCATION, blockLoc);
            });
            player.sendMessage(
                Component.translatable(
                    "pylon.message.tape_measure.postion_set",
                    RebarArgument.of("index", 2),
                    RebarArgument.of("location", toPrintable(blockLoc))
                )
            );
        }
    }

    private static String toPrintable(Location location) {
        return location.getWorld().getName() + ":" +
            location.getBlockX() + ":" +
            location.getBlockY() + ":" +
            location.getBlockZ();
    }
}
