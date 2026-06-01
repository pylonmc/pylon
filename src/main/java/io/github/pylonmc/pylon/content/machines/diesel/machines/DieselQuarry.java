package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.generic.GenericQuarry;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class DieselQuarry extends GenericQuarry implements
        FluidBufferRebarBlock {

    public final int dieselPerBlock = getSettings().getOrThrow("diesel-per-block", ConfigAdapter.INTEGER);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final int radius = getSettings().getOrThrow("radius", ConfigAdapter.INTEGER);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
        public final int dieselPerBlock = getSettings().getOrThrow("diesel-per-block", ConfigAdapter.INTEGER);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            int diameter = 2 * radius + 1;
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("mining-area", diameter + "x" + diameter),
                    RebarArgument.of("diesel-per-block", UnitFormat.MILLIBUCKETS.format(dieselPerBlock)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public DieselQuarry(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
                        .addCustomModelDataString(getKey() + ":chimney"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.4, 0.0, -0.4)
                        .scale(0.15))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.BRICKS)
                        .addCustomModelDataString(getKey() + ":side1"))
                .transformation(new TransformBuilder()
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.BRICKS)
                        .addCustomModelDataString(getKey() + ":side2"))
                .transformation(new TransformBuilder()
                        .translate(0, -0.5, 0)
                        .scale(0.8, 0.8, 1.1))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.YELLOW_TERRACOTTA)
                        .addCustomModelDataString(getKey() + ":top"))
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.199, 0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top2", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.YELLOW_TERRACOTTA)
                        .addCustomModelDataString(getKey() + ":top"))
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.2, 0.6)
                        .rotate(0, Math.PI / 4, 0))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.55F);

        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
    }

    @SuppressWarnings("unused")
    public DieselQuarry(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessing() || fluidAmount(PylonFluids.BIODIESEL) < dieselPerBlock) {
            return;
        }

        progressProcess(tickInterval);
        Vector smokePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0.4, 0.7, -0.4),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .offset(0, 1, 0)
                .count(0)
                .extra(0.05)
                .spawn();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.BIODIESEL),
                        fluidCapacity(PylonFluids.BIODIESEL),
                        20,
                        TextColor.fromHexString("#eaa627")
                ))
        ));
    }

    @Override
    protected boolean canBreakBlock() {
        return fluidAmount(PylonFluids.BIODIESEL) >= dieselPerBlock;
    }

    @Override
    protected void onBreakBlock() {
        removeFluid(PylonFluids.BIODIESEL, dieselPerBlock);
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        FluidBufferRebarBlock.super.onFluidAdded(fluid, amount);
        updateQuarry();
    }

    @Override
    public void onBlockBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        super.onBlockBreak(drops, context);
        FluidBufferRebarBlock.super.onBlockBreak(drops, context);
    }
}
