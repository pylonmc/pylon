package io.github.pylonmc.pylon.content.machines.cargo;

import com.google.common.base.Preconditions;
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
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CargoAccumulator extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock {

    public static final NamespacedKey THRESHOLD_KEY = pylonKey("threshold");

    private final VirtualInventory inputInventory = new VirtualInventory(5);
    private final VirtualInventory outputInventory = new VirtualInventory(5);

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public int threshold;

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");
    public final ItemStackBuilder thresholdButtonStack = ItemStackBuilder.gui(Material.WHITE_CONCRETE, getKey() + "threshold_button")
            .lore(Component.translatable("pylon.gui.threshold_button.lore"));

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
    public CargoAccumulator(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing(), "input");
        addCargoLogisticGroup(getFacing().getOppositeFace(), "output");
        setCargoTransferRate(transferRate);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.6)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("side1", new BlockDisplayBuilder()
                .blockData(Material.REDSTONE_LAMP.createBlockData("[lit=false]"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.45, 0.45, 0.65)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("side2", new BlockDisplayBuilder()
                .blockData(Material.REDSTONE_LAMP.createBlockData("[lit=false]"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.65, 0.45, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("input", new ItemDisplayBuilder()
                .itemStack(inputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, 0.125)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output", new ItemDisplayBuilder()
                .itemStack(outputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.125)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        threshold = 1;
    }

    @SuppressWarnings("unused")
    public CargoAccumulator(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        threshold = pdc.get(THRESHOLD_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(THRESHOLD_KEY, RebarSerializers.INTEGER, threshold);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# I i i i i i I #",
                        "# # # # # # # # #",
                        "# O o o o o o O #",
                        "# # # # # # # # #",
                        "# # # # t # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', inputInventory)
                .addIngredient('I', GuiItems.input())
                .addIngredient('o', outputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('t', new ThresholdButton())
                .build();
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doTransfer();
            }
        });
        outputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                doTransfer();
            }
        });
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("threshold", threshold)
        ));
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("input", inputInventory, "output", outputInventory);
    }

    private void doTransfer() {
        int inputTotal = 0;
        for (ItemStack stack : inputInventory.getItems()) {
            if (stack != null) {
                inputTotal += stack.getAmount();
            }
        }

        int outputTotal = 0;
        for (ItemStack stack : outputInventory.getItems()) {
            if (stack != null) {
                outputTotal += stack.getAmount();
            }
        }

        if (outputTotal == 0) {
            getLogisticGroupOrThrow("input").setFilter(null);
            getHeldEntityOrThrow(BlockDisplay.class, "side1")
                    .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=false]"));
            getHeldEntityOrThrow(BlockDisplay.class, "side2")
                    .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=false]"));
        }

        if (inputTotal >= threshold) {
            List<ItemStack> stacks = Arrays.stream(inputInventory.getItems()).toList();
            Preconditions.checkState(outputInventory.canHold(stacks));
            for (ItemStack stack : stacks) {
                outputInventory.addItem(new MachineUpdateReason(), stack);
            }
            for (int slot = 0; slot < inputInventory.getItems().length; slot++) {
                inputInventory.setItem(new MachineUpdateReason(), slot, null);
            }
            getLogisticGroupOrThrow("input").setFilter(stack -> false);
            getHeldEntityOrThrow(BlockDisplay.class, "side1")
                    .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=true]"));
            getHeldEntityOrThrow(BlockDisplay.class, "side2")
                    .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=true]"));
        }
    }

    public class ThresholdButton extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
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
            notifyWindows();
            doTransfer();
        }
    }
}
