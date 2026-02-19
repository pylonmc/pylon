package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.simple.MixingPot;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.List;


public class DieselMixingAttachment extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarProcessor,
        RebarDirectionalBlock {

    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final int downAnimationTimeTicks = getSettings().getOrThrow("down-animation-time-ticks", ConfigAdapter.INTEGER);
    public final int upAnimationTimeTicks = getSettings().getOrThrow("up-animation-time-ticks", ConfigAdapter.INTEGER);
    public final double dieselPerCraft = getSettings().getOrThrow("diesel-per-craft", ConfigAdapter.INTEGER);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public final ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public final ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public final ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    public static class Item extends RebarItem {

        public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
        public final double dieselPerCraft = getSettings().getOrThrow("diesel-per-craft", ConfigAdapter.INTEGER);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0)),
                    RebarArgument.of("diesel-per-craft", UnitFormat.MILLIBUCKETS.format(dieselPerCraft)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public DieselMixingAttachment(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

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
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(sideStack2)
                .transformation(new TransformBuilder()
                        .translate(0, -0.5, 0)
                        .scale(0.9, 0.8, 1.1))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("mixing_attachment_shaft", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
                        .addCustomModelDataString(getKey() + ":mixing_attachment_shaft")
                )
                .transformation(getShaftTransformation(0.7))
                .build(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
        );

        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
    }

    @SuppressWarnings("unused")
    public DieselMixingAttachment(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (isProcessing()) {
            double dieselToConsume = dieselPerCraft * getTickInterval() / getProcessTimeTicks();
            if (fluidAmount(PylonFluids.BIODIESEL) < dieselToConsume) {
                return;
            }

            removeFluid(PylonFluids.BIODIESEL, dieselToConsume);
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
            progressProcess(getTickInterval());
            return;
        }

        MixingPot mixingPot = BlockStorage.getAs(MixingPot.class, getBlock().getRelative(BlockFace.DOWN, 2));
        if (mixingPot == null || !mixingPot.tryDoRecipe()) {
            return;
        }

        PylonUtils.animate(getMixingAttachmentShaft(), downAnimationTimeTicks, getShaftTransformation(0.2));
        Bukkit.getScheduler().runTaskLater(
                Pylon.getInstance(),
                () -> {
                    PylonUtils.animate(getMixingAttachmentShaft(), upAnimationTimeTicks, getShaftTransformation(0.7));
                    startProcess(cooldownTicks);

                },
                downAnimationTimeTicks
        );
    }

    private static @NotNull Matrix4f getShaftTransformation(double yTranslation) {
        return new TransformBuilder()
                .translate(0, yTranslation, 0)
                .scale(0.2, 1.5, 0.2)
                .buildForItemDisplay();
    }

    public @Nullable ItemDisplay getMixingAttachmentShaft() {
        return getHeldEntity(ItemDisplay.class, "mixing_attachment_shaft");
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
}
