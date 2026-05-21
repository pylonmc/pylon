package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
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
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public abstract class GenericGrindstone extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<GrindstoneRecipe> {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(3);

    @SuppressWarnings("unused")
    public GenericGrindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setRecipeType(GrindstoneRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
    }

    @SuppressWarnings("unused")
    public GenericGrindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
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
        if (stack == null) {
            return;
        }

        if (getLastRecipe() != null && tryStartRecipe(getLastRecipe(), stack)) {
            return;
        }

        for (GrindstoneRecipe recipe : GrindstoneRecipe.RECIPE_TYPE) {
            if (tryStartRecipe(recipe, stack)) {
                return;
            }
        }
    }

    private boolean tryStartRecipe(GrindstoneRecipe recipe, ItemStack stack) {
        if (!recipe.input().matches(stack)) {
            return false;
        }

        if (!outputInventory.canHold(List.copyOf(recipe.results().getElements()))) {
            return true;
        }

        startRecipe(recipe, recipe.cycles() * Grindstone.CYCLE_DURATION_TICKS);
        getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull GrindstoneRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        outputInventory.addItem(null, recipe.results().getRandom());
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# I # # # O O O #",
                        "# i # p # o o o #",
                        "# I # # # O O O #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .build();
    }
    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "output", outputInventory
        );
    }
}
