package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.recipes.MixingPotRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.FluidWithAmount;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class ElectricMixer extends RebarBlock implements
        RecipeProcessorRebarBlock<MixingPotRecipe>,
        TickingRebarBlock,
        GuiRebarBlock,
        LogisticRebarBlock,
        DirectionalRebarBlock,
        VirtualInventoryRebarBlock,
        SimpleElectricRebarBlock,
        FluidRebarBlock {

    private static final ItemStackBuilder MIXING_ITEM = ItemStackBuilder.of(Material.CAULDRON)
            .name(Component.translatable("pylon.gui.mixing"));

    private final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
    private final double mixingTime = getSettingOrThrow("mixing-time", ConfigAdapter.DOUBLE);
    private final double inputCapacity = getSettingOrThrow("input-capacity", ConfigAdapter.DOUBLE);
    private final double outputCapacity = getSettingOrThrow("output-capacity", ConfigAdapter.DOUBLE);

    private static final NamespacedKey INPUT_FLUID_KEY = pylonKey("input_fluid");
    private static final NamespacedKey OUTPUT_FLUID_KEY = pylonKey("output_fluid");
    private @Nullable FluidWithAmount inputFluid = null;
    private @Nullable FluidWithAmount outputFluid = null;

    private final VirtualInventory inputInventory = new VirtualInventory(9);
    private final VirtualInventory outputInventory = new VirtualInventory(3);

    public static class Item extends RebarItem {
        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double mixingTime = getSettingOrThrow("mixing-time", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage)),
                    RebarArgument.of("mixing-time", UnitFormat.SECONDS.format(mixingTime))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricMixer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacingVertical());
        setTickInterval(tickInterval);
        setRecipeType(MixingPotRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
        createFluidPoint(FluidPointType.INPUT, BlockFace.EAST, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.WEST, context, false);
        createSimpleElectricPort(NodeType.CONSUMER, getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricMixer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        inputFluid = pdc.get(INPUT_FLUID_KEY, RebarSerializers.FLUID_WITH_AMOUNT);
        outputFluid = pdc.get(OUTPUT_FLUID_KEY, RebarSerializers.FLUID_WITH_AMOUNT);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, INPUT_FLUID_KEY, RebarSerializers.FLUID_WITH_AMOUNT, inputFluid);
        RebarUtils.setNullable(pdc, OUTPUT_FLUID_KEY, RebarSerializers.FLUID_WITH_AMOUNT, outputFluid);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        outputInventory.addPostUpdateHandler(_ -> tryStartRecipe());
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# I I I # # # # #",
                        "# i i i # O O O #",
                        "# i i i p o o o #",
                        "# i i i # O O O #",
                        "# I I I # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "output", outputInventory
        );
    }

    private void tryStartRecipe() {
        if (isProcessingRecipe()) return;

        if (inputFluid == null) return;

        List<ItemStack> items = Arrays.stream(inputInventory.getItems()).filter(Objects::nonNull).toList();
        if (items.isEmpty()) return;

        if (getLastRecipe() != null && tryStartRecipe(getLastRecipe(), items)) return;

        for (MixingPotRecipe recipe : MixingPotRecipe.RECIPE_TYPE) {
            if (tryStartRecipe(recipe, items)) {
                return;
            }
        }
    }

    private boolean tryStartRecipe(MixingPotRecipe recipe, List<ItemStack> items) {
        if (!recipe.matches(items, true, inputFluid.fluid(), inputFluid.millibuckets())) {
            return false;
        }

        switch (recipe.output()) {
            case FluidOrItem.Fluid fluidOutput -> {
                if (outputFluid != null && (!outputFluid.fluid().equals(fluidOutput.fluid()) || outputFluid.millibuckets() + fluidOutput.amountMillibuckets() > outputCapacity)) {
                    return false;
                }
            }
            case FluidOrItem.Item itemOutput -> {
                if (!outputInventory.canHold(List.of(itemOutput.item()))) {
                    return false;
                }
            }
            default -> throw new AssertionError();
        }

        startRecipe(recipe, (int) Math.ceil(mixingTime * 20));
        getRecipeProgressItem().setItem(MIXING_ITEM);
        for (RecipeInput.Item choice : recipe.inputItems()) {
            for (int i = 0; i < inputInventory.getSize(); i++) {
                ItemStack stack = inputInventory.getItem(i);
                if (stack == null) continue;
                if (choice.matches(stack)) {
                    inputInventory.setItem(new MachineUpdateReason(), i, stack.subtract(choice.getAmount()));
                    break;
                }
            }
        }
        inputFluid = inputFluid.subtractMillibuckets(recipe.inputFluid().amountMillibuckets());

        return true;
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(getTickInterval());
    }

    @Override
    public void onRecipeFinished(@NonNull MixingPotRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        switch (recipe.output()) {
            case FluidOrItem.Fluid fluidOutput -> {
                if (outputFluid == null) {
                    outputFluid = new FluidWithAmount(fluidOutput.fluid(), fluidOutput.amountMillibuckets());
                } else {
                    outputFluid = outputFluid.addMillibuckets(fluidOutput.amountMillibuckets());
                }
            }
            case FluidOrItem.Item itemOutput -> outputInventory.addItem(new MachineUpdateReason(), itemOutput.item());
            default -> throw new AssertionError();
        }
    }

    @Override
    public void onBlockBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
        FluidRebarBlock.super.onBlockBreak(drops, context);
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        outputFluid = outputFluid.subtractMillibuckets(amount);
        if (outputFluid.millibuckets() <= RebarUtils.FLUID_EPSILON) {
            outputFluid = null;
        }
        tryStartRecipe();
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        if (inputFluid == null) {
            inputFluid = new FluidWithAmount(fluid, amount);
        } else {
            inputFluid = inputFluid.addMillibuckets(amount);
        }
        tryStartRecipe();
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (inputFluid != null) {
            if (!inputFluid.fluid().equals(fluid)) {
                return 0;
            } else {
                return inputCapacity - inputFluid.millibuckets();
            }
        }

        for (MixingPotRecipe recipe : MixingPotRecipe.RECIPE_TYPE) {
            if (recipe.inputFluid().contains(fluid)) {
                return inputCapacity;
            }
        }

        return 0;
    }

    @Override
    public @NotNull List<@NotNull Pair<@NotNull RebarFluid, @NotNull Double>> getSuppliedFluids() {
        if (outputFluid == null) {
            return List.of();
        } else {
            return List.of(new Pair<>(outputFluid.fluid(), outputFluid.millibuckets()));
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContentsWithName(
                        inputFluid == null ? null : inputFluid.fluid(),
                        inputCapacity,
                        inputFluid == null ? 0 : inputFluid.millibuckets()
                ))
                .add(ProgressBar.fluidContentsWithName(
                        outputFluid == null ? null : outputFluid.fluid(),
                        outputCapacity,
                        outputFluid == null ? 0 : outputFluid.millibuckets()
                ));
    }
}