package io.github.pylonmc.pylon.content.machines.cargo;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityCulledBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.cargo.CargoDuct;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.RebarCargoConnectEvent;
import io.github.pylonmc.rebar.event.RebarCargoDisconnectEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroup;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.LogisticSlot;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class CargoExtractor extends CargoInteractor implements
        RebarCargoBlock,
        RebarTickingBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarEntityCulledBlock
{

    public static final NamespacedKey ITEMS_TO_FILTER_KEY = PylonUtils.pylonKey("items_to_filter");
    public static final NamespacedKey IS_WHITELIST_KEY = PylonUtils.pylonKey("is_whitelist");

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");
    public final ItemStackBuilder ductStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":duct");

    public final ItemStackBuilder filterGuiStack = ItemStackBuilder.gui(Material.PINK_STAINED_GLASS_PANE, getKey() + "filter")
            .name(Component.translatable("pylon.gui.filter"));

    private final VirtualInventory outputInventory = new VirtualInventory(1);
    private final VirtualInventory filterInventory = new VirtualInventory(5);

    public Set<ItemStack> itemsToFilter = new HashSet<>();
    public boolean isWhitelist = false;

    public static class Item extends RebarItem {

        public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of(
                            "transfer-rate",
                            UnitFormat.ITEMS_PER_SECOND.format(RebarCargoBlock.cargoItemsTransferredPerSecond(transferRate))
                    )
            );
        }
    }

    public class WhitelistToggleItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return ItemStackBuilder.gui(isWhitelist ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE, "blacklist-whitelist-toggle")
                    .name(Component.translatable("pylon.gui.whitelist-blacklist-toggle."
                            + (isWhitelist ? "whitelist.name" : "blacklist.name")
                    ))
                    .lore(Component.translatable("pylon.gui.whitelist-blacklist-toggle."
                            + (isWhitelist ? "whitelist.lore" : "blacklist.lore")
                    ));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            isWhitelist = !isWhitelist;
            notifyWindows();
        }
    }

    @SuppressWarnings("unused")
    public CargoExtractor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(transferRate * 20);

        addCargoLogisticGroup(getFacing(), "output");
        for (BlockFace face : RebarUtils.perpendicularImmediateFaces(getFacing())) {
            addCargoLogisticGroup(face, "output");
        }
        setCargoTransferRate(transferRate);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.4)
                        .scale(0.65, 0.65, 0.2)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output", new ItemDisplayBuilder()
                .itemStack(outputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.3)
                        .scale(0.45, 0.45, 0.05)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("duct", new ItemDisplayBuilder()
                .itemStack(ductStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.0625)
                        .scale(0.35, 0.35, 0.475)
                )
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public CargoExtractor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        itemsToFilter = pdc.get(ITEMS_TO_FILTER_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.ITEM_STACK));
        isWhitelist = pdc.get(IS_WHITELIST_KEY, RebarSerializers.BOOLEAN);
    }



    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        super.write(pdc);
        pdc.set(ITEMS_TO_FILTER_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.ITEM_STACK), itemsToFilter);
        pdc.set(IS_WHITELIST_KEY, RebarSerializers.BOOLEAN, isWhitelist);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);

        filterInventory.addPostUpdateHandler(event -> {
            itemsToFilter.clear();
            for (ItemStack stack : filterInventory.getItems()) {
                if (stack != null) {
                    itemsToFilter.add(stack.asOne());
                }
            }
        });
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDuctConnected(@NotNull RebarCargoConnectEvent event, @NotNull EventPriority priority) {
        // Remove all faces that aren't to the connected block - this will make sure only
        // one duct is connected at a time
        for (BlockFace face : getCargoLogisticGroups().keySet()) {
            if (!getBlock().getRelative(face).equals(event.getBlock1().getBlock()) && !getBlock().getRelative(face).equals(event.getBlock2().getBlock())) {
                removeCargoLogisticGroup(face);
            }
        }
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onDuctDisconnected(@NotNull RebarCargoDisconnectEvent event, @NotNull EventPriority priority) {
        // Allow connecting to all faces now that there are zero connections
        List<BlockFace> faces = RebarUtils.perpendicularImmediateFaces(getFacing());
        faces.add(getFacing());
        for (BlockFace face : faces) {
            addCargoLogisticGroup(face, "output");
        }
        for (BlockFace face : faces) {
            if (BlockStorage.get(getBlock().getRelative(face)) instanceof CargoDuct duct) {
                duct.updateConnectedFaces();
            }
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # O # # # #",
                        "# b # # o # # i #",
                        "# # # # O # # # #",
                        "# # # # # # # # #",
                        "# F f f f f f F #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('o', outputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('f', filterInventory)
                .addIngredient('F', filterGuiStack)
                .addIngredient('b', new WhitelistToggleItem())
                .addIngredient('i', new InventoryCycleItem())
                .build();
    }

    @Override
    public void tick() {
        if (targetLogisticGroup == null) {
            return;
        }

        LogisticGroup group = targetGroups.get(targetLogisticGroup);
        Preconditions.checkState(group != null);
        ItemStack output = outputInventory.getItem(0);
        if (output != null && output.getAmount() == output.getMaxStackSize()) {
            return;
        }

        for (LogisticSlot slot : group.getSlots()) {
            ItemStack slotStack = slot.getItemStack();
            if (slotStack == null || slot.getAmount() == 0 || (output != null && !output.isSimilar(slotStack))) {
                continue;
            }

            if (isWhitelist != itemsToFilter.contains(slotStack.asOne())) {
                continue;
            }

            if (output == null) {
                outputInventory.setItem(new MachineUpdateReason(), 0, slotStack.asOne());
            } else {
                outputInventory.setItem(new MachineUpdateReason(), 0, output.add());
            }
            slot.set(slotStack, slot.getAmount() - 1);
            return;
        }
    }

    @Override
    public boolean isValidGroup(@NotNull LogisticGroup group) {
        return group.getSlotType() == LogisticGroupType.BOTH || group.getSlotType() == LogisticGroupType.OUTPUT;
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("output", outputInventory, "filter", filterInventory);
    }

    @Override
    public @NotNull Iterable<UUID> getCulledEntityIds() {
        return getHeldEntities().values();
    }
}
