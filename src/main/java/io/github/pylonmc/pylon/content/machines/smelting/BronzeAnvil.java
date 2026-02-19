package io.github.pylonmc.pylon.content.machines.smelting;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.resources.IronBloom;
import io.github.pylonmc.pylon.content.tools.Hammer;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import io.github.pylonmc.rebar.util.RebarUtils;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class BronzeAnvil extends RebarBlock implements
        RebarBreakHandler,
        RebarEntityHolderBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarInteractBlock,
        RebarFallingBlock {

    public static final int TICK_INTERVAL = Settings.get(PylonKeys.BRONZE_ANVIL).getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public static final float COOL_CHANCE = Settings.get(PylonKeys.BRONZE_ANVIL).getOrThrow("cool-chance", ConfigAdapter.FLOAT);
    public static final int TOLERANCE = Settings.get(PylonKeys.BRONZE_ANVIL).getOrThrow("tolerance", ConfigAdapter.INTEGER);
    public static final Sound HAMMER_SOUND = Settings.get(PylonKeys.BRONZE_ANVIL).getOrThrow("sound.hammer", ConfigAdapter.SOUND);
    public static final Sound TONGS_SOUND = Settings.get(PylonKeys.BRONZE_ANVIL).getOrThrow("sound.tongs", ConfigAdapter.SOUND);

    public static final NamespacedKey DIRECTION_FALLING = pylonKey("direction_falling");
    public static final NamespacedKey STORED_ITEM = pylonKey("stored_item");

    private static final Matrix4f BASE_TRANSFORM = new TransformBuilder()
            .scale(0.3)
            .translate(0, (1 - .5 + 1d / 16) * 3, 0)
            .rotate(Math.PI / 2, 0, 0)
            .buildForItemDisplay();

    @SuppressWarnings("unused")
    public BronzeAnvil(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        BlockFace orientation = ((Directional) block.getBlockData()).getFacing();
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new Matrix4f(BASE_TRANSFORM)
                        .rotateLocalY(getItemRotation(getBlockFace())))
                .build(getBlock().getLocation().toCenterLocation())
        );
        setTickInterval(TICK_INTERVAL);
    }

    @SuppressWarnings("unused")
    public BronzeAnvil(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("inventory", LogisticGroupType.BOTH, new BronzeAnvilLogisticSLot(getItemDisplay()));
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        ItemStack stack = getItemDisplay().getItemStack();
        if (!stack.isEmpty()) {
            drops.add(stack);
        }
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Event.Result.DENY) return;

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if (event.getAction().isRightClick()) {
            onRightClick(event);
        } else if (event.getAction().isLeftClick()) {
            onLeftClick(event);
        }
    }

    private void onRightClick(@NotNull PlayerInteractEvent event) {
        ItemStack placedItem = event.getItem();

        ItemDisplay itemDisplay = getItemDisplay();

        if (itemDisplay != null) {
            ItemStack oldStack = itemDisplay.getItemStack();

            if (oldStack.isEmpty()) {
                if (placedItem != null) {
                    itemDisplay.setItemStack(placedItem.asOne());
                    placedItem.subtract();
                    if (RebarItem.fromStack(itemDisplay.getItemStack()) instanceof IronBloom bloom) {
                        transformForWorking(bloom.getWorking(), false);
                        bloom.setDisplayGlowOn(itemDisplay);
                    }
                }
            } else {
                event.getPlayer().give(oldStack);
                itemDisplay.setItemStack(null);

                ItemDisplay display = getItemDisplay();
                if (display != null) {
                    Matrix4f transform = new Matrix4f(BASE_TRANSFORM)
                        .rotateLocalY(getItemRotation(getBlockFace()))
                        .scaleLocal(1, 1, 1);
                    display.setTransformationMatrix(transform);
                }
            }
        }

        event.getPlayer().swingHand(EquipmentSlot.HAND);
    }

    private void onLeftClick(@NotNull PlayerInteractEvent event) {
        ItemDisplay itemDisplay = getItemDisplay();
        if (!(RebarItem.fromStack(itemDisplay.getItemStack()) instanceof IronBloom bloom)) return;

        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) return;

        Player player = event.getPlayer();

        int temperature = bloom.getTemperature();
        int workingChange = ThreadLocalRandom.current().nextInt(-1, 2);
        if (temperature == 0) {
            player.swingHand(EquipmentSlot.HAND);
            return;
        } else if (item.isSimilar(PylonItems.TONGS)) {
            workingChange -= temperature;
            getBlock().getWorld().playSound(TONGS_SOUND, player);
        } else if (RebarItem.fromStack(item) instanceof Hammer hammer) {
            if (!player.hasCooldown(item)) {
                workingChange += temperature;
                player.setCooldown(item, hammer.cooldownTicks);
                getBlock().getWorld().playSound(HAMMER_SOUND, player);
            }
        } else {
            return;
        }

        int working = bloom.getWorking();
        int newWorking = working + workingChange;
        Location centerLoc = getBlock().getRelative(BlockFace.UP).getLocation().toCenterLocation();
        if (Math.abs(newWorking) > IronBloom.MAX_WORKING) {
            bloom.setWorking(
                Math.max(
                    - IronBloom.MAX_WORKING,
                    Math.min(
                        newWorking,
                        IronBloom.MAX_WORKING
                    )
                )
            );
            return;
        } else {
            new ParticleBuilder(Particle.LAVA).location(centerLoc)
                    .receivers(32, true)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.03)
                    .count(temperature)
                    .spawn();
        }
        bloom.setWorking(newWorking);
        itemDisplay.setItemStack(bloom.getStack());
        transformForWorking(newWorking, item.isSimilar(PylonItems.TONGS));
    }

    @Override
    public void tick() {
        if (ThreadLocalRandom.current().nextFloat() > COOL_CHANCE) return;
        ItemDisplay itemDisplay = getItemDisplay();
        if (itemDisplay == null) return;
        if (!(RebarItem.fromStack(itemDisplay.getItemStack()) instanceof IronBloom bloom)) return;
        int newTemperature = Math.max(0, bloom.getTemperature() - 1);
        bloom.setTemperature(newTemperature);
        bloom.setDisplayGlowOn(itemDisplay);
        if (bloom.getWorking() >= -TOLERANCE && bloom.getWorking() <= TOLERANCE && newTemperature == 0) {
            itemDisplay.setItemStack(null);
            Location centerLoc = getBlock().getLocation().toCenterLocation();
            centerLoc.getWorld().dropItemNaturally(centerLoc, PylonItems.WROUGHT_IRON.clone());
            return;
        }
        itemDisplay.setItemStack(bloom.getStack());
    }


    @Override
    public void onFallStart(@NotNull EntityChangeBlockEvent event, @NotNull RebarFallingBlock.RebarFallingBlockEntity spawnedEntity) {
        var pdc = spawnedEntity.getEntity().getPersistentDataContainer();

        ItemDisplay display = getItemDisplay();
        RebarUtils.setNullable(pdc, STORED_ITEM, RebarSerializers.ITEM_STACK, display == null ? null : display.getItemStack());
        pdc.set(DIRECTION_FALLING, RebarSerializers.BLOCK_FACE, getBlockFace());
    }

    @Override
    public void onFallStop(@NotNull EntityChangeBlockEvent event, @NotNull RebarFallingBlock.RebarFallingBlockEntity entity) {
        var pdc = entity.getEntity().getPersistentDataContainer();

        ItemStack stack = pdc.get(STORED_ITEM, RebarSerializers.ITEM_STACK);
        BlockFace face = pdc.get(DIRECTION_FALLING, RebarSerializers.BLOCK_FACE);
        addEntity("item", new ItemDisplayBuilder()
                .itemStack(stack)
                .transformation(new Matrix4f(BASE_TRANSFORM)
                        .rotateLocalY(getItemRotation(face)))
                .build(getBlock().getLocation().toCenterLocation())
        );
    }

    public ItemDisplay getItemDisplay() {
        return getHeldEntity(ItemDisplay.class, "item");
    }

    private void transformForWorking(int working, boolean interpolate) {
        Matrix4f transform = new Matrix4f(BASE_TRANSFORM)
                .rotateLocalY(getItemRotation(getBlockFace()))
                .scaleLocal(
                        Math.max(0, working * 0.5f) + 1,
                        1,
                        Math.max(0, -working * 0.5f) + 1
                );

        ItemDisplay display = getItemDisplay();
        if (display == null) return;

        if (interpolate) {
            PylonUtils.animate(display, 5, transform);
        } else {
            display.setTransformationMatrix(transform);
        }
    }

    private BlockFace getBlockFace() {
        return ((Directional) getBlock().getBlockData()).getFacing();
    }

    private static float getItemRotation(BlockFace face) {
        return (float) switch (face) {
            case NORTH -> 3 * Math.PI / 2;
            case EAST -> Math.PI;
            case SOUTH -> Math.PI / 2;
            case WEST -> 0;
            default -> 0;
        };
    }

    static class BronzeAnvilLogisticSLot extends ItemDisplayLogisticSlot {

        public BronzeAnvilLogisticSLot(@NotNull ItemDisplay display) {
            super(display);
        }

        @Override
        public long getMaxAmount(@NotNull ItemStack stack) {
            return 1;
        }
    }
}
