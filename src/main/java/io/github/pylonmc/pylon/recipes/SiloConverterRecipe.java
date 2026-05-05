package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.storage.Silo;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public record SiloConverterRecipe(
        NamespacedKey key,
        RecipeInput.Item material,
        ItemStack result
) implements RebarRecipe {

    public static final RecipeType<SiloConverterRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("silo_converter")) {
        @Override
        protected @NotNull SiloConverterRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new SiloConverterRecipe(
                    key,
                    section.getOrThrow("material", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK)
            );
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return List.of(material);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        List<ItemStack> silos = RebarRegistry.ITEMS.getValues()
                .stream()
                .map(RebarItemSchema::createNewItem)
                .filter(item -> RebarItem.fromStack(item) instanceof Silo.Item)
                .filter(item -> !item.isSimilar(result))
                .toList();

        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # i m # c # o #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', new ItemButton(silos))
                .addIngredient('m', new ItemButton(material.getRepresentativeItem()))
                .addIngredient('c', new ItemButton(PylonItems.SILO_CONVERTER))
                .addIngredient('o', new ItemButton(result))
                .build();
    }
}
