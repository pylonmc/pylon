package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.recipe.*;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record SmelteryMeltingPoint(@NotNull NamespacedKey key, @NotNull RebarFluid fluid, double meltingPoint) implements RebarRecipe {

    public static final RecipeType<SmelteryMeltingPoint> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("smeltery_melting_point")) {
        @Override
        protected @NotNull SmelteryMeltingPoint loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new SmelteryMeltingPoint(
                    key,
                    section.getOrThrow("fluid", ConfigAdapter.REBAR_FLUID),
                    section.getOrThrow("melting-point", ConfigAdapter.DOUBLE)
            );
        }
    };

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of();
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of();
    }

    @Override
    public @Nullable Gui display() {
        return null;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static double getMeltingPoint(@NotNull RebarFluid fluid) {
        for (SmelteryMeltingPoint point : RECIPE_TYPE) {
            if (point.fluid.equals(fluid)) {
                return point.meltingPoint();
            }
        }
        return -1;
    }
}
