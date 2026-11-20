package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.api.FlammableTag;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class CombustionTower extends RebarBlock implements
        TickingRebarBlock,
        SimpleRebarMultiblock {

    @Getter
    private final double exhaustProduction = getSettingOrThrow("exhaust-production", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        @Getter
        private final double exhaustProduction = getSettingOrThrow("exhaust-production", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("exhaust-production", UnitFormat.MILLIBUCKETS_PER_SECOND.format(exhaustProduction))
            );
        }
    }

    @SuppressWarnings("unused")
    public CombustionTower(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
    }

    @SuppressWarnings("unused")
    public CombustionTower(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        FluidInputHatch inputHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH);
        if (inputHatch.getFluidType() == null) return;

        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH);

        double secondsPassed = getTickInterval() / 20.0;
        double outputRatio = Math.min(outputHatch.getFluidSpaceRemaining() / (exhaustProduction * secondsPassed), 1);
        double theoreticalMaxFuel = secondsPassed / inputHatch.getFluidType().getTag(FlammableTag.class).secondsPerBucket() * 1000;
        double inputRatio = Math.min(inputHatch.getFluidAmount() / theoreticalMaxFuel, 1);
        double finalRatio = Math.min(outputRatio, inputRatio);

        inputHatch.removeFluid(theoreticalMaxFuel * finalRatio);
        outputHatch.addFluid(PylonFluids.VERY_HOT_EXHAUST, exhaustProduction * secondsPassed * finalRatio);

        Particle.CAMPFIRE_SIGNAL_SMOKE.builder()
                .location(getBlock().getLocation().add(Vector.fromJOML(SMOKESTACK)).toCenterLocation())
                .offset(0, 1, 0)
                .count(0)
                .speed(0.03)
                .spawn();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player);
    }

    private static final Vector3i SMOKESTACK = new Vector3i(0, 3, 0);
    private static final Vector3i INPUT_HATCH = new Vector3i(0, 0, -1);
    private static final Vector3i OUTPUT_HATCH = new Vector3i(0, 0, 1);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                new Vector3i(0, 1, 0), MultiblockComponent.of(PylonKeys.SMOKESTACK_RING),
                new Vector3i(0, 2, 0), MultiblockComponent.of(PylonKeys.SMOKESTACK_RING),
                SMOKESTACK, MultiblockComponent.of(PylonKeys.SMOKESTACK_CAP),
                INPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                OUTPUT_HATCH, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH)
        );
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH).setAllowedFluids(FlammableTag.getFlammableFluids());
    }
}