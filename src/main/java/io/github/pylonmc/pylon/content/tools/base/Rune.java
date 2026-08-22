package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonConfig;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.RebarBlockBreakEvent;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.item.interfaces.ArrowRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BlockBreakRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BowRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BucketRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.EntityAttackRebarItemHandler;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemContainerContents;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author balugaq
 */
public abstract class Rune extends RebarItem {
    // These can be applied with runes
    public static final List<Class<?>> DEFAULT_APPLICABLES = List.of(
            ArrowRebarItemHandler.class,
            BowRebarItemHandler.class,
            BucketRebarItemHandler.class,
            BlockBreakRebarItemHandler.class,
            EntityAttackRebarItemHandler.class
    );

    public Rune(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Checks if the rune is applicable to the target item.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     * @return true if applicable, false otherwise
     */
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        RebarItemSchema schema = RebarItemSchema.fromStack(target);
        if (schema == null) {
            // Non-Rebar items are always applicable
            return true;
        }

        RuneApplicable checker = RebarItem.fromStack(target, RuneApplicable.class);
        if (checker != null && checker.applicableToTarget(event, rune)) {
            return true;
        }

        return DEFAULT_APPLICABLES.stream().anyMatch(clazz -> clazz.isAssignableFrom(schema.getItemClass()));
    }

    /**
     * Handles contacting between an item and a rune.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     */
    public abstract void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target);

    public static class RuneListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        void onRuneDrop(@NotNull PlayerDropItemEvent event) {
            Player player = event.getPlayer();
            Item runeEntity = event.getItemDrop();
            ItemStack runeStack = runeEntity.getItemStack();
            Rune rune = RebarItem.fromStack(runeStack, Rune.class);
            if (rune == null) {
                return;
            }

            // Fix #155 - Fireproof rune only checks proximity at the moment it's dropped
            // Force run synchronously for entity handling
            Bukkit.getScheduler().runTaskTimer(Pylon.getInstance(), task -> {
                if (runeEntity.isDead() || !runeEntity.isValid()) {
                    task.cancel();
                    return;
                }

                if (!runeEntity.isOnGround()) {
                    return;
                }

                Collection<Item> nearbyEntities = player.getWorld().getNearbyEntitiesByType(Item.class, runeEntity.getLocation(), PylonConfig.RUNE_CHECK_RANGE, item -> rune.isApplicableToTarget(event, runeStack, item.getItemStack()));
                Item targetEntity = nearbyEntities
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (targetEntity == null) {
                    // No target, skip it.
                    return;
                }

                ItemStack target = targetEntity.getItemStack();

                // All actions are handled by devs
                rune.onContactItem(event, runeStack, target);
                runeEntity.setItemStack(runeStack);
                targetEntity.setItemStack(target);
            }, 1, 2);
        }
    }

    public static abstract class BlockRuneListener implements Listener { //abstract listener
        private static final String RUNE_PREFIX = "rune"; //for vanilla block

        /**
         * get the key prefix used to identify the location of rune block
         * 
         * @return key prefix
         */
        protected abstract String getKeyPrefix();

        /**
         * check if itemStack has rune effect
         * 
         * @param stack The itemStack
         * @return true if has effect, false otherwise
         */
        protected abstract boolean hasRuneApplied(@NotNull ItemStack stack);

        /**
         * return the itemStack that has rune effect
         * 
         * @param stack The itemStack be applied
         * @param amount The amount of returned itemStack
         * @return the itemStack
         */
        protected abstract ItemStack applyRune(@NotNull ItemStack stack, int amount);

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) { // use chunk pdc
            ItemStack itemStack = event.getItemInHand();
            if (itemStack == null || itemStack.isEmpty()) {
                return;
            }

            Block block = event.getBlock();
            if (hasRuneApplied(itemStack)) {
                block.getChunk().getPersistentDataContainer().set(
                    getChunkPDC(RebarItem.isRebarItem(itemStack) ? getKeyPrefix() : RUNE_PREFIX, block.getLocation()),
                    RebarSerializers.ITEM_STACK,
                    itemStack.asOne()
                );
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockDropItem(@NotNull BlockDropItemEvent event) { //for vanilla block
            Block block = event.getBlock();
            NamespacedKey pdc = getChunkPDC(RUNE_PREFIX, block.getLocation());
            
            if (!block.getChunk().getPersistentDataContainer().has(pdc)) {
                return;
            }

            ItemStack pdcItemStack = block.getChunk().getPersistentDataContainer()
                    .get(pdc, RebarSerializers.ITEM_STACK);

            if (pdcItemStack.hasData(DataComponentTypes.CONTAINER)) { //prevent the appearance of dupe
                pdcItemStack.setData(DataComponentTypes.CONTAINER, ItemContainerContents.containerContents(new ArrayList<>()));
            }

            for (Item item : event.getItems()) {
                ItemStack itemStack = item.getItemStack();
                if (pdcItemStack.getType() != itemStack.getType()) {
                    continue;
                }

                if (itemStack.hasData(DataComponentTypes.CONTAINER)) { //for vanilla shulkerbox
                    pdcItemStack.setData(DataComponentTypes.CONTAINER, itemStack.getData(DataComponentTypes.CONTAINER));
                }

                itemStack.subtract();
                block.getWorld().dropItemNaturally(block.getLocation(), pdcItemStack);

                break;
            }

            block.getChunk().getPersistentDataContainer().remove(pdc);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onRebarBlockBreak(@NotNull RebarBlockBreakEvent event) { // for rebar block
            RebarBlock block = event.getRebarBlock();
            NamespacedKey pdc = getChunkPDC(getKeyPrefix(), block.getBlock().getLocation());
            if (!block.getBlock().getChunk().getPersistentDataContainer().has(pdc)) {
                return;
            }
            for (ItemStack itemStack : event.getDrops()) {
                RebarItemSchema dropSchema = RebarItemSchema.fromStack(itemStack);
                if (dropSchema == null || !block.getKey().equals(dropSchema.getRebarBlockKey())) {
                    continue;
                }

                ItemStack runeStack = applyRune(itemStack, 1);

                itemStack.subtract();
                event.getDrops().add(runeStack);

                break;
            }
            block.getBlock().getChunk().getPersistentDataContainer().remove(pdc);
        }

        public NamespacedKey getChunkPDC(String prefix, Location location) { //record if block has rune effect
            return PylonUtils.pylonKey(String.format("%s_%d_%d_%d",
                prefix, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
    }
}
