package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class GrindstoneHandle extends RebarBlock implements RebarInteractBlock {

    @SuppressWarnings("unused")
    public GrindstoneHandle(Block block, BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public GrindstoneHandle(Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == Event.Result.DENY
        ) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if (BlockStorage.get(getBlock().getRelative(BlockFace.DOWN)) instanceof Grindstone grindstone) {
            GrindstoneRecipe nextRecipe = grindstone.getNextRecipe();
            if (nextRecipe != null) {
                grindstone.tryStartRecipe(nextRecipe);
            }
        }
    }
}
