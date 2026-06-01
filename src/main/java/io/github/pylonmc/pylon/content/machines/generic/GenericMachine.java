package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import java.util.List;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * A generic machine that has a GUI with one input slot and three output slots. The machine must have a setting called
 * "tick-interval" which determines how many ticks it takes to process a recipe. {@link RebarRecipeProcessor#setRecipeType(RecipeType)}
 * must be called in the place constructor.
 *
 * @param <T> the type of recipe this machine processes. Assumed to have exactly one item input and at most three item outputs
 */
public abstract class GenericMachine<T extends RebarRecipe> extends RebarBlock implements
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<T>,
        RebarDirectionalBlock {

    private static final NamespacedKey RESULTS_KEY = pylonKey("results");
    private static final PersistentDataType<?, List<ItemStack>> RESULTS_TYPE = RebarSerializers.LIST.listTypeFrom(RebarSerializers.ITEM_STACK);

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(3);

    private @Nullable List<ItemStack> results;

    @SuppressWarnings("unused")
    public GenericMachine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
        setFacing(context.getFacing());
    }

    @SuppressWarnings("unused")
    public GenericMachine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        results = pdc.get(RESULTS_KEY, RESULTS_TYPE);
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
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, RESULTS_KEY, RESULTS_TYPE, results);
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

        for (T recipe : getRecipeType()) {
            if (tryStartRecipe(recipe, stack)) {
                return;
            }
        }
    }

    protected abstract int getRecipeTicks(@NotNull T recipe);
    protected abstract @NotNull List<ItemStack> getResults(@NotNull T recipe);

    protected boolean tryStartRecipe(T recipe, ItemStack stack) {
        RecipeInput.Item input = (RecipeInput.Item) recipe.getInputs().getFirst();
        if (!input.matches(stack)) {
            return false;
        }

        List<ItemStack> results = getResults(recipe);
        if (results.size() > 3) {
            throw new IllegalStateException("Recipe has more than 3 results, which is not supported by GenericMachine");
        }

        if (!outputInventory.canHold(results)) {
            return true;
        }

        this.results = results;
        startRecipe(recipe, getRecipeTicks(recipe));
        getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(input.getAmount()));
        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull T recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        List<ItemStack> results = this.results;
        this.results = null;
        for (ItemStack result : results) {
            outputInventory.addItem(new MachineUpdateReason(), result);
        }
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
