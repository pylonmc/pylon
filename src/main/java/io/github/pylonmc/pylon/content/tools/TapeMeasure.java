package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class TapeMeasure extends RebarItem implements RebarBlockInteractor {
    public static final Component INCOMPATIBLE_WORLDS = Component.translatable("pylon.message.tape_measure.incompatible_worlds");
    public static final Component INCOMPLETE_DATA = Component.translatable("pylon.message.tape_measure.incomplete_data");

    public static final NamespacedKey POSITION_ONE_KEY = pylonKey("position_one");
    public static final NamespacedKey POSITION_TWO_KEY = pylonKey("position_two");
    public static final NamespacedKey ENTITY_KEY = pylonKey("entity");

    public static final HashMap<UUID, UUID> LINES = new HashMap<>();

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

                Location pos1Above = pos1.clone().add(0, 1, 0);
                Location pos2Above = pos2.clone().add(0, 1, 0);
                pos2Above.setYaw(0);
                pos2Above.setPitch(0);

                player.sendMessage(Component.translatable("pylon.message.tape_measure.success", RebarArgument.of("distance", distance)));
                BlockDisplay display = getOrCreateLine(pos2Above, player);
                updateShowEntity(player);

                display.teleport(pos2Above);
                display.setTransformationMatrix(
                        new LineBuilder()
                                .from(new Vector3f())
                                .to(pos1Above.toVector().subtract(pos2Above.toVector()).normalize().toVector3f().mul((float) distance))
                                .thickness(0.3)
                                .build()
                                .buildForBlockDisplay()
                );
            }

            return;
        }

        if (clicked == null) {
            return;
        }

        Location blockLoc = clicked.getLocation().toCenterLocation();
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

    public @Nullable BlockDisplay getLine() {
        UUID id = getStack().getPersistentDataContainer().get(ENTITY_KEY, RebarSerializers.UUID);
        if (id == null) return null;

        Entity display = Bukkit.getEntity(id);
        if (display == null) return null;

        if (display instanceof BlockDisplay blockDisplay) return blockDisplay;
        return null;
    }

    public @NotNull BlockDisplay getOrCreateLine(Location location, Player player) {
        BlockDisplay display = getLine();
        if (display != null) return display;

        display = location.getWorld().spawn(location, BlockDisplay.class, (entity) -> {
            entity.setVisibleByDefault(false);
            player.showEntity(Pylon.getInstance(), entity);
            entity.setBlock(Material.RED_CONCRETE.createBlockData());
        });
        UUID displayId = display.getUniqueId();
        LINES.put(displayId, player.getUniqueId());
        getStack().editPersistentDataContainer(pdc -> pdc.set(ENTITY_KEY, RebarSerializers.UUID, displayId));
        return display;
    }

    public void updateShowEntity(@NotNull Player player) {
        BlockDisplay display = getLine();
        if (display == null) throw new IllegalStateException("TapeMeasure#updateShowEntity must be called while entity exists");

        UUID other = LINES.get(display.getUniqueId());
        if (other == null || player.getUniqueId().equals(other)) return;

        Player playerOther = Bukkit.getPlayer(other);
        if (playerOther != null) playerOther.hideEntity(Pylon.getInstance(), display);

        player.showEntity(Pylon.getInstance(), display);
    }

    private static String toPrintable(Location location) {
        return location.getWorld().getName() + ":" +
            location.getBlockX() + ":" +
            location.getBlockY() + ":" +
            location.getBlockZ();
    }

    public static final class EntityClear implements Listener {
        @EventHandler
        public void onDisable(PluginDisableEvent event) {
            if (event.getPlugin().equals(Pylon.getInstance())) {
                LINES.keySet().stream()
                        .map(Bukkit::getEntity)
                        .filter(Objects::nonNull)
                        .forEach(Entity::remove);
            }
        }
    }
}
