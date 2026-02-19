package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.pylon.PylonItems;
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
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CargoOverflowGate extends RebarBlock
        implements RebarDirectionalBlock, RebarGuiBlock, RebarCargoBlock, RebarVirtualInventoryBlock {

    private static final NamespacedKey SIDE_PRIORITY_KEY = pylonKey("side_priority");
    private static final NamespacedKey IS_LEFT_KEY = pylonKey("is_left");


    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory leftInventory = new VirtualInventory(1);
    private final VirtualInventory rightInventory = new VirtualInventory(1);

    @Getter
    private @NotNull SidePriority sidePriority = SidePriority.NONE;

    private boolean isLeft = false;

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder verticalStack = ItemStackBuilder.of(Material.CRIMSON_STEM)
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

    public enum SidePriority {
        NONE("none"),
        LEFT("left"),
        RIGHT("right");

        @Getter
        private final @NotNull ItemStackBuilder priorityStack;

        SidePriority(String name) {
            priorityStack = ItemStackBuilder.gui(Material.WHITE_CONCRETE, PylonItems.CARGO_OVERFLOW_GATE + ":priority:" + name)
                    .name(Component.translatable("pylon.gui.side-priority.name", RebarArgument.of("priority", Component.translatable("pylon.gui." + name))))
                    .lore(Component.translatable("pylon.gui.side-priority.lore"));
        }

        public static final PersistentDataType<?, SidePriority> PERSISTENT_DATA_TYPE = RebarSerializers.ENUM.enumTypeFrom(SidePriority.class);
    }

    @SuppressWarnings("unused")
    public CargoOverflowGate(@NotNull Block block, @NotNull BlockCreateContext context) {
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

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public CargoOverflowGate(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        sidePriority = pdc.get(SIDE_PRIORITY_KEY, SidePriority.PERSISTENT_DATA_TYPE);
        isLeft = pdc.get(IS_LEFT_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(SIDE_PRIORITY_KEY, SidePriority.PERSISTENT_DATA_TYPE, sidePriority);
        pdc.set(IS_LEFT_KEY, RebarSerializers.BOOLEAN, isLeft);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "left", leftInventory,
                "right", rightInventory
        );
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# L # # I # # R #",
                        "# l # # i # # r #",
                        "# L # # I # # R #",
                        "# # # # # # # # #",
                        "# # # # p # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('L', leftStack)
                .addIngredient('l', leftInventory)
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('R', rightStack)
                .addIngredient('r', rightInventory)
                .addIngredient('p', xyz.xenondevs.invui.item.Item.builder()
                        .setItemProvider(unused -> sidePriority.getPriorityStack())
                        .addClickHandler((item, click) -> {
                            sidePriority = switch (sidePriority) {
                                case NONE -> SidePriority.LEFT;
                                case LEFT -> SidePriority.RIGHT;
                                case RIGHT -> SidePriority.NONE;
                            };
                            item.notifyWindows();
                        })
                )
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

    private void doSplit() {
        ItemStack input = inputInventory.getItem(0);
        if (input == null) return;

        isLeft = switch (sidePriority) {
            case LEFT -> true;
            case RIGHT -> false;
            case NONE -> !isLeft;
        };

        transferToSide(input, true);
    }

    private void transferToSide(ItemStack input, boolean tryOther) {
        VirtualInventory targetInventory = isLeft ? leftInventory : rightInventory;
        ItemStack existing = targetInventory.getItem(0);
        if (existing == null || (existing.isSimilar(input) && existing.getAmount() < existing.getMaxStackSize())) {
            if (existing == null) {
                targetInventory.setItem(new MachineUpdateReason(), 0, input.asOne());
            } else {
                targetInventory.setItem(new MachineUpdateReason(), 0, existing.add());
            }
            inputInventory.setItem(new MachineUpdateReason(), 0, input.subtract());
            doSplit();
        } else if (tryOther) {
            // Try other side
            isLeft = !isLeft;
            transferToSide(input, false);
        }
    }
}
