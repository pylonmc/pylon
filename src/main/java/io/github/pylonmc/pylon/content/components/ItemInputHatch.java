package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.LogisticRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.papermc.paper.persistence.PersistentDataKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class ItemInputHatch extends RebarBlock implements
        GuiRebarBlock,
        DirectionalRebarBlock,
        LogisticRebarBlock,
        VirtualInventoryRebarBlock {

    private static final PersistentDataKey<Set<ItemTypeWrapper>> ALLOWED_ITEMS_KEY = PersistentDataKey.of(
            pylonKey("allowed_items"),
            RebarSerializers.SET.setTypeFrom(RebarSerializers.KEYED.keyedTypeFrom(ItemTypeWrapper.class, ItemTypeWrapper::of))
    );

    @Getter
    private Set<ItemTypeWrapper> allowedItems = null;

    public final VirtualInventory inventory = new VirtualInventory(1);

    public ItemInputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
    }

    public ItemInputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        allowedItems = pdc.get(ALLOWED_ITEMS_KEY);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, ALLOWED_ITEMS_KEY, allowedItems);
    }

    public @Nullable ItemStack getItem() {
        return inventory.getItem(0);
    }

    @Override
    public void postInitialise() {
        inventory.addPreUpdateHandler(e -> {
            if (e.getNewItem() != null && allowedItems != null && !allowedItems.contains(ItemTypeWrapper.of(e.getNewItem()))) {
                e.setCancelled(true);
            }
        });
        createLogisticGroup("input", LogisticGroupType.INPUT, inventory);
    }

    public void setAllowedItems(Set<ItemTypeWrapper> allowedItems) {
        this.allowedItems = allowedItems;
        refreshGui();
    }

    @Override
    public @NotNull Gui createGui() {
        List<ItemStack> allowedItems = null;
        if (this.allowedItems != null) {
            allowedItems = new ArrayList<>();
            for (ItemTypeWrapper wrapper : this.allowedItems) {
                allowedItems.add(wrapper.createItemStack());
            }
        }
        return Gui.builder()
                .setStructure(
                        "# # # # a # # # #",
                        "# # # # x # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('a', allowedItems == null ? GuiItems.background() : ItemButton.of(allowedItems))
                .addIngredient('x', inventory)
                .build();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }
}
