package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.Structure;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public record AssemblingRecipe(
        NamespacedKey key,
        List<RecipeInput.Item> inputs,
        List<ItemStack> results,
        List<Step> steps
) implements RebarRecipe {

    public record AddDisplay(
            String name,
            ItemStack stack,
            Vector2d position,
            Vector3d scale
    ) {}

    public record Step(
            String tool,
            ItemStack icon,
            int clicks,
            List<AddDisplay> addDisplays,
            List<String> removeDisplays
    ) {}

    public static final RecipeType<AssemblingRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("assembling")) {
        @Override
        protected @NotNull AssemblingRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            List<Step> steps = new ArrayList<>();
            for (ConfigSection stepSection : section.get("steps", ConfigAdapter.LIST.from(ConfigAdapter.CONFIG_SECTION))) {

                List<AddDisplay> addDisplays = new ArrayList<>();
                List<ConfigSection> addDisplaysSection = stepSection.get("add-displays", ConfigAdapter.LIST.from(ConfigAdapter.CONFIG_SECTION));
                if (addDisplaysSection != null) {
                    for (ConfigSection addDisplaySection : addDisplaysSection) {
                        addDisplays.add(new AddDisplay(
                                addDisplaySection.getOrThrow("name", ConfigAdapter.STRING),
                                addDisplaySection.getOrThrow("item", ConfigAdapter.ITEM_STACK),
                                addDisplaySection.getOrThrow("position", ConfigAdapter.VECTOR_2D),
                                addDisplaySection.getOrThrow("scale", ConfigAdapter.VECTOR_3D)
                        ));
                    }
                }

                steps.add(new Step(
                        stepSection.getOrThrow("tool", ConfigAdapter.STRING),
                        stepSection.getOrThrow("icon", ConfigAdapter.ITEM_STACK),
                        stepSection.getOrThrow("clicks", ConfigAdapter.INT),
                        addDisplays,
                        stepSection.get("remove_displays", ConfigAdapter.LIST.from(ConfigAdapter.STRING), new ArrayList<>())
                ));
            }

            List<RecipeInput.Item> inputs = section.getOrThrow("inputs", ConfigAdapter.LIST.from(ConfigAdapter.RECIPE_INPUT_ITEM));
            List<ItemStack> results = section.getOrThrow("results", ConfigAdapter.LIST.from(ConfigAdapter.ITEM_STACK));

            Preconditions.checkArgument(
                    inputs.size() <= 6,
                    "Assembling recipes can have at most 6 inputs - " + key
            );
            Preconditions.checkArgument(
                    results.size() <= 6,
                    "Assembling recipes can have at most 6 results - " + key
            );
            Preconditions.checkArgument(
                    steps.size() <= 9,
                    "Assembling recipes can have at most 9 steps - " + key
            );

            return new AssemblingRecipe(key, inputs, results, steps);
        }
    };

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull RecipeInput> getInputs() {
        return new ArrayList<>(inputs);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
            return results.stream()
                    .map(FluidOrItem::of)
                    .toList();
    }

    public static @Nullable AssemblingRecipe findRecipe(ItemStack[] items) {
        for (AssemblingRecipe recipe : RECIPE_TYPE.getRecipes()) {
            boolean hasAllIngredients = true;
            for (RecipeInput.Item requiredItem : recipe.inputs) {
                boolean hasIngredient = false;
                for (ItemStack item : items) {
                    if (item != null && requiredItem.matches(item)) {
                        hasIngredient = true;
                        break;
                    }
                }

                if (!hasIngredient) {
                    hasAllIngredients = false;
                }
            }

            if (hasAllIngredients) {
                return recipe;
            }
        }

        return null;
    }

    @Override
    public @Nullable Gui display() {
        List<Item> stepItems = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            if (step != null) {
                ItemStack stack = ItemStackBuilder.of(step.icon)
                        .name(Component.translatable("pylon.gui.assembly_table.step").arguments(
                                RebarArgument.of("step_index", i+1),
                                RebarArgument.of("tool", Component.translatable("pylon.gui.assembly_table.tools." + step.tool)),
                                RebarArgument.of("clicks", step.clicks)
                        ))
                        .clearLore()
                        .build();
                stepItems.add(Item.simple(stack));
            }
        }

        Gui gui = Gui.builder()
                .setStructure(
                        "I i i I # O o o O",
                        "I i i I t O o o O",
                        "I i i I # O o o O",
                        "# # # # # # # # #",
                        "x x x x x x x x x"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('O', GuiItems.output())
                .addIngredient('t', PylonItems.ASSEMBLY_TABLE)
                .addIngredient('x', PagedGui.itemsBuilder()
                        .setStructure("< x x x x x x x >")
                        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                        .addIngredient('<', GuiItems.pagePrevious())
                        .addIngredient('>', GuiItems.pageNext())
                        .setContent(stepItems)
                        .build()
                )
                .build();

        for (int i = 0; i < inputs.size(); i++) {
            RecipeInput.Item input = inputs.get(i);
            if (input != null) {
                gui.setItem(1 + i % 2, Math.floorDiv(i, 2), new ItemButton(input.getRepresentativeItem()));
            }
        }

        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (result != null) {
                gui.setItem(6 + i % 2, Math.floorDiv(i, 2), new ItemButton(result));
            }
        }

        return gui;
    }
}
