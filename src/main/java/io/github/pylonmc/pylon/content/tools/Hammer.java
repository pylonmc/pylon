package io.github.pylonmc.pylon.content.tools;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.smelting.BronzeAnvil;
import io.github.pylonmc.pylon.recipes.HammerRecipe;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.util.MiningLevel;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.github.pylonmc.rebar.util.RebarUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public class Hammer extends RebarItem implements RebarBlockInteractor {
    public final Material baseBlock = getBaseBlock(getKey());
    public final MiningLevel miningLevel = getMiningLevel(getKey());
    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final RandomizedSound sound = getSettings().getOrThrow("sound", ConfigAdapter.RANDOMIZED_SOUND);

    public Hammer(@NotNull ItemStack stack) {
        super(stack);
    }

    public boolean tryDoRecipe(@NotNull Block block, @Nullable Player player, @Nullable EquipmentSlot slot, @NotNull BlockFace clickedFace) {
        if (baseBlock != block.getType()) {
            if (player != null && !(BlockStorage.get(block) instanceof BronzeAnvil)) {
                player.sendMessage(Component.translatable("pylon.message.hammer_cant_use"));
            }
            return false;
        }

        if (clickedFace != BlockFace.UP) return false;

        Block blockAbove = block.getRelative(BlockFace.UP);

        List<Item> items = new ArrayList<>();
        for (Entity e : block.getWorld().getNearbyEntities(BoundingBox.of(blockAbove))) {
            if (e instanceof Item entity) {
                items.add(entity);
            }
        }

        for (HammerRecipe recipe : HammerRecipe.RECIPE_TYPE) {
            if (!miningLevel.isAtLeast(recipe.level())) {
                continue;
            }

            for (Item item : items) {
                if (!recipe.input().matches(item.getItemStack())) {
                    continue;
                }

                if (player != null) {
                    player.setCooldown(getStack(), cooldownTicks);
                    RebarUtils.damageItem(getStack(), 1, player, slot);
                } else {
                    RebarUtils.damageItem(getStack(), 1, block.getWorld());
                }

                if (ThreadLocalRandom.current().nextFloat() > recipe.getChanceFor(miningLevel)) {
                    return true; // recipe attempted but unsuccessful
                }

                int newAmount = item.getItemStack().getAmount() - recipe.input().getAmount();
                item.setItemStack(item.getItemStack().asQuantity(newAmount));
                block.getWorld().dropItem(blockAbove.getLocation().add(0.5, 0.1, 0.5), recipe.result())
                        .setVelocity(new Vector(0, 0, 0));
                block.getWorld().playSound(sound.create(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);

                return true;
            }
        }

        return false;
    }

    @Override
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event) {
        event.setUseInteractedBlock(Event.Result.DENY);

        Player player = event.getPlayer();
        if (player.hasCooldown(getStack())) return;

        Block clickedBlock = event.getClickedBlock();
        Preconditions.checkState(clickedBlock != null);

        tryDoRecipe(clickedBlock, player, event.getHand(), event.getBlockFace());
    }

    private static Material getBaseBlock(@NotNull NamespacedKey key) {
        return Map.of(
                PylonKeys.STONE_HAMMER, Material.STONE,
                PylonKeys.IRON_HAMMER, Material.IRON_BLOCK,
                PylonKeys.DIAMOND_HAMMER, Material.DIAMOND_BLOCK
        ).get(key);
    }

    private static MiningLevel getMiningLevel(@NotNull NamespacedKey key) {
        return Map.of(
                PylonKeys.STONE_HAMMER, MiningLevel.STONE,
                PylonKeys.IRON_HAMMER, MiningLevel.IRON,
                PylonKeys.DIAMOND_HAMMER, MiningLevel.DIAMOND
        ).get(key);
    }
}
