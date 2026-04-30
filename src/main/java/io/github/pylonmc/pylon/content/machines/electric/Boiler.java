package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.util.BurnerProgressItem;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Boiler extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarProcessor,
        RebarTickingBlock,
        RebarVirtualInventoryBlock,
        RebarGuiBlock,
        RebarLogisticBlock {

    public static class Item extends RebarItem {

        private final double steamPerSecond = getSettings().getOrThrow("steam-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("water-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamPerSecond * PylonFluids.WATER_TO_STEAM_RATIO)),
                    RebarArgument.of("steam-production", UnitFormat.MILLIBUCKETS_PER_SECOND.format(steamPerSecond))
            );
        }
    }

    private static final ConfigAdapter<Map<ItemStack, Integer>> FUELS_TYPE = ConfigAdapter.MAP.from(
            ConfigAdapter.ITEM_STACK,
            ConfigAdapter.INTEGER
    );

    public record Fuel(@NotNull NamespacedKey key, @NotNull ItemStack item, int burnTimeSeconds) implements Keyed {
        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }

    public static final RebarRegistry<Fuel> FUEL_REGISTRY = new RebarRegistry<>(pylonKey("boiler_fuels"));

    static {
        for (var fuel : Settings.get(PylonKeys.BOILER).getOrThrow("fuels", FUELS_TYPE).entrySet()) {
            FUEL_REGISTRY.register(new Fuel(ItemTypeWrapper.of(fuel.getKey()).getKey(), fuel.getKey(), fuel.getValue()));
        }
    }

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double steamPerSecond = getSettings().getOrThrow("steam-per-second", ConfigAdapter.DOUBLE);

    private final VirtualInventory fuelInventory = new VirtualInventory(1);
    private final BurnerProgressItem progressItem = new BurnerProgressItem();

    @SuppressWarnings("unused")
    public Boiler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
    }

    @SuppressWarnings("unused")
    public Boiler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        setProcessProgressItem(progressItem);
        createLogisticGroup("fuel", LogisticGroupType.INPUT, fuelInventory);
        fuelInventory.addPreUpdateHandler(event -> {
            ItemStack item = event.getNewItem();
            for (Fuel fuel : FUEL_REGISTRY) {
                if (fuel.item().isSimilar(item)) {
                    return;
                }
            }
            event.setCancelled(true);
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # i p i # # #",
                        "# # # i x i # # #",
                        "# # # i i i # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', GuiItems.input())
                .addIngredient('p', progressItem)
                .addIngredient('x', fuelInventory)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("fuel", fuelInventory);
    }

    private static final Vector3i WATER_INPUT_HATCH = new Vector3i(1, 0, 0);
    private static final Vector3i STEAM_OUTPUT_HATCH = new Vector3i(-1, 0, 0);
    private static final Vector3i SMOKESTACK_CAP = new Vector3i(0, 2, 1);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(WATER_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(STEAM_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));

        // bottom layer
        for (int x = -1; x <= 1; x++) {
            for (int z = 0; z <= 2; z++) {
                components.put(new Vector3i(x, -1, z), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
            }
        }

        // middle layer
        components.put(new Vector3i(-1, 0, 1), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
        components.put(new Vector3i(1, 0, 1), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
        components.put(new Vector3i(-1, 0, 2), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
        components.put(new Vector3i(0, 0, 2), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
        components.put(new Vector3i(1, 0, 2), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));

        // top layer
        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
        for (int x = -1; x <= 1; x++) {
            for (int z = 1; z <= 2; z++) {
                components.put(new Vector3i(x, 1, z), new RebarMultiblockComponent(PylonKeys.BOILER_CASING));
            }
        }

        components.put(SMOKESTACK_CAP, new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, WATER_INPUT_HATCH).setAllowedFluids(PylonFluids.WATER);
    }

    private void tryStartProcessing() {
        for (Fuel fuel : FUEL_REGISTRY) {
            if (fuelInventory.removeFirstSimilar(new MachineUpdateReason(), 1, fuel.item()) > 0) {
                startProcess(fuel.burnTimeSeconds() * 20);
                refreshBlockTextureItem();
                return;
            }
        }
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        if (!isProcessing()) {
            tryStartProcessing();
        }

        if (!isProcessing()) return;

        progressProcess(tickInterval);

        double steamProduction = steamPerSecond * (tickInterval / 20.0);
        double waterConsumption = steamProduction * PylonFluids.WATER_TO_STEAM_RATIO;
        FluidInputHatch waterInput = getMultiblockComponentOrThrow(FluidInputHatch.class, WATER_INPUT_HATCH);
        FluidOutputHatch steamOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, STEAM_OUTPUT_HATCH);
        double toRemove = Math.min(Math.min(waterInput.getFluidAmount(), waterConsumption), steamOutput.getFluidSpaceRemaining() * PylonFluids.WATER_TO_STEAM_RATIO);
        if (toRemove > 0) {
            waterInput.removeFluid(PylonFluids.WATER, toRemove);
            steamOutput.addFluid(PylonFluids.STEAM, toRemove / PylonFluids.WATER_TO_STEAM_RATIO);
        }

        Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                .location(getBlock().getLocation().add(Vector.fromJOML(RebarUtils.rotateVectorToFace(SMOKESTACK_CAP, getFacing()))).toCenterLocation())
                .offset(0, 1, 0)
                .count(0)
                .extra(0.03)
                .spawn();
    }

    @Override
    public void finishProcess() {
        refreshBlockTextureItem();
        tryStartProcessing();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Pair<@NotNull String, @NotNull Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("lit", new Pair<>(String.valueOf(isProcessing()), 2));
        return properties;
    }
}
