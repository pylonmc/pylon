package io.github.pylonmc.pylon.content.machines.fluid;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.recipes.StrainingRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
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
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class FluidStrainer extends RebarBlock implements
        RebarDirectionalBlock,
        RebarFluidBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<StrainingRecipe> {

    private static final NamespacedKey FLUID_AMOUNT_KEY = pylonKey("fluid_amount");
    private static final NamespacedKey FLUID_TYPE_KEY = pylonKey("fluid_type");

    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);
    public @Nullable RebarFluid fluidType;
    public double fluidAmount;
    private final VirtualInventory inventory = new VirtualInventory(9 * 3);

    public static class Item extends RebarItem {

        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public FluidStrainer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.UP);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.DOWN);
        setRecipeType(StrainingRecipe.RECIPE_TYPE);

        fluidType = null;
        fluidAmount = 0.0;
    }

    @SuppressWarnings("unused")
    public FluidStrainer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        fluidType = null;
        fluidAmount = pdc.get(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("inventory", LogisticGroupType.OUTPUT, inventory);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, FLUID_TYPE_KEY, RebarSerializers.REBAR_FLUID, fluidType);
        pdc.set(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, fluidAmount);
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (getCurrentRecipe() == null) {
            if (StrainingRecipe.getRecipeForFluid(fluid) == null) {
                return 0.0;
            } else {
                return 1000.0 - fluidAmount;
            }
        }
        if (getCurrentRecipe().input().contains(fluid)) {
            return getCurrentRecipe().input().amountMillibuckets() - fluidAmount;
        }
        return 0;
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        if (!isProcessingRecipe()) {
            StrainingRecipe recipe = StrainingRecipe.getRecipeForFluid(fluid);
            Preconditions.checkState(recipe != null);
            startRecipe(recipe, (int) Math.round(recipe.input().amountMillibuckets()));
            fluidType = recipe.outputFluid();
        }
        if (isProcessingRecipe()) {
            progressRecipe((int) Math.round(amount));
        }
        fluidAmount += amount;
    }

    @Override
    public @NotNull List<Pair<RebarFluid, Double>> getSuppliedFluids() {
        return fluidType != null
                ? List.of(new Pair<>(fluidType, fluidAmount))
                : List.of();
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        fluidAmount -= amount;
        if (fluidAmount < RebarUtils.FLUID_EPSILON) {
            fluidType = null;
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("info", getCurrentRecipe() == null
                        ? Component.empty()
                        : Component.translatable("pylon.waila.fluid_strainer.straining",
                                RebarArgument.of("item", getCurrentRecipe().outputItem().effectiveName()),
                                RebarArgument.of("bars", PylonUtils.createProgressBar(
                                        getCurrentRecipe().input().amountMillibuckets() - getRecipeTicksRemaining(),
                                        getCurrentRecipe().input().amountMillibuckets(),
                                        20,
                                        TextColor.color(100, 255, 100)
                                )),
                                RebarArgument.of("progress", 100.0 * getRecipeTicksRemaining() / getCurrentRecipe().input().amountMillibuckets())
                        )
                )
        ));
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # . . . . . # #")
                .addIngredient('.', inventory)
                .addIngredient('#', GuiItems.background())
                .build();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    @Override
    public void onRecipeFinished(@NotNull StrainingRecipe recipe) {
        inventory.addItem(new MachineUpdateReason(), recipe.outputItem());
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }
}