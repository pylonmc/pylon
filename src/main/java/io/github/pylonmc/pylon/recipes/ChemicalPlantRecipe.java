package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.*;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record ChemicalPlantRecipe(
        @NotNull NamespacedKey key,
        @NotNull Component displayName,
        @NotNull ItemStack displayItem,
        @NotNull List<@NotNull ItemChoice> inputItems,
        @NotNull List<@NotNull FluidChoice> inputFluids,
        @NotNull List<@NotNull ItemStack> resultItems,
        @NotNull List<@NotNull FluidWithAmount> resultFluids,
        @NotNull List<@NotNull NamespacedKey> modules
) implements RebarRecipe {

    public ChemicalPlantRecipe {
        Preconditions.checkArgument(inputItems.size() <= 2, "Cannot have any more than 2 input items");
        Preconditions.checkArgument(inputFluids.size() <= 2, "Cannot have any more than 2 input fluids");
        Preconditions.checkArgument(resultItems.size() <= 2, "Cannot have any more than 2 item results");
        Preconditions.checkArgument(resultFluids.size() <= 2, "Cannot have any more than 2 fluid results");
        Preconditions.checkArgument(modules.size() <= 2, "Cannot have any more than 2 modules");
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<ChemicalPlantRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("chemical_plant")) {

        @Override
        protected @NotNull ChemicalPlantRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new ChemicalPlantRecipe(
                    key,
                    section.getOrThrow("display.name", ConfigAdapter.MINI_MESSAGE),
                    section.getOrThrow("display.item", ConfigAdapter.ITEM_STACK),
                    section.get("inputs.items", ConfigAdapter.LIST.from(ConfigAdapter.ITEM_CHOICE), Collections.emptyList()),
                    section.get("inputs.fluids", ConfigAdapter.LIST.from(ConfigAdapter.FLUID_CHOICE), Collections.emptyList()),
                    section.get("results.items", ConfigAdapter.LIST.from(ConfigAdapter.ITEM_STACK), Collections.emptyList()),
                    section.get("results.fluids", ConfigAdapter.LIST.from(ConfigAdapter.FLUID_WITH_AMOUNT), Collections.emptyList()),
                    section.get("modules", ConfigAdapter.LIST.from(ConfigAdapter.NAMESPACED_KEY), Collections.emptyList())
            );
        }
    };

    @Override
    public @NotNull List<FluidOrItemChoice> getInputs() {
        List<FluidOrItemChoice> choices = new ArrayList<>(inputFluids);
        choices.addAll(inputItems);
        return choices;
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        List<FluidOrItem> results = new ArrayList<>(resultFluids);
        results.addAll(Lists.transform(resultItems, FluidOrItem::of));
        return results;
    }

    @Override
    public @NotNull Gui display() {
        var builder = Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# 1 2 # B # а б #",
                        "# # # # A # # # #",
                        "# a b # r # А Б #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('r', ItemButton.of(PylonItems.CHEMICAL_PLANT));

        GuiItems.fillItemsSequential(builder, '1', Lists.transform(inputItems, ItemButton::of));
        GuiItems.fillItemsSequential(builder, 'a', Lists.transform(inputFluids, FluidButton::of));

        GuiItems.fillItemsSequential(builder, 'а', Lists.transform(resultItems, ItemButton::of));
        GuiItems.fillItemsSequential(builder, 'А', Lists.transform(resultFluids, FluidButton::of));

        GuiItems.fillItemsSequential(builder, 'A', Lists.transform(modules, key -> ItemButton.of(ItemTypeWrapper.of(key).createItemStack())));

        return builder.build();
    }

    public @NotNull ItemStack createDisplayItem() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("pylon.gui.outputs"));
        for (ItemStack result : resultItems) {
            lore.add(result.effectiveName());
        }
        for (FluidWithAmount result : resultFluids) {
            lore.add(result.fluid().getName());
        }
        return ItemStackBuilder.copyOf(displayItem)
                .name(displayName)
                .lore(lore)
                .build();
    }
}