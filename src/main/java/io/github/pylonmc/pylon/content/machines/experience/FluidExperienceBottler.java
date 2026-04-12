package io.github.pylonmc.pylon.content.machines.experience;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidExperienceBottler extends RebarBlock implements RebarFluidBufferBlock, RebarGuiBlock, RebarVirtualInventoryBlock, RebarTickingBlock, RebarLogisticBlock, RebarProcessor, RebarSimpleMultiblock, RebarDirectionalBlock {

    private static final int XP_AMOUNT = Settings.get(PylonKeys.LIQUID_XP_BOTTLE).getOrThrow("experience-amount", ConfigAdapter.INTEGER);
    public final int bottleProductionRateTicks = getSettings().getOrThrow("bottle-production-rate-ticks", ConfigAdapter.INTEGER);

    public final RebarFluid inputFluid = getSettings().getOrThrow("input-fluid", ConfigAdapter.REBAR_FLUID);
    public final double inputFluidConsumptionRate = getSettings().getOrThrow("input-fluid-consumption-rate", ConfigAdapter.DOUBLE);

    public final @Nullable RebarFluid outputFluid = getSettings().get("output-fluid", ConfigAdapter.REBAR_FLUID);
    public final @Nullable Integer fluidOutputBuffer = getSettings().get("output-fluid-buffer-size", ConfigAdapter.INTEGER);
    public final @Nullable Double fluidOutputProductionRate = getSettings().get("fluid-output-production-rate", ConfigAdapter.DOUBLE);

    private final VirtualInventory bottleInput = new VirtualInventory(1);
    private final VirtualInventory bottleOutput = new VirtualInventory(1);

    public FluidExperienceBottler(@NotNull Block block, BlockCreateContext ctx) {
        super(block, ctx);
        setTickInterval(bottleProductionRateTicks);
        if (outputFluid != null) {
            if (fluidOutputBuffer == null || fluidOutputProductionRate == null) {
                throw new ExceptionInInitializerError("An output-fluid was provided, but at least one of output-fluid-buffer, output-fluid-per-second, output-wiala-bar-length, output-waila-bar-color are missing");
            }
        }
        setFacing(ctx.getFacing());
        setMultiblockDirection(ctx.getFacing());
    }

    public FluidExperienceBottler(@NotNull Block block, PersistentDataContainer pdc) {
        super(block, pdc);
        if (outputFluid != null) {
            if (fluidOutputBuffer == null || fluidOutputProductionRate == null) {
                throw new ExceptionInInitializerError("An output-fluid was provided, but at least one of output-fluid-buffer, output-fluid-per-second, output-wiala-bar-length, output-waila-bar-color are missing");
            }
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

    public @Nullable FluidInputHatch getFluidInputHatch() {
        Vector relativeLocation = Vector.fromJOML(RebarUtils.rotateVectorToFace(new Vector3i(2, 1, 0), getFacing()));
        Location inputHatchLocation = getBlock().getLocation().add(relativeLocation);
        return BlockStorage.getAs(FluidInputHatch.class, inputHatchLocation);
    }

    public @Nullable FluidInputHatch getExperienceInputHatch() {
        Vector relativeLocation = Vector.fromJOML(RebarUtils.rotateVectorToFace(new Vector3i(0, 2, 0), getFacing()));
        Location hatchLoc = getBlock().getLocation().add(relativeLocation);
        return BlockStorage.getAs(FluidInputHatch.class, hatchLoc);
    }

    public @Nullable FluidOutputHatch getFluidOutputHatch() {
        Vector relativeLocation = Vector.fromJOML(RebarUtils.rotateVectorToFace(new Vector3i(-2, 1, 0), getFacing()));
        Location inputHatchLocation = getBlock().getLocation().add(relativeLocation);
        return BlockStorage.getAs(FluidOutputHatch.class, inputHatchLocation);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }
        FluidInputHatch inputHatch = getFluidInputHatch();
        FluidOutputHatch outputHatch = getFluidOutputHatch();
        FluidInputHatch xpHatch = getExperienceInputHatch();
        if (inputHatch == null || xpHatch == null) {
            return;
        }
        if(outputFluid != null && outputHatch == null){
            return;
        }
        if (!inputHatch.hasFluid(inputFluid) || !xpHatch.hasFluid(PylonFluids.LIQUID_XP)) {
            return;
        }
        if (inputHatch.fluidAmount(inputFluid) < inputFluidConsumptionRate * bottleProductionRateTicks / 20) {
            return;
        }
        if(xpHatch.fluidAmount(PylonFluids.LIQUID_XP) < XP_AMOUNT){
            return;
        }
        if (bottleInput.getItem(0) == null || bottleInput.getItem(0).getType() != Material.GLASS_BOTTLE) {
            return;
        }
        RebarItem bottleOutputItem = RebarItem.fromStack(bottleOutput.getItem(0));
        if (bottleOutputItem != null && (!bottleOutputItem.getKey().equals(PylonKeys.LIQUID_XP_BOTTLE) || bottleOutputItem.getStack().getAmount() == bottleOutputItem.getStack().getMaxStackSize())) {
            return;
        }
        if (outputFluid != null && outputHatch.fluidSpaceRemaining(outputFluid) < fluidOutputProductionRate * bottleProductionRateTicks / 20) {
            return;
        }
        inputHatch.removeFluid(inputFluid, inputFluidConsumptionRate);
        xpHatch.removeFluid(PylonFluids.LIQUID_XP, XP_AMOUNT);
        if (outputFluid != null) {
            outputHatch.addFluid(outputFluid, fluidOutputProductionRate * bottleProductionRateTicks / 20);
        }
        bottleInput.setItem(new MachineUpdateReason(), 0, bottleInput.getItem(0).subtract());
        bottleOutput.addItem(null, PylonItems.LIQUID_XP_BOTTLE.clone());
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        FluidInputHatch inputHatch = getFluidInputHatch();
        FluidInputHatch xpHatch = getExperienceInputHatch();
        Preconditions.checkState(inputHatch != null && xpHatch != null);
        inputHatch.setFluidType(inputFluid);
        xpHatch.setFluidType(PylonFluids.LIQUID_XP);
        if(outputFluid != null){
            FluidOutputHatch outputHatch = getFluidOutputHatch();
            Preconditions.checkState(outputHatch != null);
            outputHatch.setFluidType(outputFluid);
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);
        FluidInputHatch inputHatch = getFluidInputHatch();
        if (inputHatch != null) {
            inputHatch.setFluidType(null);
        }
        FluidOutputHatch outputHatch = getFluidOutputHatch();
        if (outputHatch != null) {
            outputHatch.setFluidType(null);
        }
        FluidInputHatch xpHatch = getExperienceInputHatch();
        if(xpHatch != null){
            xpHatch.setFluidType(null);
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # I # # # O # #",
                        "# # i # # # o # #",
                        "# # I # # # O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('O', GuiItems.output())
                .addIngredient('i', bottleInput)
                .addIngredient('o', bottleOutput)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("bottleInput", bottleInput, "bottleOutput", bottleOutput);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        FluidInputHatch xpInputHatch = getExperienceInputHatch();
        if(xpInputHatch != null && xpInputHatch.hasFluid(PylonFluids.LIQUID_XP)) {
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(RebarArgument.of("xpbar", PylonUtils.createFluidAmountBar(
                    xpInputHatch.fluidAmount(PylonFluids.LIQUID_XP),
                    xpInputHatch.fluidCapacity(PylonFluids.LIQUID_XP),
                    20,
                    TextColor.fromHexString("#1dc420")
            ))));
        } else {
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(RebarArgument.of("xpbar", PylonUtils.createFluidAmountBar(
                    0,
                    0,
                    20,
                    TextColor.fromHexString("#1dc420")
            ))));
        }
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<@NotNull Vector3i, @NotNull MultiblockComponent> map = new HashMap<>();
        map.put(new Vector3i(0, 1, 0), new RebarSimpleMultiblock.VanillaMultiblockComponent(Material.IRON_BARS));
        map.put(new Vector3i(0, 2, 0), new RebarSimpleMultiblock.RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        map.put(new Vector3i(1, 1, 0), new RebarSimpleMultiblock.VanillaMultiblockComponent(Material.IRON_BARS));
        map.put(new Vector3i(1, 0, 0), new RebarSimpleMultiblock.VanillaMultiblockComponent(Material.IRON_BARS));
        map.put(new Vector3i(2, 1, 0), new RebarSimpleMultiblock.RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        if(outputFluid != null){
        map.put(new Vector3i(-1, 1, 0), new RebarSimpleMultiblock.VanillaMultiblockComponent(Material.IRON_BARS));
        map.put(new Vector3i(-1, 0, 0), new RebarSimpleMultiblock.VanillaMultiblockComponent(Material.IRON_BARS));
        map.put(new Vector3i(-2, 1, 0), new RebarSimpleMultiblock.RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        }
        return map;
    }

    public static class Item extends RebarItem {
        public final int bottleProductionRateTicks = getSettings().getOrThrow("bottle-production-rate-ticks", ConfigAdapter.INTEGER);
        public final double inputFluidConsumptionRate = getSettings().getOrThrow("input-fluid-consumption-rate", ConfigAdapter.DOUBLE);
        public final @Nullable Double fluidOutputProductionRate = getSettings().get("fluid-output-production-rate", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            List<RebarArgument> list = new ArrayList<>();
            list.add(RebarArgument.of("production-rate", UnitFormat.ITEMS_PER_SECOND.format((double) bottleProductionRateTicks / 20)));
            list.add(RebarArgument.of("fluid-input-consumption", UnitFormat.MILLIBUCKETS_PER_SECOND.format(inputFluidConsumptionRate)));
            if (fluidOutputProductionRate != null) {
                list.add(RebarArgument.of("fluid-output-production", UnitFormat.MILLIBUCKETS_PER_SECOND.format(fluidOutputProductionRate)));
            }
            return list;
        }
    }
}
