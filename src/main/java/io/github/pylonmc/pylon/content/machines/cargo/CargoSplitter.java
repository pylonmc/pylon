package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
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
import xyz.xenondevs.invui.item.AbstractBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CargoSplitter extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock {

    public static final NamespacedKey RATIO_LEFT_KEY = pylonKey("ratio_left");
    public static final NamespacedKey RATIO_RIGHT_KEY = pylonKey("ratio_right");
    public static final NamespacedKey IS_LEFT_KEY = pylonKey("is_left");
    public static final NamespacedKey ITEMS_REMAINING_KEY = pylonKey("items_remaining");

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public int ratioLeft = 1;
    public int ratioRight = 1;
    public boolean isLeft = true;
    public int itemsRemaining = 1;

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory leftInventory = new VirtualInventory(1);
    private final VirtualInventory rightInventory = new VirtualInventory(1);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder verticalStack = ItemStackBuilder.of(Material.STRIPPED_CRIMSON_STEM)
            .addCustomModelDataString(getKey() + ":vertical");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder outputLeftStack = ItemStackBuilder.of(Material.YELLOW_CONCRETE)
            .addCustomModelDataString(getKey() + ":output_left");
    public final ItemStackBuilder outputRightStack = ItemStackBuilder.of(Material.LIGHT_BLUE_CONCRETE)
            .addCustomModelDataString(getKey() + ":output_right");

    public final ItemStackBuilder leftStack = ItemStackBuilder.gui(Material.YELLOW_STAINED_GLASS_PANE, getKey() + "left")
            .name(Component.translatable("pylon.gui.left"));
    public final ItemStackBuilder rightStack = ItemStackBuilder.gui(Material.LIGHT_BLUE_STAINED_GLASS_PANE, getKey() + "right")
            .name(Component.translatable("pylon.gui.right"));
    public final ItemStackBuilder ratioStack = ItemStackBuilder.gui(Material.WHITE_CONCRETE, getKey() + "ratio");
    public final ItemStackBuilder leftButtonStack = ItemStackBuilder.gui(Material.YELLOW_STAINED_GLASS_PANE, getKey() + "left_button")
            .name(Component.translatable("pylon.gui.ratio.left_button.name"))
            .lore(Component.translatable("pylon.gui.ratio.left_button.lore"));
    public final ItemStackBuilder rightButtonStack = ItemStackBuilder.gui(Material.LIGHT_BLUE_STAINED_GLASS_PANE, getKey() + "right_button")
            .name(Component.translatable("pylon.gui.ratio.right_button.name"))
            .lore(Component.translatable("pylon.gui.ratio.right_button.lore"));

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("input", inputInventory, "left", leftInventory, "right", rightInventory);
    }

    public static class RatioButton extends AbstractBoundItem {

        private final ItemStackBuilder stack;
        private final Supplier<Integer> getRatio;
        private final Consumer<Integer> setRatio;

        public RatioButton(ItemStackBuilder stack, Supplier<Integer> getRatio, Consumer<Integer> setRatio) {
            this.stack = stack;
            this.getRatio = getRatio;
            this.setRatio = setRatio;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            if (clickType.isLeftClick()) {
                setRatio.accept(getRatio.get() + 1);
            } else {
                setRatio.accept(Math.max(1, getRatio.get() - 1));
            }
            getGui().notifyWindows('a');
        }

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return stack;
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
    public CargoSplitter(@NotNull Block block, @NotNull BlockCreateContext context) {
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

        addEntity("vertical", new ItemDisplayBuilder()
                .itemStack(verticalStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.45, 0.75, 0.45)
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
    public CargoSplitter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        ratioLeft = pdc.get(RATIO_LEFT_KEY, RebarSerializers.INTEGER);
        ratioRight = pdc.get(RATIO_RIGHT_KEY, RebarSerializers.INTEGER);
        isLeft = pdc.get(IS_LEFT_KEY, RebarSerializers.BOOLEAN);
        itemsRemaining = pdc.get(ITEMS_REMAINING_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(RATIO_LEFT_KEY, RebarSerializers.INTEGER, ratioLeft);
        pdc.set(RATIO_RIGHT_KEY, RebarSerializers.INTEGER, ratioRight);
        pdc.set(IS_LEFT_KEY, RebarSerializers.BOOLEAN, isLeft);
        pdc.set(ITEMS_REMAINING_KEY, RebarSerializers.INTEGER, itemsRemaining);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# L # # I # # R #",
                        "# l # # i # # r #",
                        "# L # # I # # R #",
                        "# # # # # # # # #",
                        "# # # < a > # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('L', leftStack)
                .addIngredient('l', leftInventory)
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('R', rightStack)
                .addIngredient('r', rightInventory)
                .addIngredient('a', xyz.xenondevs.invui.item.Item.simple(p -> ratioStack.clone()
                        .name(Component.translatable("pylon.gui.ratio.name").arguments(
                                RebarArgument.of("left", ratioLeft),
                                RebarArgument.of("right", ratioRight)
                        ))))
                .addIngredient('<', new RatioButton(leftButtonStack, () -> ratioLeft, amount -> ratioLeft = amount))
                .addIngredient('>', new RatioButton(rightButtonStack, () -> ratioRight, amount -> ratioRight = amount))
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
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("left", ratioLeft),
                RebarArgument.of("right", ratioRight),
                RebarArgument.of("side", isLeft
                        ? Component.translatable("pylon.waila.cargo_splitter.left")
                        : Component.translatable("pylon.waila.cargo_splitter.right")
                )
        ));
    }

    private void doSplit() {
        ItemStack input = inputInventory.getItem(0);
        if (input == null) {
            return;
        }

        if (isLeft) {
            ItemStack left = leftInventory.getItem(0);
            if (left == null || (left.isSimilar(input) && left.getAmount() < left.getMaxStackSize())) {
                if (left == null) {
                    leftInventory.setItem(new MachineUpdateReason(), 0, input.asOne());
                } else {
                    leftInventory.setItem(new MachineUpdateReason(), 0, left.add());
                }
                inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract());
                itemsRemaining--;
                if (itemsRemaining == 0) {
                    isLeft = !isLeft;
                    itemsRemaining = ratioRight;
                }
                doSplit();
            }
        } else {
            ItemStack right = rightInventory.getItem(0);
            if (right == null || (right.isSimilar(input) && right.getAmount() < right.getMaxStackSize())) {
                if (right == null) {
                    rightInventory.setItem(new MachineUpdateReason(), 0, input.asOne());
                } else {
                    rightInventory.setItem(new MachineUpdateReason(), 0, right.add());
                }
                inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract());
                itemsRemaining--;
                if (itemsRemaining == 0) {
                    isLeft = !isLeft;
                    itemsRemaining = ratioLeft;
                }
                doSplit();
            }
        }
    }
}
