package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.tools.Hammer;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
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
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;


public class DieselHammerHead extends RebarBlock implements
        RebarTickingBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarFluidBufferBlock,
        RebarProcessor,
        RebarDirectionalBlock,
        RebarLogisticBlock {

    public final int goDownTimeTicks = getSettings().getOrThrow("go-down-time-ticks", ConfigAdapter.INT);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
    public final double dieselPerCraft = getSettings().getOrThrow("diesel-per-craft", ConfigAdapter.INT);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.INT);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INT);

    private final VirtualInventory hammerInventory = new VirtualInventory(1);

    private final ItemStack emptyHammerTipStack = ItemStackBuilder.of(Material.AIR)
            .addCustomModelDataString(getKey() + ":hammer_tip:empty")
            .build();
    public final ItemStackBuilder hammerStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":hammer")
            .name(Component.translatable("pylon.gui.hammer"));
    public final ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public final ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public final ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    public static class Item extends RebarItem {

        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
        public final double dieselPerCraft = getSettings().getOrThrow("diesel-per-craft", ConfigAdapter.INT);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.INT);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100)),
                    RebarArgument.of("diesel-per-craft", UnitFormat.MILLIBUCKETS.format(dieselPerCraft)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public DieselHammerHead(@NotNull Block block, @NotNull BlockCreateContext context) {
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
                        .scale(0.8, 0.8, 1.1))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("hammer_head", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.GRAY_CONCRETE)
                        .addCustomModelDataString(getKey() + ":hammer_head")
                )
                .transformation(getHeadTransformation(0.7))
                .build(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
        );
        addEntity("hammer_tip", new ItemDisplayBuilder()
                .itemStack(emptyHammerTipStack)
                .transformation(getTipTransformation(-0.3))
                .build(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
        );

        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
    }

    @SuppressWarnings("unused")
    public DieselHammerHead(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        hammerInventory.addPreUpdateHandler(event -> updateHammerTip(event.getNewItem()));
        hammerInventory.addPostUpdateHandler(event -> updateHammerTip(event.getNewItem()));
        createLogisticGroup("hammer", LogisticGroupType.INPUT, hammerInventory);
    }

    public void updateHammerTip(ItemStack newItem) {
        if (!(RebarItem.fromStack(newItem) instanceof Hammer hammer)) {
            getHammerTip().setItemStack(null);
            return;
        }
        getHammerTip().setItemStack(ItemStackBuilder.of(hammer.baseBlock)
                .addCustomModelDataString(getKey() + ":hammer_tip:" + hammer.getKey().key())
                .build()
        );
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

        if (!(RebarItem.fromStack(hammerInventory.getItem(0)) instanceof Hammer hammer)) {
            return;
        }

        Block baseBlock = getBlock().getRelative(BlockFace.DOWN, 3);
        if (BlockStorage.isRebarBlock(baseBlock) || baseBlock.getType() != hammer.baseBlock) {
            return;
        }

        if (!hammer.tryDoRecipe(baseBlock, null, null, BlockFace.UP)) {
            return;
        }

        PylonUtils.animate(getHammerHead(), goDownTimeTicks, getHeadTransformation(-0.5));
        PylonUtils.animate(getHammerTip(), goDownTimeTicks, getTipTransformation(-1.5));

        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            PylonUtils.animate(getHammerHead(), (int)(hammer.cooldownTicks / speed) - goDownTimeTicks, getHeadTransformation(0.7));
            PylonUtils.animate(getHammerTip(), (int)(hammer.cooldownTicks / speed) - goDownTimeTicks, getTipTransformation(-0.3));

            new ParticleBuilder(Particle.BLOCK)
                    .data(baseBlock.getBlockData())
                    .count(20)
                    .location(baseBlock.getLocation().toCenterLocation().add(0, 0.6, 0))
                    .spawn();
            startProcess((int)(hammer.cooldownTicks / speed));
        }, goDownTimeTicks);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # H # # # #",
                        "# # # # x # # # #",
                        "# # # # H # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('H', hammerStack)
                .addIngredient('x', hammerInventory)
                .build();
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }

    public @NotNull ItemDisplay getHammerHead() {
        return getHeldEntityOrThrow(ItemDisplay.class, "hammer_head");
    }

    public @NotNull ItemDisplay getHammerTip() {
        return getHeldEntityOrThrow(ItemDisplay.class, "hammer_tip");
    }

    public static @NotNull Matrix4f getHeadTransformation(double translationY) {
        return new TransformBuilder()
                .translate(0, translationY, 0)
                .scale(0.3, 2.0, 0.3)
                .buildForItemDisplay();
    }

    public static @NotNull Matrix4f getTipTransformation(double translationY) {
        return new TransformBuilder()
                .translate(0, translationY, 0)
                .scale(0.6, 0.1, 0.6)
                .buildForItemDisplay();
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
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("hammer", hammerInventory);
    }
}
