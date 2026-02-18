package io.github.pylonmc.pylon.content.machines.smelting;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.SmelteryRecipe;
import io.github.pylonmc.pylon.util.HslColor;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.transform.TransformUtil;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleRBTreeMap;
import kotlin.Pair;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class SmelteryController extends SmelteryComponent
        implements RebarGuiBlock, RebarMultiblock, RebarTickingBlock {

    private static final NamespacedKey RUNNING_KEY = pylonKey("running");
    private static final NamespacedKey TEMPERATURE_KEY = pylonKey("temperature");
    private static final NamespacedKey FLUIDS_KEY = pylonKey("fluids");

    private static final Config settings = Settings.get(PylonKeys.SMELTERY_CONTROLLER);
    public static final int TICK_INTERVAL = settings.getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public static final double FLUID_REACTION_PER_TICK = settings.getOrThrow("fluid-reaction-per-tick", ConfigAdapter.DOUBLE);
    public static final double HEATING_FACTOR = settings.getOrThrow("heating-factor", ConfigAdapter.DOUBLE);
    public static final double COOLING_FACTOR = settings.getOrThrow("cooling-factor", ConfigAdapter.DOUBLE);
    public static final double ROOM_TEMPERATURE = settings.getOrThrow("room-temperature", ConfigAdapter.DOUBLE);

    public final int maxHeight = settings.getOrThrow("max-height", ConfigAdapter.INTEGER);

    private final Set<SmelteryComponent> components = new HashSet<>();
    private final Object2DoubleMap<RebarFluid> fluids = new Object2DoubleRBTreeMap<>(
            Comparator.<RebarFluid, FluidTemperature>comparing(fluid -> fluid.getTag(FluidTemperature.class))
                    .reversed()
                    .thenComparing(fluid -> fluid.getKey().toString())
    );

    @Getter
    private boolean running;
    @Getter
    private double temperature;
    @Getter
    private double capacity;
    private int height;

    @SuppressWarnings("unused")
    public SmelteryController(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(TICK_INTERVAL);

        temperature = ROOM_TEMPERATURE;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public SmelteryController(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        running = pdc.get(RUNNING_KEY, RebarSerializers.BOOLEAN);
        temperature = pdc.get(TEMPERATURE_KEY, RebarSerializers.DOUBLE);
        fluids.putAll(pdc.get(FLUIDS_KEY, RebarSerializers.MAP.mapTypeFrom(RebarSerializers.REBAR_FLUID, RebarSerializers.DOUBLE)));
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        applyHeat();
        pdc.set(RUNNING_KEY, RebarSerializers.BOOLEAN, running);
        pdc.set(TEMPERATURE_KEY, RebarSerializers.DOUBLE, temperature);
        pdc.set(FLUIDS_KEY, RebarSerializers.MAP.mapTypeFrom(RebarSerializers.REBAR_FLUID, RebarSerializers.DOUBLE), fluids);
    }

    @Override
    public void postBreak(@NotNull BlockBreakContext context) {
        for (SmelteryComponent component : components) {
            component.setController(null);
        }
    }

    // <editor-fold desc="GUI" defaultstate="collapsed">
    private final Item infoItem = new InfoItem();
    private final Item contentsItem = new ContentsItem();

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # i # c # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('i', infoItem)
                .addIngredient('c', contentsItem)
                .addIngredient('#', GuiItems.background())
                .build();
    }

    private class InfoItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            Material material;
            List<Component> lore = new ArrayList<>();
            if (isFormedAndFullyLoaded()) {
                if (running) {
                    material = Material.GREEN_STAINED_GLASS_PANE;
                    lore.add(Component.translatable("pylon.gui.status.running"));
                } else {
                    material = Material.YELLOW_STAINED_GLASS_PANE;
                    lore.add(Component.translatable("pylon.gui.status.not_running"));
                }
                lore.add(Component.translatable("pylon.gui.status.toggle"));
                lore.add(Component.empty());
                lore.add(Component.translatable(
                        "pylon.gui.smeltery.height",
                        RebarArgument.of("height", UnitFormat.BLOCKS.format(height))
                ));
                lore.add(Component.translatable(
                        "pylon.gui.smeltery.capacity",
                        RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(capacity).decimalPlaces(0))
                ));
                lore.add(Component.translatable(
                        "pylon.gui.smeltery.temperature",
                        RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature).decimalPlaces(1))
                ));
            } else {
                material = Material.RED_STAINED_GLASS_PANE;
                lore.add(Component.translatable("pylon.gui.status.incomplete"));
            }
            return ItemStackBuilder.of(material)
                    .name(Component.translatable("pylon.gui.status.name"))
                    .lore(lore);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            if (isFormedAndFullyLoaded()) {
                setRunning(!running);
                notifyWindows();
            }
        }
    }

    private class ContentsItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            List<Component> lore = new ArrayList<>();
            if (fluids.isEmpty()) {
                lore.add(Component.translatable("pylon.gui.smeltery.contents.empty"));
            } else {
                for (Object2DoubleMap.Entry<RebarFluid> entry : fluids.object2DoubleEntrySet()) {
                    RebarFluid fluid = entry.getKey();
                    double amount = entry.getDoubleValue();
                    lore.add(Component.text().build().append(Component.translatable(
                            "pylon.gui.smeltery.contents.fluid",
                            RebarArgument.of(
                                    "amount",
                                    UnitFormat.MILLIBUCKETS.format(amount)
                                            .decimalPlaces(1)
                                            .unitStyle(Style.empty())
                            ),
                            RebarArgument.of("fluid", fluid.getName())
                    )));
                }
            }
            return ItemStackBuilder.of(Material.LAVA_BUCKET)
                    .name(Component.translatable("pylon.gui.smeltery.contents.name"))
                    .lore(lore);
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        }
    }

    // </editor-fold>

    // <editor-fold desc="Multiblock" defaultstate="collapsed">
    private final BlockPosition center = new BlockPosition(
            getBlock().getRelative(
                    ((Directional) getBlock().getBlockData()).getFacing().getOppositeFace(),
                    2
            )
    );

    // @formatter:off
    private static final Vector3i[] MULTIBLOCK_DIRECTIONS = new Vector3i[] {
            new Vector3i(-2, 0, -2), new Vector3i(-2, 0, -1), new Vector3i(-2, 0, 0), new Vector3i(-2, 0, 1), new Vector3i(-2, 0, 2),
            new Vector3i(-1, 0, -2),                                                                                                      new Vector3i(-1, 0, 2),
            new Vector3i(0, 0, -2),                                                                                                       new Vector3i(0, 0, 2),
            new Vector3i(1, 0, -2),                                                                                                       new Vector3i(1, 0, 2),
            new Vector3i(2, 0, -2),  new Vector3i(2, 0, -1),  new Vector3i(2, 0, 0),  new Vector3i(2, 0, 1),  new Vector3i(2, 0, 2),
    };
    private final List<BlockPosition> multiblockPositions = Arrays.stream(MULTIBLOCK_DIRECTIONS)
            .map(center::plus)
            .toList();

    private static final Vector3i[] INSIDE_DIRECTIONS = new Vector3i[] {
            new Vector3i(-1, 0, -1), new Vector3i(-1, 0, 0), new Vector3i(-1, 0, 1),
            new Vector3i(0, 0, -1),  new Vector3i(0, 0, 0),  new Vector3i(0, 0, 1),
            new Vector3i(1, 0, -1),  new Vector3i(1, 0, 0),  new Vector3i(1, 0, 1),
    };
    private final List<BlockPosition> insidePositions = Arrays.stream(INSIDE_DIRECTIONS)
            .map(center::plus)
            .toList();

    private static final Vector3i[] MULTIBLOCK_WITH_AIR = ArrayUtils.addAll(MULTIBLOCK_DIRECTIONS, INSIDE_DIRECTIONS);
    private final List<BlockPosition> multiblockWithAirPositions = Arrays.stream(MULTIBLOCK_WITH_AIR)
            .map(center::plus)
            .toList();
    // @formatter:on

    @Override
    public @NotNull Set<ChunkPosition> getChunksOccupied() {
        return multiblockPositions.stream()
                .map(BlockPosition::getChunk)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean checkFormed() {
        height = 0;

        for (SmelteryComponent component : components) {
            component.setController(null);
        }
        components.clear();

        // Check floor
        if (!checkAllComponent(addY(multiblockWithAirPositions, -1))) {
            return false;
        }

        // Check sides up to world height
        for (int i = 0; i < maxHeight; i++) {
            if (checkAllComponent(addY(multiblockPositions, i)) && checkAllAir(addY(insidePositions, i))) {
                if (++height >= maxHeight) {
                    break;
                }
            } else {
                break;
            }
        }

        return height > 0;
    }

    @Override
    public void onMultiblockFormed() {
        onMultiblockRefreshed();
    }

    @Override
    public void onMultiblockRefreshed() {
        capacity = height * insidePositions.size() * 1000;
        double totalFluid = getTotalFluid();
        if (totalFluid > capacity) {
            double ratio = capacity / totalFluid;
            for (RebarFluid fluid : fluids.keySet()) {
                fluids.computeDouble(fluid, (key, value) -> value * ratio);
            }
        }

        for (SmelteryComponent component : components) {
            component.setController(this);
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        setRunning(false);
        if (!partUnloaded) {
            height = 0;
            capacity = 0;
            temperature = ROOM_TEMPERATURE;
            fluids.clear();
            removePixels();
        }
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        if (otherBlock.getY() < center.getY() - 1) return false;
        return otherBlock.getX() >= center.getX() - 2 && otherBlock.getX() <= center.getX() + 2
                && otherBlock.getZ() >= center.getZ() - 2 && otherBlock.getZ() <= center.getZ() + 2;
    }

    private static List<BlockPosition> addY(@NotNull List<BlockPosition> positions, int y) {
        if (y == 0) return positions;
        return positions.stream()
                .map(pos -> pos.addScalar(0, y, 0))
                .toList();
    }

    private boolean checkAllComponent(@NotNull List<BlockPosition> positions) {
        Set<SmelteryComponent> components = new HashSet<>();
        for (BlockPosition pos : positions) {
            if (BlockStorage.get(pos) instanceof SmelteryComponent component) {
                components.add(component);
            } else {
                return false;
            }
        }
        this.components.addAll(components);
        return true;
    }

    private boolean checkAllAir(List<BlockPosition> blockPositions) {
        for (BlockPosition pos : blockPositions) {
            if (!pos.getBlock().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    // </editor-fold>

    // <editor-fold desc="Fluid" defaultstate="collapsed">
    public void addFluid(@NotNull RebarFluid fluid, double amount) {
        Preconditions.checkArgument(fluid.hasTag(FluidTemperature.class), "Fluid does not have a temperature tag");
        Preconditions.checkArgument(amount > 0, "Amount must be positive");

        double amountToAdd = Math.min(amount, capacity - getTotalFluid());
        fluids.mergeDouble(fluid, amountToAdd, Double::sum);
    }

    public void removeFluid(@NotNull RebarFluid fluid, double amount) {
        Preconditions.checkArgument(fluid.hasTag(FluidTemperature.class), "Fluid does not have a temperature tag");
        Preconditions.checkArgument(amount > 0, "Amount must be positive");

        fluids.mergeDouble(fluid, -amount, Double::sum);
        if (fluids.getDouble(fluid) <= 0.001) { // Consider anything less than a nanobucket as empty
            fluids.removeDouble(fluid);
        }
    }

    public double getFluidAmount(@NotNull RebarFluid fluid) {
        return fluids.getDouble(fluid);
    }

    public double getTotalFluid() {
        double sum = 0;
        for (double value : fluids.values()) {
            sum += value;
        }
        if (sum <= 0.001) return 0;
        return sum;
    }

    public @Nullable Pair<RebarFluid, Double> getBottomFluid() {
        Object2DoubleMap.Entry<RebarFluid> lastEntry = null;
        for (var entry : fluids.object2DoubleEntrySet()) {
            lastEntry = entry;
        }
        if (lastEntry == null) return null;
        return new Pair<>(lastEntry.getKey(), lastEntry.getDoubleValue());
    }
    // </editor-fold>

    // <editor-fold desc="Heating" defaultstate="collapsed">
    private int heaters = 0;
    private double avgTarget = -1;

    public void heatAsymptotically(double target) {
        if (avgTarget == -1) {
            avgTarget = target;
            heaters = 1;
        } else {
            avgTarget += (target - avgTarget) / ++heaters;
        }
    }

    private void applyHeat() {
        if (temperature < avgTarget) {
            temperature += (avgTarget - temperature) * HEATING_FACTOR;
        }
        avgTarget = -1;
        heaters = 0;
    }
    // </editor-fold>

    // <editor-fold desc="Fluid display" defaultstate="collapsed">
    private final List<TextDisplay> pixels = new ArrayList<>();
    private static final int RESOLUTION = Settings.get(PylonKeys.SMELTERY_CONTROLLER).getOrThrow("display.resolution", ConfigAdapter.INTEGER);
    private static final int PIXELS_PER_SIDE = 3 * RESOLUTION;

    private static final SimplexOctaveGenerator LAVA_NOISE = new SimplexOctaveGenerator(ThreadLocalRandom.current().nextLong(), 4);

    static {
        LAVA_NOISE.setScale(1 / 16.0);
    }

    private void spawnPixels() {
        pixels.clear();

        Location location = center.getLocation().add(-1, 0, -1);
        for (int x = 0; x < PIXELS_PER_SIDE; x++) {
            for (int z = 0; z < PIXELS_PER_SIDE; z++) {
                Location relative = location.clone().add((double) x / RESOLUTION, 0, (double) z / RESOLUTION);
                pixels.add(PylonUtils.spawnUnitSquareTextDisplay(relative, PylonUtils.METAL_GRAY, display -> {
                    display.setTransformationMatrix(
                            TransformUtil.transformationToMatrix(display.getTransformation())
                                    .translateLocal(0, -1, 0) // move the origin so it will be correct after rotation
                                    .rotateLocalX((float) Math.toRadians(-90))
                                    .scaleLocal(1f / RESOLUTION)
                    );
                    display.setBrightness(new Display.Brightness(15, 15));
                    display.setTeleportDuration(Math.min(59, TICK_INTERVAL));
                    display.setPersistent(false); // do not save to world
                }));
            }
        }
    }

    private @NotNull List<TextDisplay> getPixels() {
        if (pixels.isEmpty()) {
            spawnPixels();
        }
        return pixels;
    }

    private void removePixels() {
        for (TextDisplay pixel : pixels) {
            if (pixel.isValid()) {
                pixel.remove();
            }
        }
        pixels.clear();
    }

    private static final double LIGHTNESS_VARIATION = settings.getOrThrow("display.lightness-variation", ConfigAdapter.DOUBLE);
    private static final double LIGHTNESS_SPEED = settings.getOrThrow("display.lightness-speed", ConfigAdapter.DOUBLE);
    private double lastHeight = 0;

    private void updateFluidDisplay() {
        HslColor color = HslColor.fromRgb(PylonUtils.colorFromTemperature(temperature));
        double fill = getTotalFluid() / capacity;
        if (Double.isNaN(fill) || Double.isInfinite(fill)) {
            fill = 0;
        }
        if (fill <= 0 && lastHeight <= center.getY() - 0.01) {
            removePixels();
            return;
        }

        double finalHeight = center.getY() + height * fill - 0.01;
        boolean decreased = lastHeight > finalHeight;

        List<TextDisplay> pixels = getPixels();
        for (int i = 0; i < pixels.size(); i++) {
            TextDisplay entity = pixels.get(i);
            if (!entity.isValid()) continue;

            int x = i % PIXELS_PER_SIDE;
            int z = (i / PIXELS_PER_SIDE) % PIXELS_PER_SIDE;
            double value = LAVA_NOISE.noise(
                    x,
                    z,
                    (System.currentTimeMillis() / 1000.0) * LIGHTNESS_SPEED,
                    0.01,
                    0.01,
                    true
            );
            HslColor newColor = new HslColor(
                    color.hue(),
                    color.saturation(),
                    color.lightness() + value * LIGHTNESS_VARIATION
            );
            entity.setBackgroundColor(newColor.toRgb());


            if (lastHeight != finalHeight) {
                Location location = entity.getLocation();
                location.setY(finalHeight);
                if (decreased) {
                    entity.setTeleportDuration(0);
                }
                entity.teleportAsync(location).whenComplete((b, t) -> {
                    if (decreased) {
                        entity.setTeleportDuration(Math.min(59, TICK_INTERVAL));
                    }
                });
            }
        }
        lastHeight = finalHeight;
    }
    // </editor-fold>

    private void performRecipes() {
        if (fluids.isEmpty()) return;
        recipeLoop:
        for (SmelteryRecipe recipe : SmelteryRecipe.RECIPE_TYPE) {
            if (recipe.getTemperature() > temperature) continue;

            for (RebarFluid fluid : recipe.getFluidInputs().keySet()) {
                if (getFluidAmount(fluid) == 0) continue recipeLoop;
            }

            double highestFluidAmount = getFluidAmount(recipe.getHighestFluid());
            double consumptionRatio = highestFluidAmount / FLUID_REACTION_PER_TICK;
            double currentTemperature = temperature;
            for (var entry : recipe.getFluidInputs().entrySet()) {
                RebarFluid fluid = entry.getKey();
                double amount = entry.getValue() * consumptionRatio;
                removeFluid(fluid, amount);
            }
            for (var entry : recipe.getFluidOutputs().entrySet()) {
                RebarFluid fluid = entry.getKey();
                double amount = entry.getValue() * consumptionRatio;
                addFluid(fluid, amount);
            }
            temperature = currentTemperature; // offset addFluid/removeFluid temperature change
        }
    }

    @Override
    public void tick() {
        if (isFormedAndFullyLoaded()) {
            if (running) {
                applyHeat();
                performRecipes();
            }
            temperature -= (temperature - ROOM_TEMPERATURE) * COOLING_FACTOR;
            updateFluidDisplay();
        }
        infoItem.notifyWindows();
        contentsItem.notifyWindows();
    }

    public void setRunning(boolean running) {
        this.running = running;
        refreshBlockTextureItem();
    }

    @Override
    public @NotNull Map<String, Pair<String, Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("running", new Pair<>(String.valueOf(isFormedAndFullyLoaded() && running), 2));
        return properties;
    }
}
