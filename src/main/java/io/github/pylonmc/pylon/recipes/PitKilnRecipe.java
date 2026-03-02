package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record PitKilnRecipe(
        @NotNull NamespacedKey key,
        @NotNull List<RecipeInput.Item> input,
        @NotNull List<ItemStack> output
) implements RebarRecipe {

    public static final RecipeType<PitKilnRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("pit_kiln")) {
        @Override
        protected @NotNull PitKilnRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new PitKilnRecipe(
                    key,
                    section.getOrThrow("inputs", ConfigAdapter.LIST.from(ConfigAdapter.RECIPE_INPUT_ITEM)),
                    section.getOrThrow("outputs", ConfigAdapter.LIST.from(ConfigAdapter.ITEM_STACK))
            );
        }
    };

    public PitKilnRecipe {
        Preconditions.checkArgument(!input.isEmpty(), "Input cannot be empty");
        Preconditions.checkArgument(!output.isEmpty(), "Output cannot be empty");
        Preconditions.checkArgument(output.size() <= input.size(), "Output cannot exceed input size");
    }

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        return new ArrayList<>(input);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return output.stream().map(FluidOrItem::of).toList();
    }

    @Override
    public @NotNull Gui display() {
        var gui = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# 0 1 # # # a b #",
                        "# 2 3 # p # c d #",
                        "# 4 5 # # # e f #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('p', ItemButton.from(PylonItems.PIT_KILN));
        for (int i = 0; i < 6; i++) {
            if (i >= input().size()) break;
            gui.addIngredient((char) ('0' + i), ItemButton.from(input.get(i)));
        }
        for (int i = 0; i < 6; i++) {
            if (i >= output().size()) break;
            gui.addIngredient((char) ('a' + i), ItemButton.from(output.get(i)));
        }
        return gui.build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
