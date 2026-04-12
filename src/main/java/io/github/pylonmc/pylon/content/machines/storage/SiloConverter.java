package io.github.pylonmc.pylon.content.machines.storage;

import io.github.pylonmc.pylon.recipes.SiloConverterRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.Map;


public class SiloConverter extends RebarBlock implements RebarGuiBlock, RebarVirtualInventoryBlock {

    public final ItemStackBuilder materialStack = ItemStackBuilder.gui(Material.CYAN_STAINED_GLASS_PANE, getKey() + ":material")
            .name(Component.translatable("pylon.gui.material"));

    public final VirtualInventory inputInventory = new VirtualInventory(1);
    public final VirtualInventory materialInventory = new VirtualInventory(1);
    public final VirtualInventory outputInventory = new VirtualInventory(1);

    public SiloConverter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public SiloConverter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        outputInventory.addPreUpdateHandler(event -> {
            if (event.getUpdateReason() instanceof MachineUpdateReason) {
                return;
            }

            if (!event.isRemove()) {
                event.setCancelled(true);
                return;
            }
            inputInventory.setItemAmount(new MachineUpdateReason(), 0, inputInventory.getItem(0).getAmount() - 1);
            materialInventory.setItemAmount(new MachineUpdateReason(), 0, materialInventory.getItem(0).getAmount() - 1);
        });
        inputInventory.addPostUpdateHandler(event -> recomputeOutput());
        materialInventory.addPostUpdateHandler(event -> recomputeOutput());
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# I # M # # # O #",
                        "# i # m # # # o #",
                        "# I # M # # # O #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('M', materialStack)
                .addIngredient('O', GuiItems.output())
                .addIngredient('i', inputInventory)
                .addIngredient('m', materialInventory)
                .addIngredient('o', outputInventory)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "material", materialInventory,
                "output", outputInventory
        );
    }

    public void recomputeOutput() {
        ItemStack input = inputInventory.getItem(0);
        ItemStack material = materialInventory.getItem(0);

        if (material == null || !(RebarItem.fromStack(input) instanceof Silo.Item inputSilo)) {
            outputInventory.setItem(new MachineUpdateReason(), 0, null);
            return;
        }

        ItemStack inputSiloStack = inputSilo.getSiloStack();
        Long inputSiloAmount = inputSilo.getSiloAmount();

        for (SiloConverterRecipe recipe : SiloConverterRecipe.RECIPE_TYPE) {
            if (!recipe.material().matches(material)) {
                continue;
            }

            Silo.Item newSilo = RebarItem.fromStack(recipe.result().clone(), Silo.Item.class);

            if (newSilo.getKey().equals(RebarItem.fromStack(input).getKey())) {
                continue;
            }

            if (inputSiloStack != null && inputSiloAmount != null) {
                long newSiloCapacity = newSilo.capacityStacks * inputSiloStack.getMaxStackSize();
                if (inputSiloAmount > newSiloCapacity) {
                    continue;
                }
            }

            if (inputSiloStack != null) {
                newSilo.setSiloStack(inputSiloStack);
            }
            if (inputSiloAmount != null) {
                newSilo.setSiloAmount(inputSiloAmount);
            }

            outputInventory.setItem(new MachineUpdateReason(), 0, newSilo.getStack());
            break;
        }
    }
}
