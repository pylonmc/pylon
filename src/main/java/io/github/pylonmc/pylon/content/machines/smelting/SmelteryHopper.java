package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.github.pylonmc.pylon.api.MeltingPoint;
import io.github.pylonmc.pylon.recipes.MeltingRecipe;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VanillaInventoryRebarBlockHandler;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.VanillaInventoryLogisticSlot;

public final class SmelteryHopper extends SmelteryComponent implements
        TickingRebarBlock,
        VanillaInventoryRebarBlockHandler,
        LogisticRebarBlock,
        BlockBreakRebarBlockHandler {

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public SmelteryHopper(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public SmelteryHopper(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        Hopper hopper = (Hopper) getBlock().getState();
        createLogisticGroup(
                "input",
                LogisticGroupType.INPUT,
                new VanillaInventoryLogisticSlot(getBlock(), hopper.getInventory(), 0),
                new VanillaInventoryLogisticSlot(getBlock(), hopper.getInventory(), 1),
                new VanillaInventoryLogisticSlot(getBlock(), hopper.getInventory(), 2),
                new VanillaInventoryLogisticSlot(getBlock(), hopper.getInventory(), 3),
                new VanillaInventoryLogisticSlot(getBlock(), hopper.getInventory(), 4)
        );
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onItemMoveFrom(InventoryMoveItemEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override
    public void onBlockBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        Hopper hopper = (Hopper) getBlock().getState();

        for (ItemStack item : hopper.getInventory()) {
            if (item != null) {
                drops.add(item);
            }
        }
    }

    @Override
    public void tick() {
        SmelteryController controller = getController();
        if (controller == null) return;
        Hopper hopper = (Hopper) getBlock().getState(false);
        for (ItemStack item : hopper.getInventory().getContents()) {
            if (item == null) continue;
            MeltingRecipe recipe = null;
            for (MeltingRecipe meltingRecipe : MeltingRecipe.RECIPE_TYPE) {
                if (meltingRecipe.input().matchesIgnoringAmount(item)) {
                    recipe = meltingRecipe;
                    break;
                }
            }
            if (recipe == null) continue;
            double fluidAmountAfterAdding = controller.getTotalFluid() + recipe.resultAmount();
            double temperature = recipe.result().getTag(MeltingPoint.class).temperature();
            if (controller.getTemperature() >= temperature && fluidAmountAfterAdding <= controller.getCapacity()) {
                controller.addFluid(recipe.result(), recipe.resultAmount());
                item.subtract();
                break;
            }
        }
    }
}
