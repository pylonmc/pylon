package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.PitKilnRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.time.Duration;
import java.util.*;

public final class PitKiln extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarGuiBlock,
        RebarTickingBlock,
        RebarVirtualInventoryBlock,
        RebarRecipeProcessor<PitKilnRecipe> {

    public static final int PROCESSING_TIME_SECONDS =
            Settings.get(PylonKeys.PIT_KILN).getOrThrow("processing-time-seconds", ConfigAdapter.INTEGER);

    private static final double MULTIPLIER_CAMPFIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.campfire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_SOUL_CAMPFIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.soul-campfire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_FIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.fire", ConfigAdapter.DOUBLE);
    private static final double MULTIPLIER_SOUL_FIRE = Settings.get(PylonKeys.PIT_KILN).getOrThrow("speed-multipliers.soul-fire", ConfigAdapter.DOUBLE);

    public static final class Item extends RebarItem {

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("smelting_time", UnitFormat.formatDuration(Duration.ofSeconds(PROCESSING_TIME_SECONDS), false)),
                    RebarArgument.of("campfire", MULTIPLIER_CAMPFIRE),
                    RebarArgument.of("soul_campfire", MULTIPLIER_SOUL_CAMPFIRE),
                    RebarArgument.of("fire", MULTIPLIER_FIRE),
                    RebarArgument.of("soul_fire", MULTIPLIER_SOUL_FIRE)
            );
        }
    }

    private final VirtualInventory inventory = new VirtualInventory(3);

    @SuppressWarnings("unused")
    public PitKiln(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public PitKiln(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postBreak(@NotNull BlockBreakContext context) {
        removeWailas();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # x x x # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', inventory)
                .build();
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            if (isProcessingRecipe()) {
                stopRecipe();
            }
            return;
        }
        if (!isProcessingRecipe()) {
            tryStartProcessing();
        }

        if (!isProcessingRecipe()) return;
        progressRecipe(getTickInterval());
        if (Objects.requireNonNull(getRecipeTicksRemaining()) > 0) return;

        finishRecipe();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onRecipeFinished(@NonNull PitKilnRecipe recipe) {
        // Remove blocks to consume
        for (Vector3i coal : COAL_POSITIONS) {
            Block coalBlock = getBlock().getRelative(coal.x(), coal.y(), coal.z());
            new BlockBreakBlockEvent(coalBlock, getBlock(), List.of()).callEvent();
            coalBlock.setType(Material.AIR);
        }
        Block fireBlock = getBlock().getRelative(FIRE_POSITION.x(), FIRE_POSITION.y(), FIRE_POSITION.z());
        switch (fireBlock.getType()) {
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
                if (!(fireBlock.getBlockData() instanceof Campfire campfireData)) {
                    break;
                }
                campfireData.setLit(false);
                fireBlock.getWorld().playSound(fireBlock.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                fireBlock.setBlockData(campfireData);
                break;
            default:
                new BlockBreakBlockEvent(fireBlock, getBlock(), List.of()).callEvent();
                fireBlock.getWorld().playSound(fireBlock.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                fireBlock.setType(Material.AIR);
        }

        // Determine amount of times to perform the recipe
        Object2IntMap<ItemStack> amounts = countItems();
        int ratio = Integer.MAX_VALUE;
        for (RecipeInput.Item input : recipe.input()) {
            for (var entry : amounts.object2IntEntrySet()) {
                if (input.contains(entry.getKey())) {
                    ratio = Math.min(ratio, entry.getIntValue() / input.getAmount());
                    break;
                }
            }
        }
        if (ratio == 0) {
            throw new IllegalStateException("Ratio was 0");
        }

        // Remove inputs
        for (RecipeInput.Item input : recipe.input()) {
            int remaining = input.getAmount() * ratio;
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null || !input.contains(item)) continue;

                int toTake = Math.min(remaining, item.getAmount());
                inventory.setItemAmount(new MachineUpdateReason(), slot, item.getAmount() - toTake);

                remaining -= toTake;
                if (remaining == 0) break;
            }
            if (remaining > 0) {
                throw new IllegalStateException("Had items remaining");
            }
        }

        // Add outputs
        List<ItemStack> extra = new ArrayList<>();
        for (ItemStack output : recipe.output()) {
            ItemStack item = output.asQuantity(output.getAmount() * ratio);
            int amountLeft = inventory.addItem(new MachineUpdateReason(), item);
            if (amountLeft > 0) {
                extra.add(output.asQuantity(amountLeft));
            }
        }

        // Drop outputs that didn't fit below (fire is out anyway)
        Location down = getBlock().getRelative(BlockFace.DOWN).getLocation().toCenterLocation();
        for (ItemStack item : extra) {
            down.getWorld().dropItemNaturally(down, item);
        }
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        Integer processingTime = getRecipeTicksRemaining();
        Component status = processingTime != null ?
                Component.translatable(
                        "pylon.waila.pit_kiln.smelting",
                        RebarArgument.of(
                                "time",
                                UnitFormat.formatDuration(Duration.ofSeconds(processingTime / 20), false)
                        )
                ) :
                Component.translatable("pylon.waila.pit_kiln.invalid_recipe");
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
            Waila.addWailaOverride(block, this::getWaila);
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

    private Object2IntMap<ItemStack> countItems() {
        Object2IntMap<ItemStack> amounts = new Object2IntOpenHashMap<>();
        amounts.defaultReturnValue(0);
        for (ItemStack item : inventory.getItems()) {
            if (item == null) continue;
            amounts.mergeInt(item.asOne(), item.getAmount(), Integer::sum);
        }
        return amounts;
    }

    private void tryStartProcessing() {
        if (isProcessingRecipe() || inventory.isEmpty()) return;
        Object2IntMap<ItemStack> amounts = countItems();
        recipeLoop:
        for (PitKilnRecipe recipe : PitKilnRecipe.RECIPE_TYPE) {
            for (RecipeInput.Item input : recipe.input()) {
                boolean found = false;
                for (var entry : amounts.object2IntEntrySet()) {
                    if (input.contains(entry.getKey()) && entry.getIntValue() >= input.getAmount()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue recipeLoop;
                }
            }
            double multiplier = switch (getBlock().getRelative(BlockFace.DOWN).getType()) {
                case CAMPFIRE -> MULTIPLIER_CAMPFIRE;
                case SOUL_CAMPFIRE -> MULTIPLIER_SOUL_CAMPFIRE;
                case FIRE -> MULTIPLIER_FIRE;
                case SOUL_FIRE -> MULTIPLIER_SOUL_FIRE;
                default -> throw new AssertionError("Should not happen");
            };
            startRecipe(recipe, (int) (PROCESSING_TIME_SECONDS * 20 / multiplier));
            break;
        }
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
            components.put(
                    coalPosition,
                    new MixedMultiblockComponent(
                            new VanillaMultiblockComponent(Material.COAL_BLOCK),
                            new RebarMultiblockComponent(PylonKeys.CHARCOAL_BLOCK)
                    )
            );
        }

        for (Vector3i topPosition : TOP_POSITIONS) {
            components.put(topPosition, new VanillaMultiblockComponent(Material.COARSE_DIRT));
        }

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
