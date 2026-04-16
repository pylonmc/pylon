package io.github.pylonmc.pylon.content.machines.hydraulics;

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
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.LogisticSlot;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class HydraulicHammerHead extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarProcessor,
        RebarLogisticBlock,
        RebarDirectionalBlock {

    public final int goDownTimeTicks = getSettings().getOrThrow("go-down-time-ticks", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
    public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final ItemStack emptyHammerTipStack = ItemStackBuilder.of(Material.AIR)
            .addCustomModelDataString(getKey() + ":hammer_tip:empty")
            .build();

    public final ItemStackBuilder hammerStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":hammer")
            .name(Component.translatable("pylon.gui.hammer"));

    public static class Item extends RebarItem {

        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
        public final double hydraulicFluidPerCraft = getSettings().getOrThrow("hydraulic-fluid-per-craft", ConfigAdapter.INTEGER);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100)),
                    RebarArgument.of("hydraulic-fluid-per-craft", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerCraft)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    private final VirtualInventory hammerInventory = new VirtualInventory(1);

    @SuppressWarnings("unused")
    public HydraulicHammerHead(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);

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

        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicHammerHead(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        hammerInventory.addPreUpdateHandler(event -> updateHammerTip(event.getNewItem()));
        hammerInventory.addPostUpdateHandler(event -> updateHammerTip(event.getNewItem()));
        createLogisticGroup("hammer", LogisticGroupType.INPUT, hammerInventory);
    }

    public void updateHammerTip(ItemStack newItem) {
        if (!(RebarItem.fromStack(newItem, Hammer.class) instanceof Hammer hammer)) {
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
            removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidPerCraft * getTickInterval() / getProcessTimeTicks());
            addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidPerCraft * getTickInterval() / getProcessTimeTicks());
            progressProcess(getTickInterval());
            return;
        }

        if (!(RebarItem.fromStack(hammerInventory.getItem(0), Hammer.class) instanceof Hammer hammer)) {
            return;
        }

        Block baseBlock = getBlock().getRelative(BlockFace.DOWN, 3);
        if (BlockStorage.isRebarBlock(baseBlock) || baseBlock.getType() != hammer.baseBlock) {
            return;
        }

        if (fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidPerCraft) {
            return;
        }

        if (!hammer.tryDoRecipe(baseBlock, null, null)) {
            return;
        }

        PylonUtils.animate(getHammerHead(), goDownTimeTicks, getHeadTransformation(-0.5));
        PylonUtils.animate(getHammerTip(), goDownTimeTicks, getTipTransformation(-1.5));

        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            if (!new BlockPosition(getBlock()).getChunk().isLoaded()) {
                return;
            }

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
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }

    public @Nullable ItemDisplay getHammerHead() {
        return getHeldEntity(ItemDisplay.class, "hammer_head");
    }

    public @Nullable ItemDisplay getHammerTip() {
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
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                ))
        ));
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
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("hammer", hammerInventory);
    }
}
