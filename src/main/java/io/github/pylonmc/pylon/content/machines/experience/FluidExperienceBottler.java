package io.github.pylonmc.pylon.content.machines.experience;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidExperienceBottler extends RebarBlock implements
        RebarFluidBufferBlock,
        RebarInventoryBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarProcessor {

    private static final int XP_AMOUNT = Settings.get(PylonKeys.LIQUID_XP_BOTTLE).getOrThrow("experience-amount", ConfigAdapter.INTEGER);
    public final double bottleProductionTime = getSettings().getOrThrow("bottle-production-time-seconds", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public final RebarFluid inputFluid = getSettings().getOrThrow("input-fluid", ConfigAdapter.REBAR_FLUID);
    public final double inputFluidAmount = getSettings().getOrThrow("input-fluid-amount", ConfigAdapter.DOUBLE);

    public final @Nullable RebarFluid outputFluid = getSettings().get("output-fluid", ConfigAdapter.REBAR_FLUID);
    public final @Nullable Double outputFluidAmount = getSettings().get("output-fluid-amount", ConfigAdapter.DOUBLE);

    private final VirtualInventory bottleInput = new VirtualInventory(1);
    private final VirtualInventory bottleOutput = new VirtualInventory(1);

    private static final Vector3i FLUID_INPUT_HATCH_POS = new Vector3i(-2, -1, 0);
    private static final Vector3i EXPERIENCE_INPUT_HATCH_POS = new Vector3i(2, -1, 0);
    private static final Vector3i FLUID_OUTPUT_HATCH_POS = new Vector3i(0, -1, 2);

    public static class Item extends RebarItem {
        public final double bottleProductionTimeSeconds = getSettings().getOrThrow("bottle-production-time-seconds", ConfigAdapter.DOUBLE);
        public final double inputFluidAmount = getSettings().getOrThrow("input-fluid-amount", ConfigAdapter.DOUBLE);
        public final @Nullable Double outputFluidAmount = getSettings().get("output-fluid-amount", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            List<RebarArgument> list = new ArrayList<>();
            list.add(RebarArgument.of("time-per-bottle", UnitFormat.SECONDS.format(bottleProductionTimeSeconds)));
            list.add(RebarArgument.of("fluid-input-consumption", UnitFormat.MILLIBUCKETS_PER_ITEM.format(inputFluidAmount).decimalPlaces(1)));
            if (outputFluidAmount != null) {
                list.add(RebarArgument.of("fluid-output-production", UnitFormat.MILLIBUCKETS_PER_ITEM.format(outputFluidAmount).decimalPlaces(2)));
            }
            return list;
        }
    }

    public FluidExperienceBottler(@NotNull Block block, BlockCreateContext ctx) {
        super(block, ctx);
        setTickInterval(tickInterval);
        if (outputFluid != null) {
            Preconditions.checkNotNull(outputFluidAmount, "An output-fluid was provided, but output-fluid-amount was not.");
        }
        setFacing(ctx.getFacing());
        setMultiblockDirection(ctx.getFacing());
        setProcessProgressItem(new ProgressItem(PylonItems.LIQUID_XP_BOTTLE, false));
    }

    public FluidExperienceBottler(@NotNull Block block, PersistentDataContainer pdc) {
        super(block, pdc);
        if (outputFluid != null) {
            Preconditions.checkNotNull(outputFluidAmount, "An output-fluid was provided, but output-fluid-amount was not.");
        }
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, bottleInput);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, bottleOutput);
        bottleOutput.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBufferBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }
        if (isProcessing()) {
            progressProcess(getTickInterval());
            return;
        }
        FluidInputHatch inputHatch = getMultiblockComponent(FluidInputHatch.class, FLUID_INPUT_HATCH_POS);
        FluidOutputHatch outputHatch = getMultiblockComponent(FluidOutputHatch.class, FLUID_OUTPUT_HATCH_POS);
        FluidInputHatch xpHatch = getMultiblockComponent(FluidInputHatch.class, EXPERIENCE_INPUT_HATCH_POS);
        if (inputHatch == null || xpHatch == null) {
            return;
        }
        if (outputFluid != null && outputHatch == null) {
            return;
        }
        if (!inputHatch.hasFluid(inputFluid) || !xpHatch.hasFluid(PylonFluids.LIQUID_XP)) {
            return;
        }
        if (inputHatch.fluidAmount(inputFluid) < inputFluidAmount) {
            return;
        }
        if (xpHatch.fluidAmount(PylonFluids.LIQUID_XP) < XP_AMOUNT) {
            return;
        }
        if (bottleInput.getItem(0) == null || bottleInput.getItem(0).getType() != Material.GLASS_BOTTLE) {
            return;
        }
        RebarItem bottleOutputItem = RebarItem.fromStack(bottleOutput.getItem(0));
        if (bottleOutputItem != null && (!bottleOutputItem.getKey().equals(PylonKeys.LIQUID_XP_BOTTLE) || bottleOutputItem.getStack().getAmount() == bottleOutputItem.getStack().getMaxStackSize())) {
            return;
        }
        if (outputFluid != null && outputHatch.fluidSpaceRemaining(outputFluid) < outputFluidAmount) {
            return;
        }
        inputHatch.removeFluid(inputFluid, inputFluidAmount);
        xpHatch.removeFluid(PylonFluids.LIQUID_XP, XP_AMOUNT);
        if (outputFluid != null) {
            outputHatch.addFluid(outputFluid, outputFluidAmount);
        }
        bottleInput.setItem(new MachineUpdateReason(), 0, bottleInput.getItem(0).subtract());
        startProcess((int) Math.round(bottleProductionTime * 20));
    }

    @Override
    public void onProcessFinished() {
        bottleOutput.addItem(null, PylonItems.LIQUID_XP_BOTTLE.clone());
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        FluidInputHatch inputHatch = getMultiblockComponent(FluidInputHatch.class, FLUID_INPUT_HATCH_POS);
        FluidInputHatch xpHatch = getMultiblockComponent(FluidInputHatch.class, EXPERIENCE_INPUT_HATCH_POS);
        Preconditions.checkState(inputHatch != null && xpHatch != null);
        inputHatch.setFluidType(inputFluid);
        xpHatch.setFluidType(PylonFluids.LIQUID_XP);
        if (outputFluid != null) {
            FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUT_HATCH_POS);
            outputHatch.setFluidType(outputFluid);
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # I # # # O # #",
                        "# # i # p # o # #",
                        "# # I # # # O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('O', GuiItems.output())
                .addIngredient('i', bottleInput)
                .addIngredient('o', bottleOutput)
                .addIngredient('p', getProcessProgressItem())
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("bottleInput", bottleInput, "bottleOutput", bottleOutput);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        if (getProcessTicksRemaining() == null || getProcessTimeTicks() == null) {
            return new WailaDisplay(getNameTranslationKey());
        }
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("progressbar", PylonUtils.createProgressBar(
                        (double) (getProcessTimeTicks() - getProcessTicksRemaining()) / getProcessTimeTicks(), 20, TextColor.color(0, 255, 0)))
        ));
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<@NotNull Vector3i, @NotNull MultiblockComponent> map = new HashMap<>();
        map.put(new Vector3i(0, -1, 0), MultiblockComponent.of(Material.LAPIS_BLOCK));
        map.put(new Vector3i(-1, -1, 0), MultiblockComponent.of(Material.POLISHED_DEEPSLATE_WALL));
        map.put(new Vector3i(1, -1, 0), MultiblockComponent.of(Material.POLISHED_DEEPSLATE_WALL));
        map.put(FLUID_INPUT_HATCH_POS, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        map.put(EXPERIENCE_INPUT_HATCH_POS, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        if (outputFluid != null) {
            map.put(new Vector3i(0, -1, 1), MultiblockComponent.of(Material.POLISHED_DEEPSLATE_WALL));
            map.put(FLUID_OUTPUT_HATCH_POS, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));
        }
        return map;
    }
}
