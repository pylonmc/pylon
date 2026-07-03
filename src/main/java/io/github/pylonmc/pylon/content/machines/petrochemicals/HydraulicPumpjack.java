package io.github.pylonmc.pylon.content.machines.petrochemicals;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HydraulicPumpjack extends RebarBlock implements
        DirectionalRebarBlock,
        SimpleRebarMultiblock,
        TickingRebarBlock {

    public static class Item extends RebarItem {

        public final int maxOilPerSecond = getSettingOrThrow("max-oil-per-second", ConfigAdapter.INTEGER);
        public final int hydraulicFluidPerSecond = getSettingOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("max-oil-per-second", UnitFormat.MILLIBUCKETS_PER_SECOND.format(maxOilPerSecond)),
                    RebarArgument.of("hydraulic-fluid-per-second", UnitFormat.MILLIBUCKETS_PER_SECOND.format(hydraulicFluidPerSecond))
            );
        }
    }

    public final ItemStackBuilder engineRodStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":engine_rod");
    public final ItemStackBuilder oilRodStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":oil_rod");
    public final ItemStackBuilder beamStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":beam");
    public final ItemStackBuilder balanceStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":balance");

    public static final NamespacedKey ANIMATION_TICK = PylonUtils.pylonKey("animation_tick");

    private static final Vector3i HYDRAULIC_FLUID_INPUT = new Vector3i(-1, 0, 0);
    private static final Vector3i DIRTY_HYDRAULIC_FLUID_OUTPUT = new Vector3i(-1, 0, 1);
    private static final Vector3i OIL_OUTPUT = new Vector3i(-1, 0, 5);
    private static final Vector3i HYDRAULIC_FRACTURE = new Vector3i(0, 0, 5);
    private static final Vector3i FLARE_STACK_TIP = new Vector3i(-1, 2, 6);

    private static final double BEAM_LENGTH = 5.0;

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int maxOilPerSecond = getSettingOrThrow("max-oil-per-second", ConfigAdapter.INTEGER);
    public final int hydraulicFluidPerSecond = getSettingOrThrow("hydraulic-fluid-per-second", ConfigAdapter.INTEGER);
    public final int strokeDuration = getSettingOrThrow("stroke-duration", ConfigAdapter.INTEGER);
    public final double animationAmplitude = getSettingOrThrow("animation-amplitude", ConfigAdapter.DOUBLE);
    public final double yieldDepletion = getSettingOrThrow("yield-depletion", ConfigAdapter.DOUBLE);

    private int animationTick;

    public HydraulicPumpjack(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);

        Vector engineRodPosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0, 1.5, 1.0),
                getFacing()
        ));

        addEntity("engine_rod", new ItemDisplayBuilder()
                .itemStack(engineRodStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -animationAmplitude, 0)
                        .scale(0.2, 3, 0.2)
                )
                .build(getBlock().getLocation().toCenterLocation().add(engineRodPosition))
        );

        Vector oilRodPosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0, 1.0, 5.0),
                getFacing()
        ));

        addEntity("oil_rod", new ItemDisplayBuilder()
                .itemStack(oilRodStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, animationAmplitude, 0)
                        .scale(0.2, 4, 0.2)
                )
                .build(getBlock().getLocation().toCenterLocation().add(oilRodPosition))
        );

        Vector beamPosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0, 3.0, 3.0),
                getFacing()
        ));

        addEntity("beam", new ItemDisplayBuilder()
                .itemStack(beamStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.asin(animationAmplitude / (0.5 * BEAM_LENGTH)), 0, 0)
                        .scale(0.3, 0.5, BEAM_LENGTH)
                )
                .build(getBlock().getLocation().toCenterLocation().add(beamPosition))
        );

        addEntity("balance", new ItemDisplayBuilder()
                .itemStack(balanceStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.asin(animationAmplitude / (0.5 * BEAM_LENGTH)), 0, 0)
                        .scale(2.0, 0.3, 0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().add(beamPosition))
        );
    }

    public HydraulicPumpjack(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        animationTick = pdc.get(ANIMATION_TICK, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(ANIMATION_TICK, RebarSerializers.INTEGER, animationTick);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(1, 0, -1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(0, 0, -1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(-1, 0, -1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(1, 0, 0), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(HYDRAULIC_FLUID_INPUT, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(1, 0, 1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(0, 0, 1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(DIRTY_HYDRAULIC_FLUID_OUTPUT, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));

        components.put(new Vector3i(0, 1, 1), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(1, 1, 1), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 1, 0), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(1, 1, -1), MultiblockComponent.of(PylonKeys.REINFORCED_GLASS_CASING));
        components.put(new Vector3i(0, 1, -1), MultiblockComponent.of(PylonKeys.REINFORCED_PLATING));
        components.put(new Vector3i(-1, 1, -1), MultiblockComponent.of(PylonKeys.REINFORCED_GLASS_CASING));
        components.put(new Vector3i(0, 1, 0), MultiblockComponent.of(PylonKeys.HYDRAULIC_MOTOR));

        components.put(new Vector3i(1, 0, 3), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(1, 1, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 2, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 3, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 0, 3), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(-1, 1, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 2, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 3, 3), MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM));

        components.put(OIL_OUTPUT, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(HYDRAULIC_FRACTURE, MultiblockComponent.of(PylonKeys.HYDRAULIC_FRACTURE));
        components.put(new Vector3i(-1, 0, 6), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        components.put(new Vector3i(-1, 1, 6), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE));
        components.put(FLARE_STACK_TIP, MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE));

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT).setAllowedFluid(PylonFluids.HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, DIRTY_HYDRAULIC_FLUID_OUTPUT).setFluidType(PylonFluids.DIRTY_HYDRAULIC_FLUID);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, OIL_OUTPUT).setFluidType(PylonFluids.OIL);
        Waila.removeWailaOverride(getMultiblockBlock(HYDRAULIC_FRACTURE));
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        FluidInputHatch hydraulicFluidInput = getMultiblockComponentOrThrow(FluidInputHatch.class, HYDRAULIC_FLUID_INPUT);
        FluidOutputHatch dirtyHydraulicFluidOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, DIRTY_HYDRAULIC_FLUID_OUTPUT);
        FluidOutputHatch oilOutput = getMultiblockComponentOrThrow(FluidOutputHatch.class, OIL_OUTPUT);
        HydraulicFracture fracture = getMultiblockComponentOrThrow(HydraulicFracture.class, HYDRAULIC_FRACTURE);

        if (fracture.yield < 1.0e-3) {
            return;
        }

        boolean isUpStroke = animationTick >= strokeDuration / 2;

        if (hydraulicFluidInput.getFluidAmount() < hydraulicFluidPerSecond * getTickInterval() / 20.0
                || dirtyHydraulicFluidOutput.getFluidSpaceRemaining() < hydraulicFluidPerSecond * getTickInterval() / 20.0
                || (isUpStroke && oilOutput.getFluidSpaceRemaining() < maxOilPerSecond * getTickInterval() / 20.0)
        ) {
            return;
        }

        hydraulicFluidInput.removeFluid(hydraulicFluidPerSecond * getTickInterval() / 20.0);
        dirtyHydraulicFluidOutput.addFluid(hydraulicFluidPerSecond * getTickInterval() / 20.0);
        if (isUpStroke) {
            oilOutput.addFluid(2.0 * fracture.yield * maxOilPerSecond * getTickInterval() / 20.0);
        }

        double displacement = animationAmplitude * Math.cos(Math.PI * 2.0 * animationTick / strokeDuration);
        double angle = Math.asin(displacement / (0.5 * BEAM_LENGTH));

        ItemDisplay engineRod = getHeldEntityOrThrow(ItemDisplay.class, "engine_rod");
        engineRod.setInterpolationDuration(getTickInterval());
        engineRod.setInterpolationDelay(0);
        engineRod.setTransformationMatrix(new TransformBuilder()
                .lookAlong(getFacing())
                .translate(0, -displacement, 0)
                .scale(0.2, 3, 0.2)
                .buildForItemDisplay()
        );

        ItemDisplay oilRod = getHeldEntityOrThrow(ItemDisplay.class, "oil_rod");
        oilRod.setInterpolationDuration(getTickInterval());
        oilRod.setInterpolationDelay(0);
        oilRod.setTransformationMatrix(new TransformBuilder()
                .lookAlong(getFacing())
                .translate(0, displacement, 0)
                .scale(0.2, 4, 0.2)
                .buildForItemDisplay()
        );

        ItemDisplay beam = getHeldEntityOrThrow(ItemDisplay.class, "beam");
        beam.setInterpolationDuration(getTickInterval());
        beam.setInterpolationDelay(0);
        beam.setTransformationMatrix(new TransformBuilder()
                .lookAlong(getFacing())
                .rotate(angle, 0, 0)
                .scale(0.3, 0.5, BEAM_LENGTH)
                .buildForItemDisplay()
        );

        ItemDisplay balance = getHeldEntityOrThrow(ItemDisplay.class, "balance");
        balance.setInterpolationDuration(getTickInterval());
        balance.setInterpolationDelay(0);
        balance.setTransformationMatrix(new TransformBuilder()
                .lookAlong(getFacing())
                .rotate(angle, 0, 0)
                .scale(2.0, 0.4, 0.4)
                .buildForItemDisplay()
        );

        if (isUpStroke) {
            new ParticleBuilder(Particle.SMOKE)
                    .location(getMultiblockBlock(HYDRAULIC_FRACTURE).getLocation().toCenterLocation().add(0, 0.6, 0))
                    .count((int) (10 * -Math.sin(Math.PI * 2.0 * animationTick / strokeDuration)))
                    .offset(0.2, 0, 0.2)
                    .extra(0.003)
                    .spawn();
        }

        if (animationTick == (int) (strokeDuration * 0.8)) {
            new ParticleBuilder(Particle.FLAME)
                    .location(getMultiblockBlock(FLARE_STACK_TIP).getLocation().toCenterLocation().add(0, 0.6, 0))
                    .count(20)
                    .extra(0.0)
                    .spawn();
            new ParticleBuilder(Particle.LARGE_SMOKE)
                    .location(getMultiblockBlock(FLARE_STACK_TIP).getLocation().toCenterLocation().add(0, 0.9, 0))
                    .count(50)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.01)
                    .spawn();
        }

        if (animationTick == strokeDuration / 2) {
            fracture.yield /= (1 + yieldDepletion);
        }

        animationTick = (animationTick + 1) % strokeDuration;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        HydraulicFracture fracture = getMultiblockComponentOrThrow(HydraulicFracture.class, HYDRAULIC_FRACTURE);
        double oilPerSecond = fracture.yield * maxOilPerSecond;
        return WailaDisplay.of(this, player)
                .add(fracture.yield < 1.0e-3
                        ? Component.translatable("pylon.message.pumpjack.no-oil")
                        : new ProgressBar()
                        .barColor(TextColor.color(HSVLike.hsvLike((float) (fracture.yield * 0.324F), 1.0F, 1.0F)))
                        .bars(30)
                        .proportion(fracture.yield)
                        .suffix(Component.text(" ").append(UnitFormat.MILLIBUCKETS_PER_SECOND.format(oilPerSecond).decimalPlaces(1)))
                );
    }
}
