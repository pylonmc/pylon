package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;
import static java.lang.Math.PI;

public class Pedestal extends RebarBlock implements
        EntityHolderRebarBlock,
        InteractRebarBlockHandler,
        BlockBreakRebarBlockHandler,
        LogisticRebarBlock {

    private static final NamespacedKey ROTATION_KEY = pylonKey("rotation");
    private static final NamespacedKey LOCKED_KEY = pylonKey("locked");
    private static final NamespacedKey LOCKED_BY_KEY = pylonKey("locked_by");
    private double rotation;
    @Getter
    private boolean locked;
    @Nullable @Getter
    private Location lockedBy;

    @SuppressWarnings("unused")
    public Pedestal(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        addEntity("item", new ItemDisplayBuilder()
                .transformation(transformBuilder().buildForItemDisplay())
                .build(getBlock().getLocation().toCenterLocation())
        );

        rotation = 0;
        locked = false;
        lockedBy = null;
}

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public Pedestal(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        rotation = pdc.get(ROTATION_KEY, RebarSerializers.DOUBLE);
        locked = pdc.get(LOCKED_KEY, RebarSerializers.BOOLEAN);
        lockedBy = pdc.get(LOCKED_BY_KEY, RebarSerializers.LOCATION);
    }

    @Override
    public void postInitialise() {
        // cargo must not touch the item while a recipe is using it (issue #950)
        createLogisticGroup("inventory", LogisticGroupType.BOTH, new ItemDisplayLogisticSlot(getItemDisplay()) {
            @Override
            public boolean canSet(@Nullable ItemStack stack, long amount) {
                return !locked;
            }
        });
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(ROTATION_KEY, RebarSerializers.DOUBLE, rotation);
        pdc.set(LOCKED_KEY, RebarSerializers.BOOLEAN, locked);
        if (lockedBy != null) {
            pdc.set(LOCKED_BY_KEY, RebarSerializers.LOCATION, lockedBy);
        } else {
            pdc.remove(LOCKED_BY_KEY);
        }
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (locked || event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        } else if (event.useItemInHand() != Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        ItemDisplay display = getItemDisplay();

        // rotate
        if (player.isSneaking()) {
            rotation += PI / 4;
            display.setTransformationMatrix(transformBuilder().buildForItemDisplay());
            return;
        }

        // drop old item
        ItemStack oldStack = display.getItemStack();
        ItemStack newStack = event.getItem();

        if (!oldStack.isEmpty()) {
            player.give(oldStack);
            display.setItemStack(null);
            return;
        }

        // insert new item
        if (newStack != null) {
            if (!canPlaceItem(player, newStack)) {
                return;
            }

            display.setItemStack(newStack.asOne());
            newStack.subtract();
        }
    }

    /**
     * @return true if the item can be placed on the pedestal, false otherwise
     */
    public boolean canPlaceItem(Player player, ItemStack stack) {
        return true;
    }

    /**
     * Locks the pedestal for exclusive use by the given altar (e.g. while a recipe is running).
     * The altar is remembered so that breaking the pedestal can interrupt its recipe.
     */
    public void lock(@NotNull Block altar) {
        locked = true;
        lockedBy = altar.getLocation();
    }

    public void unlock() {
        locked = false;
        lockedBy = null;
    }

    /**
     * @return true if this pedestal is locked by an altar other than the given one
     */
    public boolean isLockedByOtherThan(@NotNull Block altar) {
        if (!locked) {
            return false;
        }
        return lockedBy == null || !lockedBy.equals(altar.getLocation());
    }

    private void cancelUsingAltar() {
        if (lockedBy == null) {
            return;
        }
        RebarBlock block = BlockStorage.get(lockedBy.getBlock());
        if (block instanceof Altar altar && altar.isProcessingRecipe()) {
            altar.cancelRecipe();
        }
        lockedBy = null;
    }

    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        ItemStack item = getItemDisplay().getItemStack();
        if (!item.getType().isAir()) {
            drops.add(item);
        }
        if (locked) {
            cancelUsingAltar();
        }
    }

    public ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    public TransformBuilder transformBuilder() {
        return new TransformBuilder()
                .translate(0, 0.7, 0)
                .scale(0.4)
                .rotate(0, rotation, 0);
    }
}
