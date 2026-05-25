package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.util.BurnerProgressItem;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeType;
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeWrapper;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public class ElectricFurnace extends RebarBlock implements
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarNoVanillaInventoryBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<FurnaceRecipeWrapper>,
        RebarDirectionalBlock,
        RebarElectricConsumerBlock {

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);
    private final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100)),
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricFurnace(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
        setRecipeType(FurnaceRecipeType.INSTANCE);
        setRecipeProgressItem(new BurnerProgressItem());
    }

    @SuppressWarnings("unused")
    public ElectricFurnace(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("input", inputInventory, "output", outputInventory);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        outputInventory.addPostUpdateHandler(_ -> tryStartRecipe());
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # I # # # O # #",
                        "# # i # p # o # #",
                        "# # I # # # O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .build();
    }

    private void tryStartRecipe() {
        if (isProcessingRecipe()) return;

        ItemStack stack = inputInventory.getItem(0);
        if (stack == null || stack.isEmpty()) return;

        if (getLastRecipe() != null && tryStartRecipe(getLastRecipe(), stack)) return;

        for (FurnaceRecipeWrapper recipe : FurnaceRecipeType.INSTANCE) {
            if (tryStartRecipe(recipe, stack)) {
                break;
            }
        }
    }

    private boolean tryStartRecipe(FurnaceRecipeWrapper recipe, ItemStack stack) {
        if (!recipe.isInput(stack) || !outputInventory.canHold(recipe.getRecipe().getResult())) {
            return false;
        }

        startRecipe(recipe, (int) Math.ceil(recipe.getRecipe().getCookingTime() / speed));
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract());
        Furnace furnaceData = (Furnace) getBlock().getBlockData();
        furnaceData.setLit(true);
        getBlock().setBlockData(furnaceData);
        refreshBlockTextureItem();
        setRequiredPower(powerUsage);
        return true;
    }

    @Override
    public void onRecipeFinished(@NonNull FurnaceRecipeWrapper recipe) {
        Furnace furnaceData = (Furnace) getBlock().getBlockData();
        furnaceData.setLit(false);
        getBlock().setBlockData(furnaceData);
        refreshBlockTextureItem();
        setRequiredPower(0);
        outputInventory.addItem(new MachineUpdateReason(), recipe.getRecipe().getResult());
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(tickInterval);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Pair<@NotNull String, @NotNull Integer>> getBlockTextureProperties() {
        var props = super.getBlockTextureProperties();
        props.put("lit", new Pair<>(String.valueOf(isProcessingRecipe()), 2));
        return props;
    }
}