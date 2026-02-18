package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
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
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CargoGate extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock {

    public static final NamespacedKey THRESHOLD_KEY = pylonKey("threshold");
    public static final NamespacedKey ITEMS_REMAINING_KEY = pylonKey("items_remaining");

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public int threshold = 1;
    public int itemsRemaining = 1;

    private final VirtualInventory outputInventory = new VirtualInventory(1);
    private final VirtualInventory leftInventory = new VirtualInventory(1);
    private final VirtualInventory rightInventory = new VirtualInventory(1);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");
    public final ItemStackBuilder inputLeftStack = ItemStackBuilder.of(Material.YELLOW_CONCRETE)
            .addCustomModelDataString(getKey() + ":input_left");
    public final ItemStackBuilder inputRightStack = ItemStackBuilder.of(Material.LIGHT_BLUE_CONCRETE)
            .addCustomModelDataString(getKey() + ":input_right");

    public final ItemStackBuilder leftStack = ItemStackBuilder.gui(Material.YELLOW_STAINED_GLASS_PANE, getKey() + "left")
            .name(Component.translatable("pylon.gui.left"));
    public final ItemStackBuilder rightStack = ItemStackBuilder.gui(Material.LIGHT_BLUE_STAINED_GLASS_PANE, getKey() + "right")
            .name(Component.translatable("pylon.gui.right"));
    public final ItemStackBuilder thresholdButtonStack = ItemStackBuilder.gui(Material.WHITE_CONCRETE, getKey() + "threshold_button")
            .lore(Component.translatable("pylon.gui.threshold_button.lore"));

    public class ThresholdButton extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return thresholdButtonStack
                .name((Component.translatable("pylon.gui.threshold_button.name").arguments(
                        RebarArgument.of("threshold", threshold)
                )));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            if (clickType.isLeftClick()) {
                threshold += 1;
            } else {
                threshold = Math.max(1, threshold - 1);
            }
            itemsRemaining = threshold;
            notifyWindows();
        }
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
    public CargoGate(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing().getOppositeFace(), "output");
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

        addEntity("repeater", new BlockDisplayBuilder()
                .blockData(Material.REPEATER.createBlockData("[powered=false]"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.6, 0)
                        .scale(0.5)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("input", new ItemDisplayBuilder()
                .itemStack(outputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.2)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output_left", new ItemDisplayBuilder()
                .itemStack(inputLeftStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.2, 0, 0)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output_right", new ItemDisplayBuilder()
                .itemStack(inputRightStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.2, 0, 0)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public CargoGate(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        threshold = pdc.get(THRESHOLD_KEY, RebarSerializers.INTEGER);
        itemsRemaining = pdc.get(ITEMS_REMAINING_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(THRESHOLD_KEY, RebarSerializers.INTEGER, threshold);
        pdc.set(ITEMS_REMAINING_KEY, RebarSerializers.INTEGER, itemsRemaining);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# L # # O # # R #",
                        "# l # # o # # r #",
                        "# L # # O # # R #",
                        "# # # # # # # # #",
                        "# # # # t # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('L', leftStack)
                .addIngredient('l', leftInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .addIngredient('R', rightStack)
                .addIngredient('r', rightInventory)
                .addIngredient('t', new ThresholdButton())
                .build();
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, new VirtualInventoryLogisticSlot(outputInventory, 0));
        createLogisticGroup("left", LogisticGroupType.INPUT, new VirtualInventoryLogisticSlot(leftInventory, 0));
        createLogisticGroup("right", LogisticGroupType.INPUT, new VirtualInventoryLogisticSlot(rightInventory, 0));
        outputInventory.addPostUpdateHandler(event -> {
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
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("threshold", threshold),
                RebarArgument.of("side", itemsRemaining == 0
                                ? Component.translatable("pylon.waila.cargo_splitter.left")
                                : Component.translatable("pylon.waila.cargo_splitter.right")
                )
        ));
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "output", outputInventory,
                "left", leftInventory,
                "right", rightInventory
        );
    }

    private void doSplit() {
        getHeldEntityOrThrow(BlockDisplay.class, "repeater")
                .setBlock(Material.REPEATER.createBlockData("[powered=" + (itemsRemaining == 0 ? "true" : "false") + "]"));

        VirtualInventory inputInventory = itemsRemaining == 0 ? rightInventory : leftInventory;
        ItemStack input = inputInventory.getItem(0);
        if (input == null) {
            return;
        }

        ItemStack output = outputInventory.getItem(0);
        if (output == null || (output.isSimilar(input) && output.getAmount() < output.getMaxStackSize())) {
            if (output == null) {
                outputInventory.setItem(new MachineUpdateReason(), 0, input.asOne());
            } else {
                outputInventory.setItem(new MachineUpdateReason(), 0, output.add());
            }
            inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract());
            itemsRemaining = itemsRemaining == 0
                    ? threshold
                    : itemsRemaining - 1;
            doSplit();
        }
    }
}
