package io.github.pylonmc.pylon.content.tools;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.RebarBlockBreakEvent;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;


import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @author balugaq
 */
@SuppressWarnings("UnstableApiUsage")
public class FireproofRune extends Rune {
    public static final Tag<DamageType> IS_FIRE_TAG = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getTag(DamageTypeTagKeys.IS_FIRE);

    public static final Component SUCCESS = Component.translatable("pylon.message.fireproof_result.success");
    public static final Component TOOLTIP = Component.translatable("pylon.message.fireproof_result.tooltip");

    public static final NamespacedKey FIREPROOF_KEY = pylonKey("have_fireproof");

    private final RandomizedSound applySound = getSettingOrThrow("apply-sound", ConfigAdapter.RANDOMIZED_SOUND);

    public FireproofRune(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Fixes #156 - Fireproof rune can be applied multiple times
     * <p>
     * Checks if the rune is applicable to the target item.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     * @return true if applicable, false otherwise
     */
    @Override
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        if (hasRuneApplied(target)) return false;
        DamageResistant data = target.getData(DataComponentTypes.DAMAGE_RESISTANT);
        if (data == null) return true;
        return !data.types().equals(IS_FIRE_TAG);
    }

    /**
     * Handles contacting between an item and a rune.
     *
     * @param event  The event
     * @param rune   The rune item, amount may be > 1
     * @param target The item to handle, amount may be > 1
     */
    @Override
    public void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        // As many runes as possible to consume
        int consume = Math.min(rune.getAmount(), target.getAmount());

        Player player = event.getPlayer();
        ItemStack handle = applyRune(target, consume);

        // (N)Either left runes or targets
        int leftRunes = rune.getAmount() - consume;
        int leftTargets = target.getAmount() - consume;

        Location explodeLoc = event.getItemDrop().getLocation();
        World world = explodeLoc.getWorld();
        if (leftRunes > 0) {
            world.dropItemNaturally(explodeLoc, rune.asQuantity(leftRunes)).setGlowing(true);
        }
        if (leftTargets > 0) {
            world.dropItemNaturally(explodeLoc, target.asQuantity(leftTargets)).setGlowing(true);
        }
        world.dropItemNaturally(explodeLoc, handle).setGlowing(true);

        // simple particles
        spawnParticle(Particle.EXPLOSION, explodeLoc, 1);
        spawnParticle(Particle.FLAME, explodeLoc, 50);
        spawnParticle(Particle.SMOKE, explodeLoc, 40);
        world.playSound(applySound.create(), explodeLoc.x(), explodeLoc.y(), explodeLoc.z());

        target.setAmount(0);
        rune.setAmount(0);
        player.sendMessage(SUCCESS);
    }

    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {
        new ParticleBuilder(particle)
                .location(location)
                .offset(0, 0, 0)
                .count(count)
                .spawn();
    }

    public static ItemStack applyRune(@NotNull ItemStack itemStack, int amount) {
        return ItemStackBuilder.of(itemStack.asQuantity(amount))
                .set(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(IS_FIRE_TAG))
                .editPdc(pdc -> pdc.set(FIREPROOF_KEY, RebarSerializers.BOOLEAN, true))
                .lore(TOOLTIP)
                .build();
    }

    /**
     * Checks if the target already has the fireproof rune applied
     *
     * @return true if the fireproof rune has been used on the item, false otherwise
     */
    public static boolean hasRuneApplied(@NotNull ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().getOrDefault(FIREPROOF_KEY, RebarSerializers.BOOLEAN, false);
    }

    public static class FireproofRuneListener implements Listener {
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
                ItemStack fireproofStack = applyRune(itemStack, 1);
                itemStack.subtract();
                event.getDrops().add(fireproofStack);
                break;
            }
            block.getBlock().getChunk().getPersistentDataContainer().remove(getChunkPDC(block.getBlock().getLocation()));
        }

        public NamespacedKey getChunkPDC(Location location) {
            return pylonKey(String.format("%s_%d_%d_%d",
                "fireproof", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
    }
}
