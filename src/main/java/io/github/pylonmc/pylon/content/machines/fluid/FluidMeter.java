package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class FluidMeter extends RebarBlock implements
        RebarFluidTank,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarGuiBlock {

    public static final NamespacedKey MEASUREMENTS_KEY = PylonUtils.pylonKey("measurements");
    public static final NamespacedKey NUMBER_OF_MEASUREMENTS_KEY = PylonUtils.pylonKey("number_of_measurements");

    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
    public final int minNumberOfMeasurements = getSettings().getOrThrow("min-number-of-measurements", ConfigAdapter.INTEGER);
    public final int maxNumberOfMeasurements = getSettings().getOrThrow("max-number-of-measurements", ConfigAdapter.INTEGER);

    private double fluidAddedLastUpdate;
    private final List<Double> measurements;
    private int numberOfMeasurements;

    public static class Item extends RebarItem {

        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
        public final int minNumberOfMeasurements = getSettings().getOrThrow("min-number-of-measurements", ConfigAdapter.INTEGER);
        public final int maxNumberOfMeasurements = getSettings().getOrThrow("max-number-of-measurements", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer)),
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

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.WHITE_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder topStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":top");
    public final ItemStackBuilder projectorStack = ItemStackBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS)
            .addCustomModelDataString(getKey() + ":projector");

    @SuppressWarnings("unused")
    public FluidMeter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        setCapacity(buffer);
        setTickInterval(RebarConfig.FLUID_TICK_INTERVAL);

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.25F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.25F);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.25, 0.25, 0.5)
                )
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("top", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.2, 0.3, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("projector", new ItemDisplayBuilder()
                .itemStack(projectorStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.125, 0)
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.12, 0.12, 0.12)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("flow_rate", new TextDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.35, 0.0))
                        .scale(0.3, 0.3, 0.3)
                )
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .text(UnitFormat.MILLIBUCKETS_PER_SECOND.format(0).asComponent())
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("fluid", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.3, 0.0))
                        .scale(0.07, 0.07, 0.07)
                )
                .itemStack(new ItemStack(Material.BARRIER))
                .billboard(Display.Billboard.VERTICAL)
                .build(block.getLocation().toCenterLocation())
        );

        measurements = new ArrayList<>();
        numberOfMeasurements = minNumberOfMeasurements;
    }

    @SuppressWarnings("unused")
    public FluidMeter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        measurements = new ArrayList<>(pdc.get(MEASUREMENTS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE)));
        numberOfMeasurements = pdc.get(NUMBER_OF_MEASUREMENTS_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(MEASUREMENTS_KEY, RebarSerializers.LIST.listTypeFrom(RebarSerializers.DOUBLE), measurements);
        pdc.set(NUMBER_OF_MEASUREMENTS_KEY, RebarSerializers.INTEGER, numberOfMeasurements);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # m # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', new MeasurementDurationItem())
                .build();
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        RebarFluidTank.super.onFluidAdded(fluid, amount);
        fluidAddedLastUpdate = amount;
        getHeldEntityOrThrow(ItemDisplay.class, "fluid").setItemStack(fluid.getItem());
    }

    @Override
    public void tick() {
        measurements.add(fluidAddedLastUpdate);
        fluidAddedLastUpdate = 0.0;
        if (measurements.size() > numberOfMeasurements) {
            for (int i = 0; i < measurements.size() - numberOfMeasurements; i++) {
                measurements.removeFirst();
            }
        }
        double total = measurements.stream()
                .mapToDouble(x -> x)
                .sum();
        double average = (total / measurements.size()) * 20.0 / getTickInterval();
        Component component = UnitFormat.MILLIBUCKETS_PER_SECOND.format(average).decimalPlaces(0).asComponent();
        getHeldEntityOrThrow(TextDisplay.class, "flow_rate").text(component);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                        getFluidAmount(),
                        getFluidCapacity(),
                        20,
                        TextColor.color(200, 255, 255)
                )),
                RebarArgument.of("fluid", getFluidType() == null
                        ? Component.translatable("pylon.fluid.none")
                        : getFluidType().getName()
                ),
                RebarArgument.of("duration", UnitFormat.formatDuration(getDuration(numberOfMeasurements), true, true))
        ));
    }

    public static Duration getDuration(int numberOfMeasurements) {
        return Duration.ofMillis((long) numberOfMeasurements * RebarConfig.FLUID_TICK_INTERVAL * 50);
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
