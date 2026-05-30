package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.recipes.PressRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public abstract class GenericPress extends RebarBlock implements
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarFluidBufferBlock,
        RebarRecipeProcessor<PressRecipe> {

    public final double timePerItem = getSettings().getOrThrow("time-per-item", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double plantOilBuffer = getSettings().getOrThrow("plant-oil-buffer", ConfigAdapter.DOUBLE);

    protected final VirtualInventory inputInventory = new VirtualInventory(1);
    protected final ItemStackBuilder pressStack = ItemStackBuilder.of(Material.COMPOSTER)
            .addCustomModelDataString(getKey() + ":press");
    protected final ItemStackBuilder pressLidStack = ItemStackBuilder.of(Material.COMPOSTER)
            .addCustomModelDataString(getKey() + ":press_lid");

    @SuppressWarnings("unused")
    public GenericPress(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        setRecipeType(PressRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
        createFluidBuffer(PylonFluids.PLANT_OIL, plantOilBuffer, false, true);
    }

    @SuppressWarnings("unused")
    public GenericPress(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        });
    }

    protected void addPressEntities(@NotNull Block block) {
        addEntity("press", new ItemDisplayBuilder()
                .itemStack(pressStack)
                .transformation(new TransformBuilder()
                        .translate(0, 0.3, 0)
                        .scale(0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("press_lid", new ItemDisplayBuilder()
                .itemStack(pressLidStack)
                .transformation(new TransformBuilder()
                        .translate(0, 0.3, 0)
                        .rotate(Math.PI, 0, 0)
                        .scale(0.5999))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
    }

    @Override
    public boolean setFluid(@NotNull RebarFluid fluid, double amount) {
        double current = fluidAmount(fluid);
        boolean output = RebarFluidBufferBlock.super.setFluid(fluid, amount);
        if (amount < current) {
            tryStartRecipe();
        }

        return output;
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

        for (PressRecipe recipe : PressRecipe.RECIPE_TYPE) {
            if (tryStartRecipe(recipe, stack)) {
                break;
            }
        }
    }

    protected boolean tryStartRecipe(PressRecipe recipe, ItemStack stack) {
        double plantOilAmount = recipe.oilAmount();
        if (fluidSpaceRemaining(PylonFluids.PLANT_OIL) < plantOilAmount || !recipe.input().matches(stack)) {
            return false;
        }

        startRecipe(recipe, (int) (timePerItem * 20));
        getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
        inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
        return true;
    }

    @Override
    public void onRecipeFinished(@NotNull PressRecipe recipe) {
        addFluid(PylonFluids.PLANT_OIL, recipe.oilAmount());
        getRecipeProgressItem().setItem(GuiItems.background());
        tryStartRecipe();
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # I # # # #",
                        "# # # # i # # # #",
                        "# # # # p # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .build();
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory
        );
    }
}





