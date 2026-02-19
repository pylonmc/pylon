package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarBreakHandler;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import lombok.Getter;
import lombok.Setter;
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

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;
import static java.lang.Math.PI;

public class Pedestal extends RebarBlock implements
        RebarEntityHolderBlock,
        RebarInteractBlock,
        RebarBreakHandler,
        RebarLogisticBlock {

    private static final NamespacedKey ROTATION_KEY = pylonKey("rotation");
    private static final NamespacedKey LOCKED_KEY = pylonKey("locked");
    private double rotation;
    @Getter @Setter
    private boolean locked;

    @SuppressWarnings("unused")
    public Pedestal(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);

        addEntity("item", new ItemDisplayBuilder()
                .transformation(transformBuilder().buildForItemDisplay())
                .build(getBlock().getLocation().toCenterLocation())
        );

        rotation = 0;
        locked = false;
}

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public Pedestal(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
        
        rotation = pdc.get(ROTATION_KEY, RebarSerializers.DOUBLE);
        locked = pdc.get(LOCKED_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("inventory", LogisticGroupType.BOTH, new ItemDisplayLogisticSlot(getItemDisplay()));
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(ROTATION_KEY, RebarSerializers.DOUBLE, rotation);
        pdc.set(LOCKED_KEY, RebarSerializers.BOOLEAN, locked);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
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
        if (!oldStack.getType().isAir()) {
            player.give(oldStack);
            display.setItemStack(null);
            return;
        }

        // insert new item
        if (newStack != null) {
            ItemStack stackToInsert = newStack.asQuantity(1);
            display.setItemStack(stackToInsert);
            newStack.subtract();
        }
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        drops.add(getItemDisplay().getItemStack());
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
