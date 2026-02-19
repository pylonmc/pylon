package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public record MoldingRecipe(
        NamespacedKey key,
        ItemStack input,
        ItemStack result,
        int moldingCycles
) implements RebarRecipe {

    public static final RecipeType<MoldingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("molding")) {
        @Override
        protected @NotNull MoldingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new MoldingRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
                            .asQuantity(section.getOrThrow("resultAmount", ConfigAdapter.INTEGER)),
                    section.getOrThrow("cycles", ConfigAdapter.INTEGER)
            );
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(RecipeInput.of(input));
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # i # m # o # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', new ItemButton(input))
                .addIngredient('m', new ItemButton(ItemStackBuilder.of(PylonItems.BRICK_MOLD)
                        .clearLore()
                        .lore(Component.translatable("pylon.guide.recipe.molding")
                                .arguments(RebarArgument.of("molding-cycles", moldingCycles)))
                        .build()
                ))
                .addIngredient('o', new ItemButton(result))
                .build();
    }
}
