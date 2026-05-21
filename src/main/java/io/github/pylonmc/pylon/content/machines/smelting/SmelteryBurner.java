package io.github.pylonmc.pylon.content.machines.smelting;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import kotlin.Pair;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

public final class SmelteryBurner extends SmelteryComponent implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarProcessor {

    private final ItemStackBuilder notBurningProgressItem = ItemStackBuilder.of(Material.CHARCOAL)
            .name(Component.translatable("pylon.gui.smeltery_burner.not_burning"));
    private final ItemStackBuilder burningProgressItem = ItemStackBuilder.of(Material.BLAZE_POWDER)
            .name(Component.translatable("pylon.gui.smeltery_burner.burning"));

    private final VirtualInventory inventory = new VirtualInventory(3);
    private final ProgressItem progressItem = new ProgressItem(notBurningProgressItem);

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(SmelteryController.TICK_INTERVAL);
    }

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        setProcessProgressItem(progressItem);
        createLogisticGroup("fuel", LogisticGroupType.BOTH, inventory);
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
                        "# # # # f # # # #",
                        "# # # x x x # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('f', progressItem)
                .addIngredient('x', inventory)
                .addIngredient('#', GuiItems.background())
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

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                continue;
            }

            ItemType itemType = item.getType().asItemType();
            if (itemType == null || !itemType.isFuel()) {
                continue;
            }

            progressItem.setItem(burningProgressItem);

            if (item.getType() == Material.LAVA_BUCKET) {
                inventory.setItem(null, i, new ItemStack(Material.BUCKET));
            } else {
                inventory.setItem(null, i, item.subtract());
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
        progressItem.setItem(notBurningProgressItem);

        Furnace furnace = (Furnace) getBlock().getBlockData();
        furnace.setLit(false);
        getBlock().setBlockData(furnace);
        refreshBlockTextureItem();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("fuels", inventory);
    }
}
