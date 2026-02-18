package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.VirtualInventoryLogisticSlot;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CargoMeter extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock,
        RebarTickingBlock {

    public static final NamespacedKey MEASUREMENTS_KEY = PylonUtils.pylonKey("measurements");
    public static final NamespacedKey NUMBER_OF_MEASUREMENTS_KEY = PylonUtils.pylonKey("number_of_measurements");

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);
    public final int minNumberOfMeasurements = getSettings().getOrThrow("min-number-of-measurements", ConfigAdapter.INTEGER);
    public final int maxNumberOfMeasurements = getSettings().getOrThrow("max-number-of-measurements", ConfigAdapter.INTEGER);

    private int itemsAddedLastUpdate;
    private final List<Integer> measurements;
    private int numberOfMeasurements;

    private final VirtualInventory inventory = new VirtualInventory(1);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder side1Stack = ItemStackBuilder.of(Material.BARREL)
            .addCustomModelDataString(getKey() + ":side1");
    public final ItemStackBuilder side2Stack = ItemStackBuilder.of(Material.BARREL)
            .addCustomModelDataString(getKey() + ":side2");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");
    public final ItemStackBuilder projectorStack = ItemStackBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS)
            .addCustomModelDataString(getKey() + ":projector");

    public static class Item extends RebarItem {

        public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);
        public final int minNumberOfMeasurements = getSettings().getOrThrow("min-number-of-measurements", ConfigAdapter.INTEGER);
        public final int maxNumberOfMeasurements = getSettings().getOrThrow("max-number-of-measurements", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of(
                            "transfer-rate",
                            UnitFormat.ITEMS_PER_SECOND.format(RebarCargoBlock.cargoItemsTransferredPerSecond(transferRate))
                    ),
                    RebarArgument.of(
                            "min-measurement-time",
                            UnitFormat.formatDuration(getDuration(minNumberOfMeasurements), true, true)
                    ),
                    RebarArgument.of(
                            "max-measurement-time",
                            UnitFormat.formatDuration(getDuration(maxNumberOfMeasurements), true, true)
                    )
            );
        }
    }

    @SuppressWarnings("unused")
    public CargoMeter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        setTickInterval(RebarConfig.CARGO_TICK_INTERVAL);

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

        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(side1Stack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.45, 0.45, 0.65)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(side2Stack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.65, 0.45, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("projector", new ItemDisplayBuilder()
                .itemStack(projectorStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.25, 0)
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.3, 0.3, 0.3)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("flow_rate", new TextDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.62, 0.0))
                        .scale(0.6, 0.6, 0.6)
                )
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .text(UnitFormat.ITEMS_PER_SECOND.format(0).asComponent())
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.53, 0.0))
                        .scale(0.15, 0.15, 0.15)
                )
                .itemStack(new ItemStack(Material.BARRIER))
                .billboard(Display.Billboard.VERTICAL)
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

        measurements = new ArrayList<>();
        numberOfMeasurements = minNumberOfMeasurements;
    }

    @SuppressWarnings("unused")
    public CargoMeter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        measurements = new ArrayList<>(pdc.get(MEASUREMENTS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.INTEGER)));
        numberOfMeasurements = pdc.get(NUMBER_OF_MEASUREMENTS_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(MEASUREMENTS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.INTEGER), measurements);
        pdc.set(NUMBER_OF_MEASUREMENTS_KEY, RebarSerializers.INTEGER, numberOfMeasurements);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("input", LogisticGroupType.INPUT, new VirtualInventoryLogisticSlot(inventory, 0));
        createLogisticGroup("output", LogisticGroupType.OUTPUT, new VirtualInventoryLogisticSlot(inventory, 0));
        inventory.addPostUpdateHandler(event -> {
            ItemStack newStack = event.getNewItem();
            getHeldEntityOrThrow(ItemDisplay.class, "item")
                    .setItemStack(newStack == null ? new ItemStack(Material.BARRIER) : newStack);
            if (event.isAdd()) {
                itemsAddedLastUpdate += event.getAddedAmount();
            }
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # x # # # #",
                        "# # # # m # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', inventory)
                .addIngredient('m', new MeasurementDurationItem())
                .build();
    }

    @Override
    public void tick() {
        measurements.add(itemsAddedLastUpdate);
        itemsAddedLastUpdate = 0;
        if (measurements.size() > numberOfMeasurements) {
            for (int i = 0; i < measurements.size() - numberOfMeasurements; i++) {
                measurements.removeFirst();
            }
        }
        double total = measurements.stream()
                .mapToDouble(x -> x)
                .sum();
        double average = (total / measurements.size()) * 20.0 / getTickInterval();
        Component component = UnitFormat.ITEMS_PER_SECOND.format(average).decimalPlaces(2).asComponent();
        getHeldEntityOrThrow(TextDisplay.class, "flow_rate").text(component);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("duration", UnitFormat.formatDuration(getDuration(numberOfMeasurements), true, true))
        ));
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    public static Duration getDuration(int numberOfMeasurements) {
        return Duration.ofMillis((long) numberOfMeasurements * RebarConfig.CARGO_TICK_INTERVAL * 50);
    }

    public class MeasurementDurationItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            return ItemStackBuilder.of(Material.WHITE_CONCRETE)
                    .name(Component.translatable("pylon.gui.fluid_meter.name").arguments(
                            RebarArgument.of("measurement-duration", UnitFormat.formatDuration(getDuration(numberOfMeasurements), true, true))
                    ))
                    .lore(Component.translatable("pylon.gui.fluid_meter.lore"));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            int newValue;
            if (clickType.isLeftClick()) {
                newValue = numberOfMeasurements + (clickType.isShiftClick() ? 10 : 1);
            } else if (clickType.isRightClick()) {
                newValue = numberOfMeasurements + (clickType.isShiftClick() ? -10 : -1);
            } else {
                newValue = numberOfMeasurements;
            }
            numberOfMeasurements = Math.clamp(newValue, minNumberOfMeasurements, maxNumberOfMeasurements);
            notifyWindows();
        }
    }
}
