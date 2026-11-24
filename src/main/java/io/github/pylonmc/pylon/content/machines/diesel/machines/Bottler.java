package io.github.pylonmc.pylon.content.machines.diesel.machines;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.machines.generic.GenericMachine;
import io.github.pylonmc.pylon.recipes.BottlingRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.NoJobRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

public class Bottler extends GenericMachine<BottlingRecipe> implements
        SimpleRebarMultiblock,
        NoJobRebarBlock {

    @Getter
    private final double dieselPerSecond = getSettingOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        @Getter
        private final double dieselPerSecond = getSettingOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("diesel", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselPerSecond)));
        }
    }

    @SuppressWarnings("unused")
    public Bottler(@NotNull Block block, BlockCreateContext ctx) {
        super(block, ctx);
        setRecipeType(BottlingRecipe.RECIPE_TYPE);
    }

    @SuppressWarnings("unused")
    public Bottler(@NotNull Block block, PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull BottlingRecipe recipe) {
        return recipe.duration();
    }

    @Override
    protected @NotNull List<@NotNull ItemStack> getResults(@NonNull BottlingRecipe recipe) {
        return List.of(recipe.result());
    }

    @Override
    protected boolean tryStartRecipe(BottlingRecipe recipe, ItemStack stack) {
        FluidInputHatch fluidHatch = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_HATCH);
        if (!recipe.inputFluid().matches(fluidHatch.getFluidWithAmount())) return false;
        boolean started = super.tryStartRecipe(recipe, stack);
        if (started) {
            fluidHatch.removeFluid(recipe.inputFluid().getAmount());
        }
        return started;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded() || !isProcessingRecipe()) return;
        if (!getMultiblockComponentOrThrow(FluidInputHatch.class, DIESEL_HATCH).removeFluid(dieselPerSecond / getTicksPerSecond())) return;
        progressRecipe(getTickInterval());
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (isProcessingRecipe()) {
            display.add(ProgressBar.recipeProgress(1.0 - getRecipeProgress()));
        }
        return display;
    }

    private static final Vector3i DIESEL_HATCH = new Vector3i(1, -1, 0);
    private static final Vector3i FLUID_HATCH = new Vector3i(-1, -1, 0);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                DIESEL_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                FLUID_HATCH, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH),
                new Vector3i(0, -1, 0), MultiblockComponent.of(Material.IRON_BLOCK)
        );
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        getMultiblockComponentOrThrow(FluidInputHatch.class, DIESEL_HATCH).setAllowedFluid(PylonFluids.BIODIESEL);
        getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_HATCH).setAllowedFluids(RebarRegistry.FLUIDS.stream().toList());
    }
}
