package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class TapeMeasure extends RebarItem {

    public final Material material = getSettings().getOrThrow("material", ConfigAdapter.MATERIAL);
    public final Material finishedMaterial = getSettings().getOrThrow("finished-material", ConfigAdapter.MATERIAL);
    public final double thickness = getSettings().getOrThrow("thickness", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public TapeMeasure(@NotNull ItemStack stack) {
        super(stack);
    }

    private static class TapeMeasureTask extends BukkitRunnable {
        public final Location startLocation;
        private Location previousEndLocation;
        public final UUID playerId;
        public final UUID lineId;
        public final UUID textId;
        public final int tickInterval;
        public final double thickness;
        public final Material finishedMaterial;

        public TapeMeasureTask(
                Location startLocation,
                UUID playerId,
                UUID lineId,
                UUID textId,
                int tickInterval,
                double thickness,
                Material finishedMaterial
        ) {
            super();
            this.startLocation = startLocation;
            this.playerId = playerId;
            this.lineId = lineId;
            this.textId = textId;
            this.tickInterval = tickInterval;
            this.thickness = thickness;
            this.finishedMaterial = finishedMaterial;
        }

        @Override
        public void run() {
            ItemDisplay line = (ItemDisplay) Bukkit.getEntity(lineId);
            TextDisplay text = (TextDisplay) Bukkit.getEntity(textId);
            if (line == null || text == null) {
                TapeMeasureService.cancel(playerId);
                return;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                TapeMeasureService.cancel(playerId);
                return;
            }

            Location endLocation = getPlayerTarget(player);
            if (endLocation == null) {
                return;
            }

            if (!endLocation.getWorld().equals(startLocation.getWorld())) {
                TapeMeasureService.cancel(player.getUniqueId());
                return;
            }

            if (previousEndLocation == null || previousEndLocation.distance(endLocation) > 0.01) {
                int animationTime = tickInterval;
                if (line.getLocation().distance(endLocation) > 16) {
                    // Teleport line closer to ensure it doesn't get so far away that it stops being rendered
                    // or despawns
                    line.teleport(endLocation);
                    animationTime = 0;
                }

                text.setTeleportDuration(tickInterval);
                Vector3d eyeAdjustment = PylonUtils.getDirection(endLocation, player.getEyeLocation())
                        .normalize()
                        .mul(0.3);
                text.teleport(endLocation.clone().add(Vector.fromJOML(eyeAdjustment)));
                text.text(UnitFormat.BLOCKS.format(startLocation.clone().distance(endLocation))
                        .decimalPlaces(2)
                        .asComponent()
                );

                PylonUtils.animate(line, animationTime, new LineBuilder()
                        .from(startLocation.clone().subtract(line.getLocation()).toVector().toVector3d())
                        .to(endLocation.clone().subtract(line.getLocation()).toVector().toVector3d())
                        .thickness(thickness)
                        .extraLength(thickness)
                        .build()
                        .buildForItemDisplay()
                );

                previousEndLocation = endLocation;
            }
        }

        private static @Nullable Location getPlayerTarget(@NonNull Player player) {
            AttributeInstance blockInteractionRange = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
            double reach = 4.5;
            if (blockInteractionRange != null) {
                reach = blockInteractionRange.getValue();
            }

            RayTraceResult result = player.rayTraceBlocks(reach);
            if (result != null && result.getHitBlock() != null) {
                Vector vector = result.getHitPosition();
                return new Location(result.getHitBlock().getWorld(), vector.getX(), vector.getY(), vector.getZ());
            }

            return null;
        }
    }

    private static final class TapeMeasureService {

        private static final Map<UUID, TapeMeasureTask> tasks = new HashMap<>();

        public static boolean isMeasuring(@NotNull Player player) {
            return tasks.containsKey(player.getUniqueId());
        }

        public static void start(@NotNull Player player, @NotNull Location startLocation, @NotNull TapeMeasure tapeMeasure) {
            if (isMeasuring(player)) {
                cancel(player.getUniqueId());
            }

            ItemDisplay line = new ItemDisplayBuilder()
                    .material(tapeMeasure.material)
                    .persistent(false)
                    .transformation(new TransformBuilder().scale(0))
                    .build(startLocation);

            TextDisplay text = new TextDisplayBuilder()
                    .persistent(false)
                    .transformation(new TransformBuilder()
                            .translate(0, 0.2, 0)
                            .scale(0.5)
                    )
                    .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                    .billboard(Display.Billboard.VERTICAL)
                    .build(startLocation);

            TapeMeasureTask task = new TapeMeasureTask(
                    startLocation,
                    player.getUniqueId(),
                    line.getUniqueId(),
                    text.getUniqueId(),
                    tapeMeasure.tickInterval,
                    tapeMeasure.thickness,
                    tapeMeasure.finishedMaterial
            );
            task.runTaskTimer(Pylon.getInstance(), 0, tapeMeasure.tickInterval);
            tasks.put(player.getUniqueId(), task);
        }

        private static void finish(@NotNull Player player) {
            TapeMeasureTask task = tasks.get(player.getUniqueId());
            task.cancel();

            if (task.lineId != null) {
                if (Bukkit.getEntity(task.lineId) instanceof ItemDisplay line) {
                    line.setItemStack(new ItemStack(task.finishedMaterial));
                }
            }
        }

        private static void cancel(@NotNull UUID uuid) {
            TapeMeasureTask task = tasks.remove(uuid);
            if (task == null) {
                return;
            }

            task.cancel();

            if (task.lineId != null) {
                Entity line = Bukkit.getEntity(task.lineId);
                if (line != null) {
                    line.remove();
                }
            }

            if (task.textId != null) {
                Entity text = Bukkit.getEntity(task.textId);
                if (text != null) {
                    text.remove();
                }
            }
        }
    }

    public static class TapeMeasureListener implements Listener {

        @EventHandler
        private static void onPlayerScroll(@NonNull PlayerItemHeldEvent event) {
            ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
            if (isRebarItem(heldItem, TapeMeasure.class)) {
                TapeMeasureService.cancel(event.getPlayer().getUniqueId());
            }
        }

        // includes dropping
        @EventHandler
        private static void onInventoryChange(@NonNull PlayerInventorySlotChangeEvent event) {
            if (event.getSlot() == event.getPlayer().getInventory().getHeldItemSlot()
                    && isRebarItem(event.getOldItemStack(), TapeMeasure.class)
                    && !isRebarItem(event.getNewItemStack(), TapeMeasure.class)) {
                TapeMeasureService.cancel(event.getPlayer().getUniqueId());
            }
        }

        @EventHandler
        public static void onInteract(@NotNull PlayerInteractEvent event) {
            // We can't use interact handlers because they don't fire for off-hand if player left clicks
            TapeMeasure mainHand = fromStack(event.getPlayer().getInventory().getItemInMainHand(), TapeMeasure.class);
            TapeMeasure offHand = fromStack(event.getPlayer().getInventory().getItemInOffHand(), TapeMeasure.class);
            if (offHand == null && mainHand == null) {
                return;
            }

            if (event.getHand() == EquipmentSlot.OFF_HAND && event.getAction().isRightClick()) {
                // Don't fire twice for right clicks
                return;
            }

            TapeMeasure tapeMeasure = mainHand == null ? offHand : mainHand;

            if (event.getAction().isLeftClick()) {
                if (TapeMeasureService.isMeasuring(event.getPlayer())) {
                    TapeMeasureService.cancel(event.getPlayer().getUniqueId());
                    return;
                }
            }

            if (event.getAction().isRightClick() && event.getInteractionPoint() != null) {
                if (TapeMeasureService.isMeasuring(event.getPlayer())) {
                    TapeMeasureService.finish(event.getPlayer());
                    return;
                }

                TapeMeasureService.start(event.getPlayer(), event.getInteractionPoint(), tapeMeasure);
            }
        }
    }
}
