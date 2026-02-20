package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTank;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankWithDisplayEntity;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.time.Duration;
import java.util.List;
import java.util.Map;


public class Collimator extends RebarBlock implements
        RebarFluidTank,
        RebarProcessor,
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarTickingBlock,
        RebarSimpleMultiblock,
        RebarVirtualInventoryBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int obscyraPerCohesiveUnit = getSettings().getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER);
    public final int secondsPerCohesiveUnit = getSettings().getOrThrow("seconds-per-cohesive-unit", ConfigAdapter.INTEGER);
    public final VirtualInventory inventory = new VirtualInventory(1);

    public static class Item extends RebarItem {

        public final int obscyraPerCohesiveUnit = getSettings().getOrThrow("obscyra-per-cohesive-unit", ConfigAdapter.INTEGER);
        public final int secondsPerCohesiveUnit = getSettings().getOrThrow("seconds-per-cohesive-unit", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("obscyra-per-cohesive-unit", UnitFormat.MILLIBUCKETS.format(obscyraPerCohesiveUnit)),
                    RebarArgument.of("time-per-cohesive-unit", UnitFormat.formatDuration(Duration.ofSeconds(secondsPerCohesiveUnit)))
            );
        }
    }

    public Collimator(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        setCapacity(obscyraPerCohesiveUnit);
        setMultiblockDirection(context.getFacing());
        startProcess(secondsPerCohesiveUnit * 20);
    }

    public Collimator(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return fluid.equals(PylonFluids.OBSCYRA);
    }

    @Override
    public void tick() {
        double obscyraToUse = (double) obscyraPerCohesiveUnit / (secondsPerCohesiveUnit * getTickInterval());
        if (isFormedAndFullyLoaded()
                && getFluidAmount() > obscyraToUse
                && inventory.canHold(PylonItems.COHESIVE_UNIT)
                && getBlock().getY() < 0
        ) {
            progressProcess(getTickInterval());
            removeFluid(obscyraToUse);
        }
    }

    @Override
    public void onProcessFinished() {
        inventory.addItem(new MachineUpdateReason(), PylonItems.COHESIVE_UNIT);
        startProcess(secondsPerCohesiveUnit * 20);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # x # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', inventory)
                .build();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey()
                .arguments(
                        RebarArgument.of("fluid-bar", PylonUtils.createFluidAmountBar(
                                getFluidAmount(),
                                getFluidCapacity(),
                                20,
                                TextColor.fromHexString("#000000")
                        )),
                        RebarArgument.of("time-remaining", UnitFormat.SECONDS.format(getProcessTicksRemaining() / 20))
                )
        );
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.COLLIMATOR_PILLAR),
                new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.COLLIMATOR_PILLAR),
                new Vector3i(0, 0, 1), new RebarMultiblockComponent(PylonKeys.COLLIMATOR_PILLAR)
        );
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidTank.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }
}
