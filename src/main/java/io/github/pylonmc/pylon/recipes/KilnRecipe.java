package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.eclipse.sisu.Nullable;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


/**
 * @param input1 the first input item (respects amount)
 * @param input2 the second input item (respects amount)
 * @param result the output item (respects amount)
 * @param timeTicks the recipe time in ticks
 */
public record KilnRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input1,
        @Nullable RecipeInput.Item input2,
        @Nullable ItemStack outputItem,
        @Nullable RebarFluid outputFluid,
        @Nullable Double outputFluidAmount,
        int timeTicks,
        double temperature
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<KilnRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("kiln")) {
        @Override
        protected @NotNull KilnRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {

            RebarFluid outputFluid = section.get("output-fluid", ConfigAdapter.REBAR_FLUID);
            Double outputFluidAmount = section.get("output-fluid-amount", ConfigAdapter.DOUBLE);
            Preconditions.checkState((outputFluid == null) == (outputFluidAmount == null), "Either none or both of output-fluid and output-fluid-amount should be set");
            return new KilnRecipe(
                    key,
                    section.getOrThrow("input1", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.get("input2", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.get("output-item", ConfigAdapter.ITEM_STACK),
                    outputFluid,
                    outputFluidAmount,
                    section.getOrThrow("time-ticks", ConfigAdapter.INTEGER),
                    section.getOrThrow("temperature", ConfigAdapter.DOUBLE)
            );
        }
    };

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        List<RecipeInput> inputs = new ArrayList<>();
        inputs.add(input1);
        if (input2 != null) {
            inputs.add(input2);
        }
        return inputs;
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        List<FluidOrItem> inputs = new ArrayList<>();
        if (outputItem != null) {
            inputs.add(FluidOrItem.of(outputItem));
        }
        if (outputFluid != null) {
            inputs.add(FluidOrItem.of(outputFluid, outputFluidAmount));
        }
        return inputs;
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # d # # # #",
                        "# i j # b # o p #",
                        "# # # # t # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', ItemButton.of(input1))
                .addIngredient('j', ItemButton.of(input2))
                .addIngredient('b', PylonItems.KILN)
                .addIngredient('o', ItemButton.of(outputItem))
                .addIngredient('p', new FluidButton(outputFluidAmount, outputFluid))
                .addIngredient('d', GuiItems.progressCyclingItem(timeTicks, ItemStackBuilder.of(Material.CLOCK)
                        .name(Component.translatable(
                                "pylon.guide.recipe.kiln",
                                RebarArgument.of("time", UnitFormat.SECONDS.format(timeTicks / 20))
                        ))
                ))
                .addIngredient('t', GuiItems.progressCyclingItem(timeTicks, ItemStackBuilder.of(Material.REDSTONE)
                        .name(Component.translatable(
                                "pylon.guide.recipe.temperature",
                                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature))
                        ))
                ))
                .build();
    }
}