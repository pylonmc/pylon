package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.VirtualInventoryLogisticSlot;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;


public class CargoFilter extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock {

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory leftInventory = new VirtualInventory(1);
    private final VirtualInventory rightInventory = new VirtualInventory(1);
    private final VirtualInventory filterInventory = new VirtualInventory(5);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder outputLeftStack = ItemStackBuilder.of(Material.YELLOW_CONCRETE)
            .addCustomModelDataString(getKey() + ":output_left");
    public final ItemStackBuilder outputRightStack = ItemStackBuilder.of(Material.LIGHT_BLUE_CONCRETE)
            .addCustomModelDataString(getKey() + ":output_right");

    public final ItemStackBuilder filterGuiStack = ItemStackBuilder.gui(Material.PINK_STAINED_GLASS_PANE, getKey() + "filter")
            .name(Component.translatable("pylon.gui.filter"));

    public final ItemStackBuilder leftGuiStack = ItemStackBuilder.gui(Material.YELLOW_STAINED_GLASS_PANE, getKey() + "left")
            .name(Component.translatable("pylon.gui.left"));
    public final ItemStackBuilder rightGuiStack = ItemStackBuilder.gui(Material.LIGHT_BLUE_STAINED_GLASS_PANE, getKey() + "right")
            .name(Component.translatable("pylon.gui.right"));

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "left", leftInventory,
                "right", rightInventory,
                "filter", filterInventory
        );
    }

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

    @SuppressWarnings("unused")
    public CargoFilter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing(), "input");
        addCargoLogisticGroup(RebarUtils.rotateFaceToReference(getFacing(), BlockFace.EAST), "left");
        addCargoLogisticGroup(RebarUtils.rotateFaceToReference(getFacing(), BlockFace.WEST), "right");
        setCargoTransferRate(transferRate);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.7)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("comparator", new BlockDisplayBuilder()
                .blockData(Material.COMPARATOR.createBlockData("[powered=false]"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.6, 0)
                        .scale(0.5)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("input", new ItemDisplayBuilder()
                .itemStack(inputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, 0.2)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output_left", new ItemDisplayBuilder()
                .itemStack(outputLeftStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.2, 0, 0)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output_right", new ItemDisplayBuilder()
                .itemStack(outputRightStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.2, 0, 0)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public CargoFilter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# L # # I # # R #",
                        "# l # # i # # r #",
                        "# L # # I # # R #",
                        "# # # # # # # # #",
                        "# F f f f f f F #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('L', leftGuiStack)
                .addIngredient('l', leftInventory)
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('R', rightGuiStack)
                .addIngredient('r', rightInventory)
                .addIngredient('f', filterInventory)
                .addIngredient('F', filterGuiStack)
                .build();
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("input", LogisticGroupType.INPUT, new VirtualInventoryLogisticSlot(inputInventory, 0));
        createLogisticGroup("left", LogisticGroupType.OUTPUT, new VirtualInventoryLogisticSlot(leftInventory, 0));
        createLogisticGroup("right", LogisticGroupType.OUTPUT, new VirtualInventoryLogisticSlot(rightInventory, 0));
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doSplit();
            }
        });
        leftInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doSplit();
            }
        });
        rightInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doSplit();
            }
        });
        filterInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doSplit();
            }
        });
    }

    private void doSplit() {
        ItemStack input = inputInventory.getItem(0);
        if (input == null) {
            return;
        }

        boolean matchesFilter = false;
        for (ItemStack filterStack : filterInventory.getItems()) {
            if (input.isSimilar(filterStack)) {
                matchesFilter = true;
                break;
            }
        }

        getHeldEntityOrThrow(BlockDisplay.class, "comparator")
                .setBlock(Material.COMPARATOR.createBlockData("[powered=" + (matchesFilter ? "true" : "false") +"]"));

        if (matchesFilter) {
            ItemStack filteredStack = leftInventory.getItem(0);
            if (filteredStack == null
                    || (filteredStack.isSimilar(input) && filteredStack.getAmount() < filteredStack.getMaxStackSize())
            ) {
                if (filteredStack == null) {
                    leftInventory.setItem(new MachineUpdateReason(), 0, input);
                    inputInventory.setItem(new MachineUpdateReason(), 0, null);
                } else {
                    int newAmount = Math.min(filteredStack.getMaxStackSize(), filteredStack.getAmount() + input.getAmount());
                    int toSubtract = newAmount - filteredStack.getAmount();
                    leftInventory.setItem(new MachineUpdateReason(), 0, filteredStack.asQuantity(newAmount));
                    inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract(toSubtract));
                }
            }
        } else {
            ItemStack unfilteredStack = rightInventory.getItem(0);
            if (unfilteredStack == null
                    || (unfilteredStack.isSimilar(input) && unfilteredStack.getAmount() < unfilteredStack.getMaxStackSize())
            ) {
                if (unfilteredStack == null) {
                    rightInventory.setItem(new MachineUpdateReason(), 0, input);
                    inputInventory.setItem(new MachineUpdateReason(), 0, null);
                } else {
                    int newAmount = Math.min(unfilteredStack.getMaxStackSize(), unfilteredStack.getAmount() + input.getAmount());
                    int toSubtract = newAmount - unfilteredStack.getAmount();
                    rightInventory.setItem(new MachineUpdateReason(), 0, unfilteredStack.asQuantity(newAmount));
                    inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract(toSubtract));
                }
            }
        }
    }
}
