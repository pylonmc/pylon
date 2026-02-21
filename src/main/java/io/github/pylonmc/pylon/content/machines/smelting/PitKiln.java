package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.PitKilnRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.*;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class PitKiln extends RebarBlock implements
        RebarSimpleMultiblock, RebarInteractBlock, RebarTickingBlock, RebarBreakHandler, RebarVanillaContainerBlock {

    public static final int CAPACITY = Settings.get(PylonKeys.PIT_KILN).getOrThrow("capacity", ConfigAdapter.INTEGER);
    public static final int PROCESSING_TIME_SECONDS =
            Settings.get(PylonKeys.PIT_KILN).getOrThrow("processing-time-seconds", ConfigAdapter.INTEGER);

    private static final double MULTIPLIER_CAMPFIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.campfire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_SOUL_CAMPFIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.soul-campfire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_FIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.fire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_SOUL_FIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.soul-fire", ConfigAdapter.DOUBLE);

    private static final double MULTIPLIER_DIRT = Settings.get(PylonKeys.PIT_KILN).getOrThrow("item-multipliers.coarse-dirt", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_PODZOL = Settings.get(PylonKeys.PIT_KILN).getOrThrow("item-multipliers.podzol", ConfigAdapter.DOUBLE);

    public static final class Item extends RebarItem {

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("capacity", CAPACITY),
                    RebarArgument.of("smelting_time", UnitFormat.formatDuration(Duration.ofSeconds(PROCESSING_TIME_SECONDS), false)),
                    RebarArgument.of("campfire", MULTIPLIER_CAMPFIRE),
                    RebarArgument.of("soul_campfire", MULTIPLIER_SOUL_CAMPFIRE),
                    RebarArgument.of("fire", MULTIPLIER_FIRE),
                    RebarArgument.of("soul_fire", MULTIPLIER_SOUL_FIRE),
                    RebarArgument.of("coarse_dirt", MULTIPLIER_DIRT),
                    RebarArgument.of("podzol", MULTIPLIER_PODZOL)
            );
        }
    }

    private static final NamespacedKey CONTENTS_KEY = pylonKey("contents");
    private static final PersistentDataType<?, Set<ItemStack>> CONTENTS_TYPE =
            RebarSerializers.SET.setTypeFrom(RebarSerializers.ITEM_STACK);
    private static final NamespacedKey PROCESSING_KEY = pylonKey("processing");
    private static final NamespacedKey PROCESSING_TIME_KEY = pylonKey("processing_time");

    private final Set<ItemStack> contents;
    private final Set<ItemStack> processing;
    private @Nullable Double processingTime;

    @SuppressWarnings("unused")
    public PitKiln(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        contents = new HashSet<>();
        processing = new HashSet<>();
        processingTime = null;
    }

    @SuppressWarnings("unused")
    public PitKiln(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        contents = pdc.get(CONTENTS_KEY, CONTENTS_TYPE);
        processing = pdc.get(PROCESSING_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.ITEM_STACK));
        processingTime = pdc.get(PROCESSING_TIME_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(CONTENTS_KEY, CONTENTS_TYPE, contents);
        pdc.set(PROCESSING_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.ITEM_STACK), processing);
        RebarUtils.setNullable(pdc, PROCESSING_TIME_KEY, RebarSerializers.DOUBLE, processingTime);
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        drops.addAll(contents);
    }

    @Override
    public void postBreak(@NotNull BlockBreakContext context) {
        removeWailas();
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Event.Result.DENY || !event.getHand().isHand()) return;

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) {
            return;
        }

        event.getPlayer().swingHand(event.getHand());
        addItem(item, true);
    }

    public int countItems() {
        int amount = 0;
        for (ItemStack item : contents) {
            amount += item.getAmount();
        }
        return amount;
    }

    /**
     * Will add 1 of the type specified in the itemstack
     *
     * @param item specified itemstack type
     * @param directRemoval if item stack passed will be decreased
     */
    public void addItem(ItemStack item, boolean directRemoval) {
        if (countItems() >= CAPACITY) return;

        for (ItemStack contentItem : contents) {
            if (contentItem.isSimilar(item)) {
                contentItem.add();
                if (directRemoval) item.subtract();
                return;
            }
        }

        contents.add(item.asOne());

        if (directRemoval) item.subtract();
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            if (processingTime != null) {
                processingTime = null;
                processing.clear();
            }
            return;
        }
        if (processingTime == null) {
            tryStartProcessing();
        }

        if (processingTime == null) return;
        processingTime -= getTickInterval() / 20.0;
        if (processingTime > 0) return;

        processingTime = null;
        double calcMultiplier = 0;
        for (Vector3i top : TOP_POSITIONS) {
            Block topBlock = getBlock().getRelative(top.x(), top.y(), top.z());
            calcMultiplier += switch (topBlock.getType()) {
                case PODZOL -> MULTIPLIER_PODZOL;
                case COARSE_DIRT -> MULTIPLIER_DIRT;
                default -> throw new AssertionError();
            };
            topBlock.setType(Material.COARSE_DIRT);
        }

        double multiplier = calcMultiplier / TOP_POSITIONS.size();

        outputLoop:
        for (ItemStack outputItem : processing) {
            int addAmount = (int) Math.floor(outputItem.getAmount() * multiplier);
            for (ItemStack contentItem : contents) {
                if (contentItem.isSimilar(outputItem)) {
                    contentItem.add(addAmount);
                    continue outputLoop;
                }
            }
            contents.add(outputItem.asQuantity(addAmount));
        }
        processing.clear();
        for (Vector3i coal : COAL_POSITIONS) {
            Block coalBlock = getBlock().getRelative(coal.x(), coal.y(), coal.z());
            coalBlock.setType(Material.AIR);
        }
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        return getComponentWaila(player);
    }

    private WailaDisplay getComponentWaila(@NotNull Player player) {
        Component status = processingTime != null
                ? Component.translatable(
                        "pylon.waila.pit_kiln.smelting",
                        RebarArgument.of(
                                "time",
                                UnitFormat.formatDuration(Duration.ofSeconds(processingTime.longValue()), false)
                        )
                )
                : Component.translatable("pylon.waila.pit_kiln.invalid_recipe");
        return new WailaDisplay(Component.translatable(
                "pylon.item.pit_kiln.waila",
                RebarArgument.of("info", status)
        ));
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        for (Vector3i relative : getComponents().keySet()) {
            BlockPosition block = new BlockPosition(getBlock()).addScalar(relative.x(), relative.y(), relative.z());
            Waila.addWailaOverride(block, this::getComponentWaila);
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);
        removeWailas();
    }

    private void removeWailas() {
        for (Vector3i relative : getComponents().keySet()) {
            BlockPosition block = new BlockPosition(getBlock()).addScalar(relative.x(), relative.y(), relative.z());
            Waila.removeWailaOverride(block);
        }
    }

    private void tryStartProcessing() {
        if (processingTime != null || contents.isEmpty()) return;
        recipeLoop:
        for (PitKilnRecipe recipe : PitKilnRecipe.RECIPE_TYPE) {
            int ratio = Integer.MAX_VALUE;
            for (RecipeInput.Item input : recipe.input()) {
                int existing = 0;
                for (ItemStack contentItem : contents) {
                    if (input.contains(contentItem)) {
                        existing = contentItem.getAmount();
                        break;
                    }
                }
                int required = input.getAmount();
                if (existing < required) {
                    continue recipeLoop;
                }
                ratio = Math.min(ratio, existing / required);
            }
            if (ratio <= 0) continue;

            for (RecipeInput.Item input : recipe.input()) {
                int removeAmount = input.getAmount() * ratio;
                for (ItemStack contentItem : contents) {
                    if (input.contains(contentItem)) {
                        contentItem.subtract(removeAmount);
                        break;
                    }
                }
            }
            Set<ItemStack> outputItems = new HashSet<>(recipe.output().size());
            for (ItemStack outputItem : recipe.output()) {
                ItemStack outputCopy = outputItem.asOne();
                outputCopy.setAmount(outputItem.getAmount() * ratio);
                outputItems.add(outputCopy);
            }
            processing.addAll(outputItems);
            double multiplier = switch (getBlock().getRelative(FIRE_POSITION.x(), FIRE_POSITION.y(), FIRE_POSITION.z()).getType()) {
                case CAMPFIRE -> MULTIPLIER_CAMPFIRE;
                case SOUL_CAMPFIRE -> MULTIPLIER_SOUL_CAMPFIRE;
                case FIRE -> MULTIPLIER_FIRE;
                case SOUL_FIRE -> MULTIPLIER_SOUL_FIRE;
                default -> throw new AssertionError();
            };
            processingTime = PROCESSING_TIME_SECONDS / multiplier;
            break;
        }
    }

    @Override @MultiHandler(priorities = { EventPriority.LOWEST, EventPriority.MONITOR }, ignoreCancelled = true)
    public void onItemMoveTo(@NotNull InventoryMoveItemEvent event, @NotNull EventPriority priority) {
        if (priority == EventPriority.LOWEST) {
            if (countItems() >= CAPACITY) {
                event.setCancelled(true);
            }
            return;
        }

        ItemStack stack = event.getItem().clone();
        event.setItem(ItemStack.empty());
        this.addItem(stack, false);
    }

    // <editor-fold desc="Multiblock" defaultstate="collapsed">
    private static final List<Vector3i> COAL_POSITIONS = List.of(
            new Vector3i(-1, 0, -1),
            new Vector3i(0, 0, -1),
            new Vector3i(1, 0, -1),
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 0),
            new Vector3i(-1, 0, 1),
            new Vector3i(0, 0, 1),
            new Vector3i(1, 0, 1)
    );

    private static final List<Vector3i> TOP_POSITIONS = List.of(
            new Vector3i(-1, 1, -1),
            new Vector3i(0, 1, -1),
            new Vector3i(1, 1, -1),
            new Vector3i(-1, 1, 0),
            new Vector3i(0, 1, 0),
            new Vector3i(1, 1, 0),
            new Vector3i(-1, 1, 1),
            new Vector3i(0, 1, 1),
            new Vector3i(1, 1, 1)
    );

    private static final Vector3i FIRE_POSITION = new Vector3i(0, -1, 0);

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        for (Vector3i coalPosition : COAL_POSITIONS) {
            components.put(coalPosition, new MixedMultiblockComponent(
                    new VanillaMultiblockComponent(Material.COAL_BLOCK),
                    new RebarMultiblockComponent(PylonKeys.CHARCOAL_BLOCK)
                )
            );
        }
        for (Vector3i podzolPosition : TOP_POSITIONS) {
            components.put(podzolPosition, new VanillaMultiblockComponent(Material.COARSE_DIRT, Material.PODZOL));
        }
        components.put(new Vector3i(-1, 0, -2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(0, 0, -2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(1, 0, -2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-1, 0, 2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(0, 0, 2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(1, 0, 2), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-2, 0, -1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-2, 0, 0), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-2, 0, 1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(2, 0, -1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(2, 0, 0), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(2, 0, 1), new VanillaMultiblockComponent(Material.COARSE_DIRT));

        components.put(new Vector3i(-1, -1, -1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(0, -1, -1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(1, -1, -1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-1, -1, 0), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(1, -1, 0), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(-1, -1, 1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(0, -1, 1), new VanillaMultiblockComponent(Material.COARSE_DIRT));
        components.put(new Vector3i(1, -1, 1), new VanillaMultiblockComponent(Material.COARSE_DIRT));

        components.put(FIRE_POSITION, new VanillaBlockdataMultiblockComponent(
                Material.CAMPFIRE.createBlockData("[lit=true]"),
                Material.SOUL_CAMPFIRE.createBlockData("[lit=true]"),
                Material.FIRE.createBlockData(),
                Material.SOUL_FIRE.createBlockData()
        ));
        return components;
    }
    // </editor-fold>
}
