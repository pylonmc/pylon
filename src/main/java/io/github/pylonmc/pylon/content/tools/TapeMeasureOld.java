package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarDroppable;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;


public class TapeMeasureOld extends RebarItem implements RebarInteractor, RebarDroppable {

    public static final NamespacedKey LINE_KEY = PylonUtils.pylonKey("line");
    public static final NamespacedKey TEXT_KEY = PylonUtils.pylonKey("text");
    public static final NamespacedKey TASK_KEY = PylonUtils.pylonKey("task");

    public final Material material = getSettings().getOrThrow("material", ConfigAdapter.MATERIAL);
    public final Material finishedMaterial = getSettings().getOrThrow("finished-material", ConfigAdapter.MATERIAL);
    public final double thickness = getSettings().getOrThrow("thickness", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public TapeMeasureOld(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND || event.getInteractionPoint() == null) {
            return;
        }

        if (!event.getAction().isRightClick()) {
            return;
        }

        if (getLine() == null || Bukkit.getEntity(getLine()) == null) {
            // line does not exist, start measuring
            Location startLocation = event.getInteractionPoint();
            ItemDisplay line = new ItemDisplayBuilder()
                    .material(material)
                    .persistent(false)
                    .transformation(new TransformBuilder().scale(0))
                    .build(startLocation);
            updateLine(line, startLocation, startLocation, tickInterval, thickness);

            int taskId = new TapeMeasureTask(startLocation, line.getUniqueId(), event.getPlayer().getUniqueId(), tickInterval, thickness)
                    .runTaskTimer(Pylon.getInstance(), tickInterval, tickInterval)
                    .getTaskId();

            setLine(line.getUniqueId());
            setTask(taskId);
        } else {
            // cancel update task
            Integer task = getTask();
            if (task != null) {
                Bukkit.getScheduler().cancelTask(task);
                setTask(null);
            }

            // change line material
            UUID lineId =  getLine();
            if (Bukkit.getEntity(lineId) instanceof ItemDisplay line) {
                line.setItemStack(new ItemStack(finishedMaterial));
            }
        }
    }

    @Override
    public void onDropped(@NotNull PlayerDropItemEvent event, @NotNull EventPriority priority) {
        cancelMeasurement();
    }

    public static class TapeMeasureTask extends BukkitRunnable {

        private final Location startlocation;
        private Location previousEndLocation;
        private final UUID lineId;
        private final UUID playerId;
        private final int tickInterval;
        private final double thickness;

        public TapeMeasureTask(Location startlocation, UUID lineId, UUID playerId, int tickInterval, double thickness) {
            super();
            this.startlocation = startlocation;
            this.lineId = lineId;
            this.playerId = playerId;
            this.tickInterval = tickInterval;
            this.thickness = thickness;
        }

        @Override
        public void run() {
            ItemDisplay line = (ItemDisplay) Bukkit.getEntity(lineId);
            if (line == null) {
                cancel();
                return;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                line.remove();
                cancel();
                return;
            }

            Location endLocation = getPlayerTarget(player);
            if (endLocation == null) {
                return;
            }

            if (previousEndLocation == null || previousEndLocation.distance(endLocation) > 0.01) {
                if (line.getLocation().distance(endLocation) > 16) {
                    // Teleport line closer to ensure it doesn't get so far away that it stops being rendered
                    // or despawns
                    line.teleport(endLocation);
                    updateLine(line, startlocation, endLocation, 0, thickness);
                } else {
                    updateLine(line, startlocation, endLocation, tickInterval, thickness);
                }
                previousEndLocation = endLocation;
            }
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

    private static void updateLine(
            @NonNull ItemDisplay line,
            @NonNull Location startLocation,
            @NonNull Location endLocation,
            int tickInterval,
            double thickness
    ) {
        PylonUtils.animate(line, tickInterval, new LineBuilder()
                .from(startLocation.clone().subtract(line.getLocation()).toVector().toVector3d())
                .to(endLocation.clone().subtract(line.getLocation()).toVector().toVector3d())
                .thickness(thickness)
                .extraLength(thickness)
                .build()
                .buildForItemDisplay()
        );
    }

    private void cancelMeasurement() {
        // Remove line
        UUID lineId = getLine();
        if (lineId != null) {
            Entity line = Bukkit.getEntity(lineId);
            if (line != null) {
                line.remove();
            }
        }
        setLine(null);

        // Cancel task
        Integer task = getTask();
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task);
        }
        setTask(null);
    }

    public @Nullable UUID getLine() {
        return getStack().getPersistentDataContainer().get(LINE_KEY, RebarSerializers.UUID);
    }

    public void setLine(@Nullable UUID uuid) {
        getStack().editPersistentDataContainer(pdc ->
                RebarUtils.setNullable(pdc, LINE_KEY, RebarSerializers.UUID, uuid)
        );
    }

    public @Nullable Integer getTask() {
        return getStack().getPersistentDataContainer().get(TASK_KEY, RebarSerializers.INTEGER);
    }

    public void setTask(@Nullable Integer task) {
        getStack().editPersistentDataContainer(pdc ->
                RebarUtils.setNullable(pdc, TASK_KEY, RebarSerializers.INTEGER, task)
        );
    }

    public @Nullable UUID getText() {
        return getStack().getPersistentDataContainer().get(TEXT_KEY, RebarSerializers.UUID);
    }

    public void setText(@Nullable UUID uuid) {
        getStack().editPersistentDataContainer(pdc ->
                RebarUtils.setNullable(pdc, TEXT_KEY, RebarSerializers.UUID, uuid)
        );
    }

    public static class TapeMeasureListener implements Listener {

        @EventHandler
        private static void onPlayerScroll(@NonNull PlayerItemHeldEvent event) {
            ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
            if (fromStack(heldItem) instanceof TapeMeasureOld tapeMeasure) {
                tapeMeasure.cancelMeasurement();
            }
        }

        @EventHandler
        private static void onSwap(@NonNull PlayerSwapHandItemsEvent event) {
            if (fromStack(event.getMainHandItem()) instanceof TapeMeasureOld tapeMeasure) {
                tapeMeasure.cancelMeasurement();
            }

            if (fromStack(event.getOffHandItem()) instanceof TapeMeasureOld tapeMeasure) {
                tapeMeasure.cancelMeasurement();
            }
        }

        @EventHandler
        private static void onInventoryChange(@NonNull PlayerInventorySlotChangeEvent event) {
            // check that the stacks are not identical. need to do == here because we want to cancel even
            // if a new tape measure is inserted
            if (event.getOldItemStack() == event.getNewItemStack()) {
                return;
            }

            if (fromStack(event.getOldItemStack()) instanceof TapeMeasureOld tapeMeasure) {
                tapeMeasure.cancelMeasurement();
            }
        }
    }
}
