package io.github.pylonmc.pylon.content.machines.diesel.production;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Biorefinery extends RebarBlock implements
        RebarDirectionalBlock,
        RebarSimpleMultiblock,
        RebarProcessor,
        RebarTickingBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double biodieselPerSecond = getSettings().getOrThrow("biodiesel-per-second", ConfigAdapter.DOUBLE);
    public final double ethanolPerMbOfBiodiesel = getSettings().getOrThrow("ethanol-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);
    public final double plantOilPerMbOfBiodiesel = getSettings().getOrThrow("plant-oil-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);

    public static final Vector3i FUEL_INPUT_HATCH = new Vector3i(1, 0, 3);
    public static final Vector3i BIODIESEL_OUTPUT_HATCH = new Vector3i(0, 0, -1);
    public static final Vector3i ETHANOL_INPUT_HATCH = new Vector3i(-1, 0, 0);
    public static final Vector3i PLANT_OIL_INPUT_HATCH = new Vector3i(2, 0, 0);

    public static class Item extends RebarItem {

        public final double biodieselPerSecond = getSettings().getOrThrow("biodiesel-per-second", ConfigAdapter.DOUBLE);
        public final double ethanolPerMbOfBiodiesel = getSettings().getOrThrow("ethanol-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);
        public final double plantOilPerMbOfBiodiesel = getSettings().getOrThrow("plant-oil-per-mb-of-biodiesel", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("biodiesel-per-second", UnitFormat.MILLIBUCKETS_PER_SECOND.format(biodieselPerSecond)),
                    RebarArgument.of("ethanol-per-mb-of-biodiesel", UnitFormat.MILLIBUCKETS.format(ethanolPerMbOfBiodiesel)),
                    RebarArgument.of("plant-oil-per-mb-of-biodiesel", UnitFormat.MILLIBUCKETS.format(plantOilPerMbOfBiodiesel))
            );
        }
    }

    public Biorefinery(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);
    }

    public Biorefinery(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        // foundation
        components.put(BIODIESEL_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(ETHANOL_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(PLANT_OIL_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(0, 0, 2), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(0, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(-1, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(FUEL_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(new Vector3i(0, 0, 4), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        // tower
        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.DISTILLATION_TOWER_RING));
        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.DISTILLATION_TOWER_RING));
        components.put(new Vector3i(0, 3, 0), new RebarMultiblockComponent(PylonKeys.DISTILLATION_TOWER_RING));
        components.put(new Vector3i(0, 4, 0), new RebarMultiblockComponent(PylonKeys.DISTILLATION_TOWER_RING));
        components.put(new Vector3i(1, 1, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 2, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 3, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 4, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 5, 0), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));

        // burner smokestack
        components.put(new Vector3i(0, 1, 3), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(0, 2, 3), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(0, 3, 3), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(0, 4, 3), new RebarMultiblockComponent(PylonKeys.SMOKESTACK_CAP));

        // casing
        components.put(new Vector3i(-1, 0, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 0, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(1, 0, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 1, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 0, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 1, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(2, 0, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(2, 1, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(2, 0, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(2, 1, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(0, 1, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(0, 1, 2), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 1, 3), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, 3), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(0, 1, 4), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));

        components.put(new Vector3i(-1, 3, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 3, 0), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 3, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(0, 3, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 3, 1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(1, 3, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(0, 3, -1), new RebarMultiblockComponent(PylonKeys.REINFORCED_PLATING));

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, ETHANOL_INPUT_HATCH).setAllowedFluids(PylonFluids.ETHANOL);
        getMultiblockComponentOrThrow(FluidInputHatch.class, PLANT_OIL_INPUT_HATCH).setAllowedFluids(PylonFluids.PLANT_OIL);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        // Tick production
        if (isProcessing()) {
            progressProcess(getTickInterval());
            FluidInputHatch ethanolInputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, ETHANOL_INPUT_HATCH);
            FluidInputHatch plantOilInputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, PLANT_OIL_INPUT_HATCH);
            FluidOutputHatch biodieselOutputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, BIODIESEL_OUTPUT_HATCH);

            double biodieselToProduce = Math.min(
                    biodieselOutputHatch.getFluidSpaceRemaining(),
                    Math.min(
                            biodieselPerSecond * getTickInterval() / 20.0,
                            Math.min(
                                    ethanolInputHatch.getFluidAmount() / ethanolPerMbOfBiodiesel,
                                    plantOilInputHatch.getFluidAmount() / plantOilPerMbOfBiodiesel
                            )
                    )
            );

            if (biodieselToProduce > 1.0e-3) {
                ethanolInputHatch.removeFluid(PylonFluids.ETHANOL, biodieselToProduce * ethanolPerMbOfBiodiesel);
                plantOilInputHatch.removeFluid(PylonFluids.PLANT_OIL, biodieselToProduce * plantOilPerMbOfBiodiesel);
                biodieselOutputHatch.addFluid(PylonFluids.BIODIESEL, biodieselToProduce);
            }

            Vector smokePosition1 = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                    new Vector3d(1, 5, 0),
                    getFacing()
            ));
            new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                    .location(getBlock().getLocation().toCenterLocation().add(smokePosition1))
                    .offset(0, 1, 0)
                    .count(0)
                    .extra(0.05)
                    .spawn();

            Vector smokePosition2 = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                    new Vector3d(0, 4, 3),
                    getFacing()
            ));
            new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                    .location(getBlock().getLocation().toCenterLocation().add(smokePosition2))
                    .offset(0, 1, 0)
                    .count(0)
                    .extra(0.05)
                    .spawn();
        }

        // Consume fuel
        if (!isProcessing()) {
            ItemInputHatch fuelInputHatch = getMultiblockComponent(ItemInputHatch.class, FUEL_INPUT_HATCH);
            ItemStack input = fuelInputHatch.inventory.getItem(0);
            if (input != null) {
                for (Fuel fuel : FUELS) {
                    if (fuel.stack.isSimilar(input)) {
                        fuelInputHatch.inventory.setItem(new MachineUpdateReason(), 0, input.subtract());
                        startProcess(fuel.burnTimeSeconds * 20);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        if (isProcessing()) {
            double percent = (double) getProcessTicksRemaining() / getProcessTimeTicks();
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                    RebarArgument.of("info", Component.translatable("pylon.message.biorefinery.has_fuel").arguments(
                            RebarArgument.of("fuel-bar", PylonUtils.createBar(
                                    percent,
                                    20,
                                    TextColor.color(255, 200, 50)
                            )),
                            RebarArgument.of("remaining-time", UnitFormat.SECONDS.format(getProcessTicksRemaining() / 20))
                    ))
            ));
        } else {
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                    RebarArgument.of("info", Component.translatable("pylon.message.biorefinery.no_fuel"))
            ));
        }
    }

    public record Fuel(
            @NotNull NamespacedKey key,
            @NotNull ItemStack stack,
            int burnTimeSeconds
    ) implements Keyed {
        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }

    public static final NamespacedKey FUELS_KEY = pylonKey("biorefinery_fuels");
    public static final RebarRegistry<Fuel> FUELS = new RebarRegistry<>(FUELS_KEY);

    static {
        RebarRegistry.addRegistry(FUELS);
        FUELS.register(new Fuel(
                pylonKey("coal"),
                new ItemStack(Material.COAL),
                15
        ));
        FUELS.register(new Fuel(
                pylonKey("coal_block"),
                new ItemStack(Material.COAL_BLOCK),
                135
        ));
        FUELS.register(new Fuel(
                pylonKey("charcoal"),
                new ItemStack(Material.CHARCOAL),
                10
        ));
        FUELS.register(new Fuel(
                pylonKey("charcoal_block"),
                PylonItems.CHARCOAL_BLOCK,
                90
        ));
    }
}
