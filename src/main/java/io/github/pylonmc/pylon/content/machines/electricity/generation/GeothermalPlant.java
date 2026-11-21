package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class GeothermalPlant extends RebarBlock implements
        SimpleRebarMultiblock,
        TickingRebarBlock {

    @UtilityClass
    public static class BiomeSettings {
        private static final Map<Biome, Double> BIOME_TEMPERATURES = new HashMap<>();
        private static final Map<Biome, ItemStack> BIOME_ITEMS = new HashMap<>();

        public static void load() {
            BIOME_TEMPERATURES.clear();
            BIOME_ITEMS.clear();

            ConfigSection settings = ConfigSection.fromSettings(PylonKeys.GEOTHERMAL_PLANT);
            Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
            ConfigAdapter<Biome> biomeAdapter = ConfigAdapter.KEYED.fromRegistry(Biome.class, biomeRegistry);

            Map<Biome, ConfigSection> biomeSettings = settings.getOrThrow("biomes", ConfigAdapter.MAP.from(biomeAdapter, ConfigAdapter.CONFIG_SECTION));
            Set<Biome> ignored = settings.getOrThrow("ignored-biomes", ConfigAdapter.SET.from(biomeAdapter));

            for (Biome biome : biomeRegistry) {
                if (ignored.contains(biome)) continue;
                ConfigSection setting = biomeSettings.get(biome);
                if (setting == null) {
                    Pylon.getInstance().getLogger().warning("Biome " + biome.getKey() + " does not have a geothermal plant settings entry, plz tell devs fix.");
                    continue;
                }
                BIOME_ITEMS.put(biome, setting.getOrThrow("item", ConfigAdapter.ITEM_STACK));
                BIOME_TEMPERATURES.put(biome, setting.getOrThrow("temperature", ConfigAdapter.DOUBLE));
            }
        }

        public static @Nullable Double getTemperature(@NotNull Biome biome) {
            return BIOME_TEMPERATURES.get(biome);
        }

        public static @Nullable ItemStack getItem(@NotNull Biome biome) {
            return BIOME_ITEMS.get(biome);
        }
    }

    @Getter
    private double efficiency = 0;

    @Getter
    private final double baseRate = getSettingOrThrow("base-rate", ConfigAdapter.DOUBLE);

    @Getter
    private final double steamToWaterRatio = getSettingOrThrow("steam-to-water-ratio", ConfigAdapter.DOUBLE);

    @SuppressWarnings("unused")
    public GeothermalPlant(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
    }

    @SuppressWarnings("unused")
    public GeothermalPlant(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        Location location = getBlock().getLocation();
        if (location.getWorld().getEnvironment() != World.Environment.NORMAL) return;

        FluidInputHatch inputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH);
        if (inputHatch.getFluidType() == null) return;
        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH);

        double secondsPassed = getTickInterval() / 20.0;
        double consumption = Math.min(baseRate * efficiency * secondsPassed, inputHatch.getFluidAmount());
        double production = Math.min(consumption * steamToWaterRatio, outputHatch.getFluidSpaceRemaining());
        double actualConsumption = production / steamToWaterRatio;

        inputHatch.removeFluid(actualConsumption);
        outputHatch.addFluid(PylonFluids.STEAM, production);
    }

    private static final Vector3i INPUT_HATCH = new Vector3i(0, 0, 1);
    private static final Vector3i OUTPUT_HATCH = new Vector3i(0, 0, -1);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                OUTPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH),
                new Vector3i(0, -1, 0), MultiblockComponent.of(PylonKeys.GEOTHERMAL_PIPE)
        );
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        return SimpleRebarMultiblock.super.isPartOfMultiblock(otherBlock)
                || (getBlock().getX() == otherBlock.getX() && getBlock().getZ() == otherBlock.getZ() && getBlock().getY() > otherBlock.getY());
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH).setAllowedFluid(PylonFluids.WATER);
        onMultiblockRefreshed();
    }

    @Override
    public void onMultiblockRefreshed() {
        efficiency = 0;
        Block bottom = getBlock().getRelative(BlockFace.DOWN);
        while (BlockStorage.isRebarBlock(bottom, PylonKeys.GEOTHERMAL_PIPE)) {
            bottom = bottom.getRelative(BlockFace.DOWN);
        }
        bottom = bottom.getRelative(BlockFace.UP);

        Double bottomAddition = BiomeSettings.getTemperature(bottom.getComputedBiome());
        Double topAddition = BiomeSettings.getTemperature(getBlock().getComputedBiome());
        if (bottomAddition == null || topAddition == null) return;

        double bottomTemp = baseTemperatureOf(bottom) + bottomAddition;
        double topTemp = baseTemperatureOf(getBlock()) + topAddition;
        efficiency = Math.abs(bottomTemp - topTemp);
    }

    /**
     * Returns a value such that the top of the world is 0 and the bottom is 1. Block y is minned to the surface.
     */
    private static double baseTemperatureOf(Block block) {
        World world = block.getWorld();
        int worldgenSurface = world.getHighestBlockYAt(block.getX(), block.getZ(), HeightMap.WORLD_SURFACE_WG);
        int y = Math.min(block.getY(), worldgenSurface);
        double scaled = (double) (y - world.getMinHeight()) / (world.getMaxHeight() - world.getMinHeight());
        return 1 - scaled;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(UnitFormat.MILLIBUCKETS_PER_SECOND.format(baseRate * efficiency).decimalPlaces(2));
    }
}