package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.util.BurnerProgressItem;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.Map;
import kotlin.Pair;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ProcessorRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public final class SmelteryBurner extends SmelteryComponent implements
        GuiRebarBlock,
        VirtualInventoryRebarBlock,
        TickingRebarBlock,
        LogisticRebarBlock,
        ProcessorRebarBlock {

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory fuelInventory = new VirtualInventory(3);
    private final BurnerProgressItem progressItem = new BurnerProgressItem();

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        setProcessProgressItem(progressItem);
        createLogisticGroup("fuel", LogisticGroupType.BOTH, fuelInventory);
    }

    @Override
    public @NotNull Map<String, Pair<String, Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("lit", new Pair<>(String.valueOf(isProcessing()), 2));
        return properties;
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # i p i # # #",
                        "# # # i x i # # #",
                        "# # # i i i # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', GuiItems.input())
                .addIngredient('p', progressItem)
                .addIngredient('x', fuelInventory)
                .build();
    }

    @Override
    public void tick() {
        progressProcess(getTickInterval());

        SmelteryController controller = getController();
        if (controller == null || !controller.isRunning()) {
            return;
        }

        if (this.isProcessing()) {
            controller.heatAsymptotically(1100); //Hardcoded temperature for now. Add custom fuels with higher temperatures later?
            return;
        }

        for (int i = 0; i < fuelInventory.getSize(); i++) {
            ItemStack item = fuelInventory.getItem(i);
            if (item == null || RebarItem.isRebarItem(item)) {
                continue;
            }

            ItemType itemType = item.getType().asItemType();
            if (itemType == null || !itemType.isFuel()) {
                continue;
            }

            if (itemType.getCraftingRemainingItem() != null) {
                ItemStack remainder = itemType.getCraftingRemainingItem().createItemStack();
                if (!fuelInventory.canHold(remainder)) {
                    continue;
                }
                fuelInventory.setItem(null, i, item.subtract());
                fuelInventory.addItem(null, remainder);
            } else {
                fuelInventory.setItem(null, i, item.subtract());
            }

            startProcess(itemType.getBurnDuration() / 2);
            Furnace furnace = (Furnace) getBlock().getBlockData();
            furnace.setLit(true);
            getBlock().setBlockData(furnace);
            refreshBlockTextureItem();

            break;
        }
    }

    @Override
    public void onProcessFinished() {
        Furnace furnace = (Furnace) getBlock().getBlockData();
        furnace.setLit(false);
        getBlock().setBlockData(furnace);
        refreshBlockTextureItem();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("fuels", fuelInventory);
    }
}
