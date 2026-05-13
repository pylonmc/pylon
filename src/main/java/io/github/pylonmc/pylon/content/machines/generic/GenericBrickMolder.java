package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.recipes.MoldingRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public abstract class GenericBrickMolder extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<MoldingRecipe> {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int ticksPerMoldingCycle = getSettings().getOrThrow("ticks-per-molding-cycle", ConfigAdapter.INTEGER);

    public GenericBrickMolder(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
        setRecipeType(MoldingRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
    }

    public GenericBrickMolder(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "output", outputInventory
        );
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        outputInventory.addPostUpdateHandler(event -> tryStartRecipe());
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        });
    }

    public void tryStartRecipe() {
        if (isProcessingRecipe()) {
            return;
        }

        ItemStack stack = inputInventory.getItem(0);
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (getLastRecipe() != null && tryStartRecipe(getLastRecipe(), stack)) {
            return;
        }

        for (MoldingRecipe recipe : MoldingRecipe.RECIPE_TYPE) {
            if (tryStartRecipe(recipe, stack)) {
                break;
            }
        }
    }

    protected boolean tryStartRecipe(MoldingRecipe recipe, ItemStack stack) {
        if (!recipe.input().isSimilar(stack) || !outputInventory.canHold(recipe.result())) {
            return false;
        }

        startRecipe(recipe, recipe.moldingCycles() * tickInterval * ticksPerMoldingCycle);
        getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull MoldingRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        outputInventory.addItem(new MachineUpdateReason(), recipe.result().clone());
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
}
