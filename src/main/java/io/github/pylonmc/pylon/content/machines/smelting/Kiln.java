package io.github.pylonmc.pylon.content.machines.smelting;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.recipes.KilnRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarRecipeProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class Kiln extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarGuiBlock,
        RebarRecipeProcessor<KilnRecipe>,
        RebarDirectionalBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock {

    public static final NamespacedKey TEMPERATURE_KEY = pylonKey("temperature");
    public static final NamespacedKey FUEL_TICKS_TOTAL_KEY = pylonKey("fuel_ticks_total");
    public static final NamespacedKey FUEL_TICKS_REMAINING_KEY = pylonKey("fuel_ticks_remaining");

    private static final Vector3i ITEM_INPUT_HATCH_1 = new Vector3i(1, 0, 1);
    private static final Vector3i ITEM_INPUT_HATCH_2 = new Vector3i(1, 1, 1);
    private static final Vector3i ITEM_OUTPUT_HATCH = new Vector3i(-1, 0, 1);
    private static final Vector3i FLUID_OUTPUT_HATCH = new Vector3i(-1, 1, 1);
    private static final Vector3i LIGHT = new Vector3i(0, 1, 1);

    private static final Random RANDOM = new Random();

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double minTemperature = getSettings().getOrThrow("min-temperature", ConfigAdapter.DOUBLE);
    public final double maxTemperature = getSettings().getOrThrow("max-temperature", ConfigAdapter.DOUBLE);
    public final double heatingRate = getSettings().getOrThrow("heating-rate", ConfigAdapter.DOUBLE);

    private final VirtualInventory fuelInventory = new VirtualInventory(1);

    private final ProgressItem fuelProgressItem = new ProgressItem(GuiItems.background());

    public final ItemStackBuilder burningStack = ItemStackBuilder.gui(Material.FLINT_AND_STEEL, getKey() + "fuel-left")
            .name(Component.translatable("pylon.gui.fuel-left"));
    public final ItemStackBuilder fuelStack = ItemStackBuilder.gui(Material.BLACK_STAINED_GLASS_PANE, getKey() + "fuel")
            .name(Component.translatable("pylon.gui.fuel"));
    public final ItemStackBuilder temperatureStack = ItemStackBuilder.gui(Material.REDSTONE, getKey() + "temperature")
            .name(Component.translatable("pylon.gui.temperature"));

    public final AbstractItem temperatureItem = new TemperatureItem();

    private int fuelTicksTotal;
    private int fuelTicksRemaining;
    public double temperature;

    @SuppressWarnings("unused")
    public Kiln(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);
        setRecipeType(KilnRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background(), false));
        temperature = minTemperature;
    }

    @SuppressWarnings("unused")
    public Kiln(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        temperature = pdc.get(TEMPERATURE_KEY, RebarSerializers.DOUBLE);
        fuelTicksTotal = pdc.get(FUEL_TICKS_TOTAL_KEY, RebarSerializers.INTEGER);
        fuelTicksRemaining = pdc.get(FUEL_TICKS_REMAINING_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(TEMPERATURE_KEY, RebarSerializers.DOUBLE, temperature);
        pdc.set(FUEL_TICKS_TOTAL_KEY, RebarSerializers.INTEGER, fuelTicksTotal);
        pdc.set(FUEL_TICKS_REMAINING_KEY, RebarSerializers.INTEGER, fuelTicksRemaining);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 0, 1), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(new Vector3i(0, 0, 2), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(ITEM_INPUT_HATCH_1, new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(ITEM_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.ITEM_OUTPUT_HATCH));
        components.put(new Vector3i(1, 0, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 0, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(1, 0, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 0, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(ITEM_INPUT_HATCH_2, new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(FLUID_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(new Vector3i(0, 1, 2), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(new Vector3i(1, 1, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 1, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(1, 1, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 1, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));

        components.put(new Vector3i(0, 2, 0), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(new Vector3i(1, 2, 1), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(new Vector3i(0, 2, 2), new VanillaMultiblockComponent(Material.MUD_BRICKS));
        components.put(new Vector3i(1, 2, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 2, 0), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(1, 2, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));
        components.put(new Vector3i(-1, 2, 2), new VanillaMultiblockComponent(Material.MUD_BRICK_WALL));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        // Fuel stuff
        tryConsumeFuel();
        if (fuelTicksRemaining >= 0) {
            fuelTicksRemaining -= getTickInterval();
        }
        tryConsumeFuel();

        if (fuelTicksRemaining >= 0) {
            fuelProgressItem.setTotalTimeTicks(fuelTicksTotal);
            fuelProgressItem.setRemainingTimeTicks(fuelTicksRemaining);
            fuelProgressItem.setItem(burningStack);
        } else {
            fuelProgressItem.setTotalTimeTicks(null);
            fuelProgressItem.setItem(GuiItems.background());
        }

        // Temperature stuff
        if (fuelTicksRemaining > 0) {
            temperature += heatingRate / getTickInterval();
        }
        temperature -= (heatingRate / (maxTemperature - minTemperature)) * (temperature - minTemperature) / getTickInterval();
        temperatureItem.notifyWindows();

        // Visual stuff
        Furnace furnace = (Furnace) getBlock().getBlockData();
        furnace.setLit(fuelTicksRemaining > 0);
        getBlock().setBlockData(furnace);

        int level = Math.clamp((int) Math.round(15 * temperature / maxTemperature), 0, 15);
        Block light = getLight();
        if (light.getType() == Material.LIGHT) {
            Light blockData = (Light) light.getBlockData();
            blockData.setLevel(level);
            light.setBlockData(blockData);
        }

        for (int i = 0; i < level; i++) {
            double x = RANDOM.nextDouble(-0.4, 0.4);
            double y = 1.0 + RANDOM.nextDouble(-0.4, 0.4);
            double z = RANDOM.nextDouble(-0.4, 0.4);
            new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                    .location(getBlock().getLocation().toCenterLocation().add(getFacing().getOppositeFace().getDirection()).add(x, y, z))
                    .offset(0.0, 1.0, 0.0)
                    .extra(0.03)
                    .count(0)
                    .spawn();
        }

        // Recipe stuff
        KilnRecipe recipe = getCurrentRecipe();
        if (recipe == null) {
            startRecipe();
        }
        if (recipe != null) {
            boolean canHoldOutputItem = getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUT_HATCH)
                    .inventory.canHold(recipe.outputItem());
            if (canHoldOutputItem && temperature > recipe.temperature()) {
                progressRecipe(getTickInterval());
            }
        }
    }

    public void tryConsumeFuel() {
        if (fuelTicksRemaining > 0) {
            return;
        }

        ItemStack fuel = fuelInventory.getItem(0);
        if (fuel == null) {
            return;
        }

        // dividing by 10 due to suspected bug with getBurnDuration
        fuelTicksTotal = fuel.getType().asItemType().getBurnDuration() / 10;
        fuelTicksRemaining = fuelTicksTotal;
        fuelInventory.setItem(new MachineUpdateReason(), 0, fuel.subtract());
    }

    public void startRecipe() {
        for (KilnRecipe recipe : KilnRecipe.RECIPE_TYPE) {
            if (temperature < recipe.temperature()) {
                continue;
            }

            if (recipe.outputItem() != null) {
                boolean canHoldOutputItem = getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUT_HATCH)
                        .inventory.canHold(recipe.outputItem());
                if (!canHoldOutputItem) {
                    continue;
                }
            }

            if (recipe.outputFluid() != null && recipe.outputFluidAmount() != null) {
                FluidOutputHatch fluidOutputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT_HATCH);
                boolean canHoldFluidOutput = fluidOutputHatch.fluid == null
                        || fluidOutputHatch.fluidAmount(fluidOutputHatch.fluid) < 1.0e-6
                        || fluidOutputHatch.fluid.equals(recipe.outputFluid()) && fluidOutputHatch.fluidSpaceRemaining(fluidOutputHatch.fluid) > recipe.outputFluidAmount();
                if (!canHoldFluidOutput) {
                    continue;
                }
            }

            ItemInputHatch itemInputHatch1 = getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUT_HATCH_1);
            ItemInputHatch itemInputHatch2 = getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUT_HATCH_2);
            ItemStack input1 = itemInputHatch1.inventory.getItem(0);
            ItemStack input2 = itemInputHatch2.inventory.getItem(0);

            boolean matches = false;
            if (recipe.input2() == null) {
                if (recipe.input1().matches(input1)) {
                    itemInputHatch1.inventory.setItem(new MachineUpdateReason(), 0, input1.subtract(recipe.input1().getAmount()));
                    matches = true;
                } else if (recipe.input1().matches(input2)) {
                    itemInputHatch2.inventory.setItem(new MachineUpdateReason(), 0, input2.subtract(recipe.input1().getAmount()));
                    matches = true;
                }
            } else {
                if (recipe.input1().matches(input1) && recipe.input2().matches(input2)) {
                    itemInputHatch1.inventory.setItem(new MachineUpdateReason(), 0, input1.subtract(recipe.input1().getAmount()));
                    itemInputHatch2.inventory.setItem(new MachineUpdateReason(), 0, input2.subtract(recipe.input2().getAmount()));
                    matches = true;
                }
                if (recipe.input1().matches(input2) && recipe.input2().matches(input1)) {
                    itemInputHatch1.inventory.setItem(new MachineUpdateReason(), 0, input1.subtract(recipe.input2().getAmount()));
                    itemInputHatch2.inventory.setItem(new MachineUpdateReason(), 0, input2.subtract(recipe.input1().getAmount()));
                    matches = true;
                }
            }

            if (!matches) {
                continue;
            }

            if (recipe.outputFluid() != null) {
                getRecipeProgressItem().setItem(ItemStackBuilder.of(recipe.outputFluid().getItem())
                        .clearLore()
                );
            } else if (recipe.outputItem() != null) {
                getRecipeProgressItem().setItem(ItemStackBuilder.of(recipe.outputItem())
                        .clearLore()
                );
            }
            startRecipe(recipe, recipe.timeTicks());
            break;
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# F f F # b t p #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('F', fuelStack)
                .addIngredient('f', fuelInventory)
                .addIngredient('p', getRecipeProgressItem())
                .addIngredient('b', fuelProgressItem)
                .addIngredient('t', temperatureItem)
                .build();
    }

    @Override
    public void onRecipeFinished(@NonNull KilnRecipe recipe) {
        if (recipe.outputFluid() != null && recipe.outputFluidAmount() != null) {
            FluidOutputHatch fluidOutputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT_HATCH);
            fluidOutputHatch.setFluidType(recipe.outputFluid());
            fluidOutputHatch.addFluid(recipe.outputFluidAmount());
        }
        if (recipe.outputItem() != null) {
            getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUT_HATCH)
                    .inventory.addItem(new MachineUpdateReason(), recipe.outputItem());
        }
        getRecipeProgressItem().setItem(GuiItems.background());
        startRecipe();
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        Block light = getLight();
        if (light.getType().isAir()) {
            light.setType(Material.LIGHT);
            Light blockData = (Light) light.getBlockData();
            blockData.setLevel(0);
            light.setBlockData(blockData);
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);
        Block light = getLight();
        if (light.getType() == Material.LIGHT) {
            light.setType(Material.AIR);
        }
    }

    public @NotNull Block getLight() {
        return getBlock().getRelative(LIGHT.x, LIGHT.y, LIGHT.z);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "fuel", fuelInventory
        );
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("temperature-bar", PylonUtils.createBar(
                        temperature / maxTemperature,
                        20,
                        PylonUtils.colorToTextColor(PylonUtils.colorFromTemperature(temperature)
                ))),
                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature).decimalPlaces(1)),
                RebarArgument.of("progress", getRecipeProgress() == null
                        ? Component.empty()
                        : Component.translatable("pylon.waila.kiln").arguments(
                        RebarArgument.of("progress", PylonUtils.createProgressBar(
                                1.0 - getRecipeProgress(),
                                20,
                                TextColor.color(255, 255, 255)
                        ))
                ))
        ));
    }

    public class TemperatureItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player viewer) {
            return temperatureStack.clone()
                    .lore(Component.translatable("pylon.gui.kiln.temperature")
                            .arguments(RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature).decimalPlaces(1)))
                    );
        }

        @Override
        public void handleClick(@NonNull ClickType clickType, @NonNull Player player, @NonNull Click click) {}
    }
}
