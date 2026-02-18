package io.github.pylonmc.pylon.content.machines.smelting;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.resources.IronBloom;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class Bloomery extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarInteractBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarBreakHandler {

    public static final int TICK_INTERVAL = Settings.get(PylonKeys.BLOOMERY).getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public static final float HEAT_CHANCE = Settings.get(PylonKeys.BLOOMERY).getOrThrow("heat-chance", ConfigAdapter.FLOAT);

    @SuppressWarnings("unused")
    public Bloomery(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .lookAlong(context.getFacing())
                        .scale(0.3)
                        .translate(0, (1 - .5 + 1d / 16) * 3, 0)
                        .rotate(Math.PI / 2, 0, 0))
                .build(getBlock().getLocation().toCenterLocation())
        );
        setTickInterval(TICK_INTERVAL);
        setMultiblockDirection(context.getFacing());
    }

    @SuppressWarnings("unused")
    public Bloomery(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
       createLogisticGroup("inventory", LogisticGroupType.BOTH, new BloomeryLogisticSlot(getItemDisplay()));
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        ItemStack stack = getItemDisplay().getItemStack();
        if (!stack.isEmpty()) {
            drops.add(stack);
        }
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (player.isSneaking() || !isFormedAndFullyLoaded()) return;

        event.setCancelled(true);
        ItemStack placedItem = event.getItem();

        ItemDisplay itemDisplay = getItemDisplay();
        ItemStack oldStack = itemDisplay.getItemStack();
        if (oldStack.getType().isAir()) {
            if (placedItem != null) {
                if (RebarItem.fromStack(placedItem) instanceof IronBloom bloom) {
                    bloom.setDisplayGlowOn(itemDisplay);
                }
                itemDisplay.setItemStack(placedItem.asOne());
                placedItem.subtract();
            }
        } else {
            for (ItemStack stack : player.getInventory().addItem(oldStack).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }
            itemDisplay.setItemStack(null);
            itemDisplay.setGlowing(false);
        }
    }

    @Override
    public void tick() {
        ItemDisplay itemDisplay = getItemDisplay();
        ItemStack stack = itemDisplay.getItemStack();
        if (stack.getType().isAir()) return;

        if (stack.isSimilar(PylonItems.SPONGE_IRON)) {
            IronBloom bloom = new IronBloom(PylonItems.IRON_BLOOM.clone());
            bloom.setTemperature(0);
            bloom.setWorking(ThreadLocalRandom.current().nextInt(IronBloom.MIN_WORKING, IronBloom.MAX_WORKING + 1));
            itemDisplay.setItemStack(bloom.getStack());
            return;
        }

        if (!(RebarItem.fromStack(stack) instanceof IronBloom bloom)) return;

        Runnable particleSpawner = () -> {
            Location pos = getBlock().getLocation().add(
                    ThreadLocalRandom.current().nextDouble(1),
                    1.2,
                    ThreadLocalRandom.current().nextDouble(1)
            );
            new ParticleBuilder(Particle.SMOKE)
                    .extra(0.01)
                    .count(0)
                    .offset(0, 1, 0)
                    .location(pos)
                    .receivers(32, true)
                    .spawn();
        };
        for (int i = 0; i < 8; i++) {
            Bukkit.getScheduler().runTaskLater(
                    Pylon.getInstance(),
                    particleSpawner,
                    ThreadLocalRandom.current().nextInt(TICK_INTERVAL)
            );
        }

        if (ThreadLocalRandom.current().nextFloat() > HEAT_CHANCE) return;

        int temperature = bloom.getTemperature();
        if (isFormedAndFullyLoaded()) {
            temperature = Math.min(IronBloom.MAX_TEMPERATURE, temperature + 1);
        } else {
            temperature = Math.max(0, temperature - 1);
        }
        bloom.setTemperature(temperature);
        itemDisplay.setItemStack(bloom.getStack());
        bloom.setDisplayGlowOn(itemDisplay);
    }

    public @NotNull ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.REFRACTORY_BRICKS),
                new Vector3i(1, 1, 0), new RebarMultiblockComponent(PylonKeys.REFRACTORY_BRICKS),
                new Vector3i(-1, 1, 0), new RebarMultiblockComponent(PylonKeys.REFRACTORY_BRICKS),
                new Vector3i(0, 1, 1), new RebarMultiblockComponent(PylonKeys.REFRACTORY_BRICKS)
        );
    }

    public static class CreationListener implements Listener {
        @EventHandler
        private void onSetFire(@NotNull BlockPlaceEvent event) {
            Block fire = event.getBlockPlaced();
            if (fire.getType() != Material.FIRE) return;
            Block against = event.getBlockAgainst();
            if (against.getType() != Material.COAL_BLOCK) return;
            List<Item> items = against.getWorld().getNearbyEntities(BoundingBox.of(fire)).stream()
                    .filter(e -> e instanceof Item)
                    .map(e -> (Item) e)
                    .toList();
            if (items.isEmpty()) return;
            Item gypsum = null;
            for (Item item : items) {
                if (item.getItemStack().isSimilar(PylonItems.GYPSUM_DUST)) {
                    gypsum = item;
                    break;
                }
            }
            if (gypsum == null) return;
            gypsum.remove();
            BlockStorage.placeBlock(against, PylonKeys.BLOOMERY);
            fire.setType(Material.AIR);
        }
    }

    static class BloomeryLogisticSlot extends ItemDisplayLogisticSlot {

        public BloomeryLogisticSlot(@NotNull ItemDisplay display) {
            super(display);
        }

        @Override
        public long getMaxAmount(@NotNull ItemStack stack) {
            return 1;
        }
    }
}
