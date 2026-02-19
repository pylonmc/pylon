package io.github.pylonmc.pylon.content.tools;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.item.base.RebarBucket;
import io.github.pylonmc.rebar.item.base.RebarDispensable;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;


public class WateringCan extends RebarItem implements RebarBlockInteractor, RebarBucket, RebarDispensable {

    public final WateringSettings settings = WateringSettings.fromConfig(getSettings());

    private static final Random random = new Random();

    public WateringCan(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("range", UnitFormat.BLOCKS.format(settings.horizontalRange())));
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        water(event.getClickedBlock().getRelative(BlockFace.UP), settings);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onBucketFilled(@NotNull PlayerBucketFillEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    // TODO this will likely need to be profiled and optimised, will not perform well with a lot of sprinklers I think
    public static void water(@NotNull Block center, @NotNull WateringSettings settings) {
        boolean wasAnyTickAttempted = false;
        int horizontalRange = settings.horizontalRange();
        for (int x = -horizontalRange; x <= horizontalRange; x++) {
            for (int z = -horizontalRange; z <= horizontalRange; z++) {
                Block block = center.getRelative(x, 0, z);

                // Search down (for a maximum of RANGE blocks) to find the first solid block
                int remainingYSteps = settings.verticalRange();
                while (block.getType().isAir() && remainingYSteps > 0) {
                    block = block.getRelative(BlockFace.DOWN);
                    remainingYSteps--;
                }

                wasAnyTickAttempted |= tryGrowBlock(block, settings);
            }
        }

        if (wasAnyTickAttempted) {
            playSound(center, settings);
        }
    }

    private static boolean tryGrowBlock(Block block, WateringSettings settings) {
        if (block.getType() == Material.SUGAR_CANE) {
            return growSugarCane(block, settings);
        } else if (block.getType() == Material.CACTUS) {
            return growCactus(block, settings);
        } else if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
            return growCrop(block, ageable, settings);
        } else if (Tag.SAPLINGS.isTagged(block.getType())) {
            return growSapling(block, settings);
        }
        return false;
    }

    private static boolean growSugarCane(@NotNull Block block, WateringSettings settings) {
        int height = 1;

        Block bottomBlock = block.getRelative(BlockFace.DOWN);
        while (bottomBlock.getType() == Material.SUGAR_CANE) {
            height++;
            bottomBlock = bottomBlock.getRelative(BlockFace.DOWN);
        }

        Block topBlock = block.getRelative(BlockFace.UP);
        while (topBlock.getType() == Material.SUGAR_CANE) {
            height++;
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        if (height < 3 && topBlock.getType() == Material.AIR) {
            if (random.nextDouble() < settings.particleChance()) {
                new ParticleBuilder(Particle.HAPPY_VILLAGER)
                        .count(1)
                        .location(topBlock.getLocation().add(0.5, 0.0, 0.5))
                        .offset(0.3, 0.3, 0.3)
                        .spawn();
            }

            if (random.nextDouble() < settings.sugarCaneChance()) {
                topBlock.setType(Material.SUGAR_CANE);
            }

            return true;
        }

        return false;
    }

    private static boolean growCactus(@NotNull Block block, WateringSettings settings) {
        int height = 1;

        Block bottomBlock = block.getRelative(BlockFace.DOWN);
        while (bottomBlock.getType() == Material.CACTUS) {
            height++;
            bottomBlock = bottomBlock.getRelative(BlockFace.DOWN);
        }

        Block topBlock = block.getRelative(BlockFace.UP);
        while (topBlock.getType() == Material.CACTUS) {
            height++;
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        if (height < 3 && topBlock.getType() == Material.AIR) {
            if (random.nextDouble() < settings.particleChance()) {
                new ParticleBuilder(Particle.HAPPY_VILLAGER)
                        .count(1)
                        .location(topBlock.getLocation().add(0.5, 0.0, 0.5))
                        .offset(0.3, 0.3, 0.3)
                        .spawn();
            }

            if (random.nextDouble() < settings.cactusChance()) {
                topBlock.setType(Material.CACTUS);
            }

            return true;
        }

        return false;
    }

    private static boolean growCrop(@NotNull Block block, @NotNull Ageable ageable, WateringSettings settings) {
        if (random.nextDouble() < settings.particleChance()) {
            new ParticleBuilder(Particle.SPLASH)
                    .count(3)
                    .location(block.getLocation().add(0.5, 0.0, 0.5))
                    .offset(0.3, 0, 0.3)
                    .spawn();
        }

        if (random.nextDouble() < settings.cropChance()) {
            ageable.setAge(ageable.getAge() + 1);
            block.setBlockData(ageable);
        }

        return true;
    }

    private static boolean growSapling(@NotNull Block block, WateringSettings settings) {
        if (random.nextDouble() < settings.saplingChance()) {
            block.applyBoneMeal(BlockFace.UP);
        }

        return true;
    }

    private static void playSound(Block block, WateringSettings settings) {
        block.getWorld().playSound(settings.sound().create(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onDispense(@NotNull BlockDispenseEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }
}
