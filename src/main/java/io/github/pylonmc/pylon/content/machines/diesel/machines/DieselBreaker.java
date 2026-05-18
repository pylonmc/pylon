package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.generic.GenericBreaker;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.base.RebarDispenser;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import java.util.List;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;


public class DieselBreaker extends GenericBreaker implements
        RebarFluidBufferBlock,
        RebarDispenser {

    public final double dieselPerBlock = getSettings().getOrThrow("diesel-per-block", ConfigAdapter.DOUBLE);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double dieselPerBlock = getSettings().getOrThrow("diesel-per-block", ConfigAdapter.DOUBLE);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("diesel-per-block", UnitFormat.MILLIBUCKETS.format(dieselPerBlock)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }
    private ItemStackBuilder drillStack = ItemStackBuilder.of(Material.YELLOW_CONCRETE)
            .addCustomModelDataString(getKey() + ":drill");
    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder topStack = ItemStackBuilder.of(Material.BLUE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":top");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    @SuppressWarnings("unused")
    public DieselBreaker(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.55F);
        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(chimneyStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.4, 0.0, -0.4)
                        .scale(0.15))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(sideStack1)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, -0.1)
                        .scale(0.8, 0.8, 0.9))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(sideStack2)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.55, 0.2, 0.55))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(drillStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0.5)
                        .scale(0.6, 0.6, 0.2)
                        .rotate(0, 0, Math.PI / 4))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
    }

    @SuppressWarnings("unused")
    public DieselBreaker(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessing()) {
            return;
        }

        progressProcess(tickInterval);
        Block drilling = getBlock().getRelative(getFacing());
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
        new ParticleBuilder(Particle.BLOCK)
                .count(5)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.6, 0))
                .data(drilling.getBlockData())
                .spawn();
    }

    @Override
    protected boolean canStartDrilling(ItemStack tool, Block block) {
        return super.canStartDrilling(tool, block) && fluidAmount(PylonFluids.BIODIESEL) >= dieselPerBlock;
    }

    @Override
    public void onProcessFinished() {
        super.onProcessFinished();
        removeFluid(PylonFluids.BIODIESEL, dieselPerBlock);
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
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidAdded(fluid, amount);
        tryStartDrilling();
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onPreDispense(@NotNull BlockPreDispenseEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }
}
