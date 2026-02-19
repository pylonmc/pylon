package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.hydraulics.Miner;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.base.*;
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
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
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
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DieselMiner extends Miner implements
        RebarTickingBlock,
        RebarDirectionalBlock,
        RebarFluidBufferBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarLogisticBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
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
                    RebarArgument.of("mining-area", diameter + "x" + diameter + "x" + diameter),
                    RebarArgument.of("diesel-per-block", UnitFormat.MILLIBUCKETS.format(dieselPerBlock)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }

    public final ItemStackBuilder toolStack = ItemStackBuilder.gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":tool")
            .name(Component.translatable("pylon.gui.tool"));
    public ItemStackBuilder topStack = ItemStackBuilder.of(Material.YELLOW_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":top");
    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    public VirtualInventory toolInventory = new VirtualInventory(1);
    public VirtualInventory outputInventory = new VirtualInventory(3);

    @SuppressWarnings("unused")
    public DieselMiner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

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
        addEntity("top1", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.199, 0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top2", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.2, 0.6)
                        .rotate(0, Math.PI / 4, 0))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.55F);

        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
    }

    @SuppressWarnings("unused")
    public DieselMiner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        super.postInitialise();
        createLogisticGroup("tool", LogisticGroupType.INPUT, toolInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        toolInventory.addPostUpdateHandler(event -> {
            stopProcess();
            updateMiner();
        });
        outputInventory.addPostUpdateHandler(event -> updateMiner());
        updateMiner();
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
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # T # O O O # #",
                        "# # t # o o o # #",
                        "# # T # O O O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('t', toolInventory)
                .addIngredient('T', toolStack)
                .addIngredient('o', outputInventory)
                .addIngredient('O', GuiItems.output())
                .build();
    }

    @Override
    public void onProcessFinished() {
        Block block = blockPositions.get(index).getBlock();
        ItemStack tool = toolInventory.getItem(0);
        List<ItemStack> drops = block.getDrops().stream().toList();
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || !new BlockBreakBlockEvent(block, getBlock(), drops).callEvent()
                || !outputInventory.canHold(drops)
        ) {
            return;
        }

        for (ItemStack drop : drops) {
            outputInventory.addItem(new MachineUpdateReason(), drop);
        }
        block.setType(Material.AIR);
        tool.setData(DataComponentTypes.DAMAGE, tool.getData(DataComponentTypes.DAMAGE) + 1);
        if (Objects.equals(tool.getData(DataComponentTypes.DAMAGE), tool.getData(DataComponentTypes.MAX_DAMAGE))) {
            toolInventory.setItem(new MachineUpdateReason(), 0, null);
        } else {
            toolInventory.setItem(new MachineUpdateReason(), 0, tool);
        }
        removeFluid(PylonFluids.BIODIESEL, dieselPerBlock);
        updateMiner();
    }

    @Override
    protected @Nullable Integer getBreakTicks(@NotNull Block block) {
        ItemStack tool = toolInventory.getItem(0);
        if (tool == null
                || !PylonUtils.shouldBreakBlockUsingTool(block, tool)
                || !outputInventory.canHold(block.getDrops().stream().toList())
                || fluidAmount(PylonFluids.BIODIESEL) < dieselPerBlock
        ) {
            return null;
        }
        return (int) Math.round(RebarUtils.getBlockBreakTicks(tool, block) / speed);
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        RebarFluidBufferBlock.super.onFluidAdded(fluid, amount);
        updateMiner();
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBufferBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "tool", toolInventory,
                "output", outputInventory
        );
    }
}
