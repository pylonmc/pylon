package io.github.pylonmc.pylon.util;

import com.google.common.base.Preconditions;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

@Accessors(fluent = true)
@Getter
@Setter
@RequiredArgsConstructor
public class RecipeSelectionGui<T extends RebarRecipe> {

    private final @NotNull RecipeType<T> recipeType;

    @SuppressWarnings("DataFlowIssue")
    private @NotNull Function<@NotNull T, @NotNull ItemProvider> displayFunction = null;

    private @NotNull Predicate<@NotNull T> filter = _ -> true;
    private @NotNull BiConsumer<@Nullable T, @NotNull Player> onSelected = (_, _) -> {
    };

    private boolean autoSelectIfOne = false;
    private boolean showNoneButton = true;

    /**
     * Opens the GUI for the player. Will not open the GUI if no recipes pass the filter, and will return false.
     *
     * @param player the player to open the GUI for
     * @return true if the GUI was opened with any recipes showing, false if no recipes passed the filter and the GUI was not opened
     */
    public boolean open(@NotNull Player player) {
        Preconditions.checkNotNull(displayFunction, "Display function not set");

        List<T> recipes = new ArrayList<>();
        for (T recipe : recipeType) {
            if (filter.test(recipe)) {
                recipes.add(recipe);
            }
        }

        if (recipes.isEmpty()) return false;
        if (autoSelectIfOne && recipes.size() == 1) {
            onSelected.accept(recipes.getFirst(), player);
            return true;
        }

        List<Item> items = new ArrayList<>();

        if (showNoneButton) {
            Item noneButton = Item.builder()
                    .setItemProvider(ItemStackBuilder.gui(Material.BARRIER, "none").name(Component.translatable("pylon.gui.none")))
                    .addClickHandler(_ -> onSelected.accept(null, player))
                    .build();
            items.add(noneButton);
        }

        for (T recipe : recipes) {
            Item item = Item.builder()
                    .setItemProvider(displayFunction.apply(recipe))
                    .addClickHandler(_ -> onSelected.accept(recipe, player))
                    .build();
            items.add(item);
        }

        Gui gui = PagedGui.itemsBuilder()
                .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', GuiItems.pagePrevious())
                .addIngredient('>', GuiItems.pageNext())
                .setContent(items)
                .build();
        Window.builder()
                .setUpperGui(gui)
                .setTitle(Component.translatable("pylon.gui.select-recipe"))
                .open(player);

        return true;
    }
}
