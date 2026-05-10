package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.recipes.FormingRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.ScrollGui;
import xyz.xenondevs.invui.inventory.OperationCategory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;

public class FormingTable extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock {

    @SuppressWarnings("unused")
    public FormingTable(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public FormingTable(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    private final VirtualInventory input = new VirtualInventory(1);
    private final VirtualInventory output = new VirtualInventory(1);

    @Override
    public void postInitialise() {
        output.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        input.setGuiPriority(OperationCategory.ADD, 1);
        output.setGuiPriority(OperationCategory.COLLECT, 1);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("input", input, "output", output);
    }

    @AllArgsConstructor
    private class FormButton extends AbstractItem {
        private final FormingRecipe recipe;

        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return new ItemWrapper(recipe.result());
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            ItemStack inputItem = input.getItem(0);
            if (inputItem == null || !recipe.input().matches(inputItem) || !output.canHold(recipe.result())) return;
            inputItem.subtract(recipe.input().getAmount());
            input.setItem(new MachineUpdateReason(), 0, inputItem);
            output.addItem(new MachineUpdateReason(), recipe.result());
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return ScrollGui.itemsBuilder()
                .setStructure(
                        "# I I I # O O O #",
                        "# I i I # O o O #",
                        "# I I I # O O O #",
                        "# # # # # # # # #",
                        "# < x x x x x > #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('O', GuiItems.output())
                .addIngredient('i', input)
                .addIngredient('o', output)
                .addIngredient('<', GuiItems.scrollLeft())
                .addIngredient('>', GuiItems.scrollRight())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .setContent(FormingRecipe.RECIPE_TYPE.stream().map(FormButton::new).toList())
                .build();
    }
}
