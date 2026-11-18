package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.tools.electric.PrintedCircuitBoard;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record PcbConfiguration(
        @NotNull NamespacedKey key,
        @NotNull Component name,
        @NotNull ItemStack item
) implements RebarRecipe {

    public static final RecipeType<PcbConfiguration> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("pcb_configuration")) {

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void addRecipe(@NonNull PcbConfiguration recipe) {
            super.addRecipe(recipe);
            ItemStack newItem = ItemStackBuilder.rebar(Material.CLAY_BALL, recipe.getKey())
                    .set(DataComponentTypes.ITEM_MODEL, Material.GREEN_CARPET.getKey())
                    .defaultTranslatableName(PylonKeys.PRINTED_CIRCUIT_BOARD)
                    .defaultTranslatableLore(PylonKeys.PRINTED_CIRCUIT_BOARD)
                    .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                    .build();
            RebarItem.suppressNameWarnings(recipe.getKey());
            RebarItem.register(PrintedCircuitBoard.class, newItem, recipe.getKey());
        }

        @Override
        protected @NotNull PcbConfiguration loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            ItemStack item = section.getOrThrow("item", ConfigAdapter.ITEM_STACK);
            return new PcbConfiguration(
                    key,
                    section.get("name", ConfigAdapter.MINI_MESSAGE, item.effectiveName()),
                    item
            );
        }
    };

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(ItemChoice.exact(PylonItems.PRINTED_CIRCUIT_BOARD));
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(RebarRegistry.ITEMS.getOrThrow(getKey()).createNewItemStack()));
    }

    @Override
    public @Nullable Gui display() {
        return null;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
