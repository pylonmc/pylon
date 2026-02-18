package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.PreRebarBlockPlaceEvent;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;


public class Grindstone extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarInteractBlock,
        RebarBreakHandler,
        RebarLogisticBlock,
        RebarRecipeProcessor<GrindstoneRecipe> {

    public static final int CYCLE_DURATION_TICKS = Settings.get(PylonKeys.GRINDSTONE)
            .getOrThrow("cycle-duration-ticks",ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public Grindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .scale(0.3)
                        .translate(0, 0.15, 0)
                        .rotate(Math.PI / 2, 0, 0))
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("block", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.SMOOTH_STONE_SLAB)
                        .addCustomModelDataString(getKey() + ":block")
                )
                .transformation(new TransformBuilder()
                        .translate(0, 0.8, 0))
                .build(getBlock().getLocation().toCenterLocation())
        );
        setRecipeType(GrindstoneRecipe.RECIPE_TYPE);
    }

    @SuppressWarnings("unused")
    public Grindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, new ItemDisplayLogisticSlot(getItemDisplay()));
    }

    @Override
    protected void postLoad() {
        if (isProcessingRecipe()) {
            finishRecipe();
        }
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        return Map.of(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.GRINDSTONE_HANDLE));
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
        ) {
            return;
        }

        event.setCancelled(true);

        if (!isFormedAndFullyLoaded()) {
            return;
        }

        ItemDisplay itemDisplay = getItemDisplay();
        ItemStack oldStack = itemDisplay.getItemStack();
        ItemStack newStack = event.getItem();

        // drop old item
        if (!oldStack.getType().isAir()) {
            getBlock().getWorld().dropItem(
                    getBlock().getLocation().toCenterLocation().add(0, 0.25, 0),
                    oldStack
            );
            itemDisplay.setItemStack(null);
            return;
        }

        // insert new item
        if (newStack != null) {
            itemDisplay.setItemStack(newStack.clone());
            newStack.setAmount(0);
        }
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        drops.add(getItemDisplay().getItemStack());
    }

    public @Nullable GrindstoneRecipe getNextRecipe() {
        if (isProcessingRecipe()) {
            return null;
        }

        ItemStack input = getItemDisplay().getItemStack();
        if (input.getType().isAir()) {
            return null;
        }

        return GrindstoneRecipe.RECIPE_TYPE.getRecipes()
                .stream()
                .filter(recipe -> recipe.input().matches(input) && input.getAmount() >= recipe.input().getAmount())
                .findFirst()
                .orElse(null);
    }

    public boolean tryStartRecipe(@NotNull GrindstoneRecipe nextRecipe) {
        ItemDisplay itemDisplay = getItemDisplay();
        ItemStack input = itemDisplay.getItemStack();
        if (input.getType().isAir()) {
            return false;
        }

        itemDisplay.setItemStack(input.subtract(nextRecipe.input().getAmount()));

        startRecipe(nextRecipe, nextRecipe.timeTicks());

        // Queue up all the animation frames
        // It's easier to do here instead of in the ticker so we don't have to keep track of the current
        // cycle and tick within that cycle
        for (int i = 0; i < nextRecipe.cycles(); i++) {
            for (int j = 0; j < 4; j++) {
                boolean isLast = i == nextRecipe.cycles() - 1 && j == 3;
                double translation = isLast ? 0.8 : 0.5;
                double rotation = (j / 4.0) * 2.0 * Math.PI;
                Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                    PylonUtils.animate(getStoneDisplay(), CYCLE_DURATION_TICKS / 4, getStoneDisplayMatrix(translation, rotation));
                    new ParticleBuilder(Particle.BLOCK)
                        .data(nextRecipe.particleBlockData())
                        .count(10)
                        .location(getBlock().getLocation().toCenterLocation())
                        .spawn();

                    progressRecipe(CYCLE_DURATION_TICKS / 4);
                }, (long) ((i + j/4.0) * CYCLE_DURATION_TICKS));
            }
        }

        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull GrindstoneRecipe recipe) {
        getBlock().getWorld().dropItemNaturally(
                getBlock().getLocation().toCenterLocation().add(0, 0.25, 0),
                recipe.results().getRandom()
        );
    }

    public @NotNull ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    public @NotNull ItemDisplay getStoneDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "block");
    }

    public static @NotNull Matrix4f getStoneDisplayMatrix(double translation, double rotation) {
        return new TransformBuilder()
                .translate(0, translation, 0)
                .rotate(0, rotation, 0)
                .buildForItemDisplay();
    }

    public static final class PlaceListener implements Listener {
        @EventHandler
        private void onPlace(PreRebarBlockPlaceEvent e) {
            if (!e.getBlockSchema().getKey().equals(PylonKeys.GRINDSTONE)) return;
            Slab slab = (Slab) e.getBlock().getBlockData();
            switch (slab.getType()) {
                case TOP -> {
                    slab.setType(Slab.Type.BOTTOM);
                    e.getBlock().setBlockData(slab);
                }
                case BOTTOM -> { /* Allow */ }
                case DOUBLE -> e.setCancelled(true);
            }
        }
    }
}
