package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.RebarBlockBreakEvent;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class SoulboundRune extends Rune {
    private static final TranslatableComponent SOULBIND_MSG = Component.translatable("pylon.message.soulbound_rune.soulbind-message");
    private static final TranslatableComponent TOOLTIP = Component.translatable("pylon.message.soulbound_rune.tooltip");
    private static final NamespacedKey SOULBOUND_KEY = pylonKey("soulbound");

    public SoulboundRune(ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        return !RebarItem.isRebarItem(target, SoulboundRune.class) && !hasRuneApplied(target);
    }

    @Override
    public void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        int consume = Math.min(rune.getAmount(), target.getAmount());

        ItemStack soulboundItem = applyRune(target, consume);

        // (N)Either left runes or targets
        int leftRunes = rune.getAmount() - consume;
        int leftTargets = target.getAmount() - consume;

        Location dropLocation = event.getItemDrop().getLocation();
        World world = dropLocation.getWorld();
        if (leftRunes > 0) {
            world.dropItemNaturally(dropLocation, rune.asQuantity(leftRunes)).setGlowing(true);
        }
        if (leftTargets > 0) {
            world.dropItemNaturally(dropLocation, target.asQuantity(leftTargets)).setGlowing(true);
        }
        world.dropItemNaturally(dropLocation, soulboundItem).setGlowing(true);

        target.setAmount(0);
        rune.setAmount(0);
        event.getPlayer().sendMessage(SOULBIND_MSG);
    }

    public static ItemStack applyRune(@NotNull ItemStack itemStack, int amount) {
        return ItemStackBuilder.of(itemStack.asQuantity(amount))
                .editPdc(pdc -> pdc.set(SOULBOUND_KEY, RebarSerializers.BOOLEAN, true))
                .lore(TOOLTIP)
                .build();
    }

    /**
     * Checks if the target already has the soulbound rune applied
     *
     * @return true if the soulbound rune has been used on the item, false otherwise
     */
    public static boolean hasRuneApplied(@NotNull ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().getOrDefault(SOULBOUND_KEY, RebarSerializers.BOOLEAN, false);
    }

    public static class SoulboundRuneListener implements Listener {

        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) { // exception being generated
            Iterator<ItemStack> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                ItemStack drop = drops.next();
                if (drop != null && hasRuneApplied(drop)) {
                    event.getItemsToKeep().add(drop);
                    drops.remove();
                }
            }
        }
        
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockPlace(BlockPlaceEvent event) { // use chunk pdc
            ItemStack itemStack = event.getItemInHand();
            if (itemStack == null || itemStack.isEmpty()) {
                return;
            }

            Block block = event.getBlock();
            if (hasRuneApplied(itemStack)) {
                block.getChunk().getPersistentDataContainer()
                    .set(getChunkPDC(block.getLocation()), RebarSerializers.ITEM_STACK, itemStack.asOne());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockBreak(@NotNull BlockBreakEvent event) { //for vanilla shulkerbox
            Block block = event.getBlock();

            if (BlockStorage.isRebarBlock(block) || !block.getChunk().getPersistentDataContainer().has(getChunkPDC(block.getLocation()))) {
                return;
            }
            ItemStack pdcItemStack = block.getChunk().getPersistentDataContainer()
                    .get(getChunkPDC(block.getLocation()), RebarSerializers.ITEM_STACK);

            if (block.getState() instanceof ShulkerBox box) {
                event.setDropItems(false);
                BlockStateMeta meta = (BlockStateMeta) pdcItemStack.getItemMeta();

                meta.setBlockState(box);
                pdcItemStack.setItemMeta(meta);

                block.getWorld().dropItemNaturally(block.getLocation(), pdcItemStack);

                block.getChunk().getPersistentDataContainer().remove(getChunkPDC(block.getLocation()));
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockDropItem(@NotNull BlockDropItemEvent event) { // for vanilla block
            Block block = event.getBlock();

            if (!block.getChunk().getPersistentDataContainer().has(getChunkPDC(block.getLocation()))) {
                return;
            }

            ItemStack pdcItemStack = block.getChunk().getPersistentDataContainer()
                    .get(getChunkPDC(block.getLocation()), RebarSerializers.ITEM_STACK);
            for (Item item : event.getItems()) {
                ItemStack itemStack = item.getItemStack();
                if (pdcItemStack.getType() != itemStack.getType()) {
                    continue;
                }
                event.getItems().remove(item);
                block.getWorld().dropItemNaturally(block.getLocation(), pdcItemStack);
                break;
            }

            block.getChunk().getPersistentDataContainer().remove(getChunkPDC(block.getLocation()));
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onRebarBlockBreak(@NotNull RebarBlockBreakEvent event) { // for rebar block
            RebarBlock block = event.getRebarBlock();
            if (!block.getBlock().getChunk().getPersistentDataContainer().has(getChunkPDC(block.getBlock().getLocation()))) {
                return;
            }

            for (ItemStack itemStack : event.getDrops()) {
                RebarItemSchema dropSchema = RebarItemSchema.fromStack(itemStack);
                if (dropSchema == null || !block.getKey().equals(dropSchema.getRebarBlockKey())) {
                    continue;
                }

                ItemStack soulboundStack = applyRune(itemStack, 1);
                itemStack.subtract();
                event.getDrops().add(soulboundStack);
                break;
            }
            block.getBlock().getChunk().getPersistentDataContainer().remove(getChunkPDC(block.getBlock().getLocation()));
        }

        public NamespacedKey getChunkPDC(Location location) {
            return pylonKey(String.format("%s_%d_%d_%d",
                "soulbound", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
    }
}
