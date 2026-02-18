package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.PressRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.entity.EntityCompostItemEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;


public class Press extends RebarBlock implements
        RebarInteractBlock,
        RebarFluidBufferBlock,
        RebarComposter,
        RebarDirectionalBlock,
        RebarRecipeProcessor<PressRecipe> {

    private static final Config settings = Settings.get(PylonKeys.PRESS);
    public static final int TIME_PER_ITEM_TICKS = settings.getOrThrow("time-per-item-ticks", ConfigAdapter.INTEGER);
    public static final int RETURN_TO_START_TIME_TICKS = settings.getOrThrow("return-to-start-time-ticks", ConfigAdapter.INTEGER);
    public static final int CAPACITY_MB = settings.getOrThrow("capacity-mb", ConfigAdapter.INTEGER);

    public static class PressItem extends RebarItem {

        public PressItem(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("time-per-item", UnitFormat.SECONDS.format(TIME_PER_ITEM_TICKS / 20.0)),
                    RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(CAPACITY_MB))
            );
        }
    }

    @SuppressWarnings("unused")
    public Press(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        setFacing(context.getFacing());
        addEntity("press_cover", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.SPRUCE_PLANKS)
                        .addCustomModelDataString(getKey() + ":press_cover"))
                .transformation(getCoverTransform(0.4))
                .build(getBlock().getLocation().toCenterLocation())
        );
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.NORTH, context, false);
        createFluidBuffer(PylonFluids.PLANT_OIL, CAPACITY_MB, false, true);
        setRecipeType(PressRecipe.RECIPE_TYPE);
    }

    @SuppressWarnings("unused")
    public Press(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void postLoad() {
        if (isProcessingRecipe()) {
            finishRecipe();
        }
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.PLANT_OIL),
                        fluidCapacity(PylonFluids.PLANT_OIL),
                        20,
                        TextColor.fromHexString("#c4b352")
                ))
        ));
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND || event.getPlayer().isSneaking()) {
            return;
        }

        event.setCancelled(true);

        tryStartRecipe();
    }

    @Override
    public void onCompostByEntity(EntityCompostItemEvent event) {
        event.setCancelled(true);
    }

    public boolean tryStartRecipe() {
        if (isProcessingRecipe()) {
            return false;
        }

        List<Item> items = getBlock()
                .getLocation()
                .toCenterLocation()
                .getNearbyEntities(0.5, 0.8, 0.5)
                .stream()
                .filter(Item.class::isInstance)
                .map(Item.class::cast)
                .toList();

        List<ItemStack> stacks = items.stream()
                .map(Item::getItemStack)
                .toList();

        for (PressRecipe recipe : PressRecipe.RECIPE_TYPE.getRecipes()) {
            for (ItemStack stack : stacks) {
                if (!recipe.input().contains(stack)
                        || recipe.oilAmount() > fluidSpaceRemaining(PylonFluids.PLANT_OIL)) {
                    continue;
                }

                stack.subtract();
                startRecipe(recipe, TIME_PER_ITEM_TICKS);
                PylonUtils.animate(getCover(), TIME_PER_ITEM_TICKS - RETURN_TO_START_TIME_TICKS, getCoverTransform(0.0));
                Bukkit.getScheduler().runTaskLater(
                        Pylon.getInstance(),
                        () -> {
                            PylonUtils.animate(getCover(), RETURN_TO_START_TIME_TICKS, getCoverTransform(0.4));
                            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), this::finishRecipe, RETURN_TO_START_TIME_TICKS);
                        },
                        TIME_PER_ITEM_TICKS - RETURN_TO_START_TIME_TICKS
                );
                return true;
            }
        }

        return false;
    }

    @Override
    public void onRecipeFinished(@NotNull PressRecipe recipe) {
        addFluid(PylonFluids.PLANT_OIL, recipe.oilAmount());
    }

    public @Nullable ItemDisplay getCover() {
        return getHeldEntity(ItemDisplay.class, "press_cover");
    }

    public static @NotNull Matrix4f getCoverTransform(double translation) {
        return new TransformBuilder()
                .translate(0, translation, 0)
                .scale(0.9, 0.1, 0.9)
                .buildForItemDisplay();
    }
}
