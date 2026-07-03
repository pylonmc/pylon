package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.recipes.HeatExchangerRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.recipe.ingredient.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidWithAmount;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class HeatExchanger extends RebarBlock implements
        SimpleRebarMultiblock,
        TickingRebarBlock {

    private final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public HeatExchanger(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public HeatExchanger(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        FluidInputHatch inputHatch1 = getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH_1);
        FluidInputHatch inputHatch2 = getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH_2);
        FluidOutputHatch outputHatch1 = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH_1);
        FluidOutputHatch outputHatch2 = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH_2);
        FluidInputHatch fromInputHatch = null;
        FluidOutputHatch fromOutputHatch = null;
        FluidInputHatch toInputHatch = null;
        FluidOutputHatch toOutputHatch = null;
        HeatExchangerRecipe matchingRecipe = null;
        for (HeatExchangerRecipe recipe : HeatExchangerRecipe.RECIPE_TYPE) {
            if (recipe.transferFrom().getFirst().matchesIgnoringAmount(inputHatch1.getFluid())) {
                fromInputHatch = inputHatch1;
                fromOutputHatch = outputHatch1;
                toInputHatch = inputHatch2;
                toOutputHatch = outputHatch2;
                matchingRecipe = recipe;
                break;
            } else if (recipe.transferFrom().getFirst().matchesIgnoringAmount(inputHatch2.getFluid())) {
                fromInputHatch = inputHatch2;
                fromOutputHatch = outputHatch2;
                toInputHatch = inputHatch1;
                toOutputHatch = outputHatch1;
                matchingRecipe = recipe;
                break;
            }
        }

        if (matchingRecipe == null) return;

        double recipeRatio = 1;

        FluidWithAmount fromOutput = matchingRecipe.transferFrom().getSecond();
        if (fromOutput != null) {
            if (!fromOutputHatch.canAcceptFluid(fromOutput.fluid())) return;
            double outputAmount = fromOutput.amount();
            double actualOutputAmount = Math.min(outputAmount, fromOutputHatch.getFluidSpaceRemaining());
            recipeRatio = Math.min(recipeRatio, actualOutputAmount / outputAmount);
        }
        FluidWithAmount toOutput = matchingRecipe.transferTo().getSecond();
        if (toOutput != null) {
            if (!toOutputHatch.canAcceptFluid(toOutput.fluid())) return;
            double outputAmount = toOutput.amount();
            double actualOutputAmount = Math.min(outputAmount, toOutputHatch.getFluidSpaceRemaining());
            recipeRatio = Math.min(recipeRatio, actualOutputAmount / outputAmount);
        }

        FluidChoice fromInput = matchingRecipe.transferFrom().getFirst();
        if (!fromInput.matchesIgnoringAmount(fromInputHatch.getFluid())) return;
        double fromInputAmount = fromInput.getAmount();
        double actualFromInputAmount = Math.min(fromInputAmount, fromInputHatch.getFluidAmount());
        recipeRatio = Math.min(recipeRatio, actualFromInputAmount / fromInputAmount);

        FluidChoice toInput = matchingRecipe.transferTo().getFirst();
        if (!toInput.matchesIgnoringAmount(toInputHatch.getFluid())) return;
        double toInputAmount = toInput.getAmount();
        double actualToInputAmount = Math.min(toInputAmount, toInputHatch.getFluidAmount());
        recipeRatio = Math.min(recipeRatio, actualToInputAmount / toInputAmount);

        recipeRatio /= getTicksPerSecond(); // Convert from per-second to per-tick

        fromInputHatch.removeFluid(fromInputAmount * recipeRatio);
        toInputHatch.removeFluid(toInputAmount * recipeRatio);
        if (fromOutput != null) {
            fromOutputHatch.addFluid(fromOutput.fluid(), fromOutput.amount() * recipeRatio);
        }
        if (toOutput != null) {
            toOutputHatch.addFluid(toOutput.fluid(), toOutput.amount() * recipeRatio);
        }
    }

    private static final Vector3i INPUT_HATCH_1 = new Vector3i(1, 0, 0);
    private static final Vector3i OUTPUT_HATCH_1 = new Vector3i(-1, 0, 0);
    private static final Vector3i INPUT_HATCH_2 = new Vector3i(0, 0, -1);
    private static final Vector3i OUTPUT_HATCH_2 = new Vector3i(0, 0, 1);

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();

        Set<RebarFluid> allowedInputs = HeatExchangerRecipe.RECIPE_TYPE.stream()
                .flatMap(recipe -> Stream.of(recipe.transferFrom().getFirst(), recipe.transferTo().getFirst()))
                .flatMap(input -> input.getFluids().stream())
                .collect(Collectors.toSet());
        getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH_1).setAllowedFluids(allowedInputs);
        getMultiblockComponentOrThrow(FluidInputHatch.class, INPUT_HATCH_2).setAllowedFluids(allowedInputs);

        Set<RebarFluid> allowedOutputs = HeatExchangerRecipe.RECIPE_TYPE.stream()
                .flatMap(recipe -> Stream.of(recipe.transferFrom().getSecond(), recipe.transferTo().getSecond()))
                .filter(Objects::nonNull)
                .map(FluidWithAmount::fluid)
                .collect(Collectors.toSet());
        getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH_1).setAllowedFluids(allowedOutputs);
        getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH_2).setAllowedFluids(allowedOutputs);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                INPUT_HATCH_1, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                OUTPUT_HATCH_1, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH),
                INPUT_HATCH_2, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                OUTPUT_HATCH_2, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH)
        );
    }
}