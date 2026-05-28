package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.recipes.TableSawRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public abstract class GenericTableSaw extends RebarBlock implements
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarEntityHolderBlock,
        RebarRecipeProcessor<TableSawRecipe> {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    protected final ItemStackBuilder sawStack = ItemStackBuilder.of(Material.IRON_BARS)
            .addCustomModelDataString(getKey() + ":saw");

    @SuppressWarnings("unused")
    public GenericTableSaw(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
        setRecipeType(TableSawRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .scale(0.3))
                .build(block.getLocation().toCenterLocation().add(0, 0.65, 0))
        );
        addEntity("saw", new ItemDisplayBuilder()
                .itemStack(sawStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.4, 0.4))
                .build(block.getLocation().toCenterLocation().add(0, 0.7, 0))
        );
    }

    @SuppressWarnings("unused")
    public GenericTableSaw(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

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

        for (TableSawRecipe recipe : TableSawRecipe.RECIPE_TYPE) {
            if (tryStartRecipe(recipe, stack)) {
                break;
            }
        }
    }

    protected boolean tryStartRecipe(TableSawRecipe recipe, ItemStack stack) {
        if (!recipe.input().isSimilar(stack) || !outputInventory.canHold(recipe.result())) {
            return false;
        }

        startRecipe(recipe, recipe.timeTicks());
        getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
        getItemDisplay().setItemStack(stack);
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull TableSawRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        getItemDisplay().setItemStack(null);
        outputInventory.addItem(null, recipe.result().clone());
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

    public ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }
}
