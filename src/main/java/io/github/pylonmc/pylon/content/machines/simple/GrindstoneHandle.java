package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.util.RandomizedSound;
import org.bukkit.Particle;
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
    
    public final RandomizedSound sound = Settings.get(PylonKeys.GRINDSTONE).getOrThrow("sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound failSound = Settings.get(PylonKeys.GRINDSTONE).getOrThrow("fail-sound", ConfigAdapter.RANDOMIZED_SOUND);

    @SuppressWarnings("unused")
    public GrindstoneHandle(Block block, BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public GrindstoneHandle(Block block, PersistentDataContainer pdc) {
        super(block, pdc);
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

        Block block = getBlock().getRelative(BlockFace.DOWN);
        if (BlockStorage.get(block) instanceof Grindstone grindstone) {
            GrindstoneRecipe nextRecipe = grindstone.getNextRecipe();
            if (nextRecipe != null) {
                if (grindstone.tryStartRecipe(nextRecipe)) {
                    block.getWorld().playSound(sound.create(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
                }
            } else if (!grindstone.getItemDisplay().getItemStack().isEmpty() && !grindstone.isProcessingRecipe()) {
                new ParticleBuilder(Particle.CRIT)
                        .count(10)
                        .location(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
                        .spawn();
                block.getWorld().playSound(failSound.create(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
            }
        }
    }
}
