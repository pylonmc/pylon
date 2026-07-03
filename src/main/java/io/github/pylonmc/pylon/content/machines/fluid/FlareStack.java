package io.github.pylonmc.pylon.content.machines.fluid;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.api.FlammableTag;
import io.github.pylonmc.pylon.api.GasTag;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;


public class FlareStack extends RebarBlock implements SimpleRebarMultiblock, FluidRebarBlock, DirectionalRebarBlock {

    public static class Item extends RebarItem {

        public double fluidPerSecond = getSettingOrThrow("fluid-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("fluid-per-second", fluidPerSecond));
        }
    }

    public double fluidPerSecond = getSettingOrThrow("fluid-per-second", ConfigAdapter.DOUBLE);
    public int smokeAmount = getSettingOrThrow("smoke-amount", ConfigAdapter.INTEGER);
    public int flameAmount = getSettingOrThrow("flame-amount", ConfigAdapter.INTEGER);

    public FlareStack(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, getFacing());
    }

    public FlareStack(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        return fluid.hasTag(FlammableTag.class)
                ? fluidPerFluidTick()
                : 0.0;
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        new ParticleBuilder(Particle.FLAME)
                .location(getBlock().getLocation().toCenterLocation().add(0, 3.6, 0))
                .count(flameAmount)
                .extra(0.001)
                .offset(0.05, 0.05, 0.05)
                .spawn();

        for (int i = 0; i < 1 + (int)(smokeAmount * amount / fluidPerFluidTick()); i++) {
            new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                    .location(getBlock().getLocation().toCenterLocation().add(0, 4, 0))
                    .offset(0, 1, 0)
                    .count(0)
                    .extra(0.08)
                    .spawn();
        }
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                new Vector3i(0, 1, 0), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE),
                new Vector3i(0, 2, 0), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE),
                new Vector3i(0, 3, 0), MultiblockComponent.of(PylonKeys.FLARE_STACK_STRUCTURE)
        );
    }

    private double fluidPerFluidTick() {
        return fluidPerSecond * RebarConfig.FLUID_TICK_INTERVAL / 20;
    }
}
