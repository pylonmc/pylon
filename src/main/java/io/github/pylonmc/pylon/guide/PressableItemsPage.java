package io.github.pylonmc.pylon.guide;

import io.github.pylonmc.pylon.recipes.PressRecipe;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class PressableItemsPage extends SimpleDynamicGuidePage {

    private static final PressableItemsPage INSTANCE = new PressableItemsPage();
    @Getter private static final Item button = new PageButton(
            ItemStackBuilder.of(Material.WHEAT)
                    .name(Component.translatable("pylon.guide.page.pressable_items"))
                    .build(),
            INSTANCE
    );

    public PressableItemsPage() {
        super(pylonKey("pressable_items"), PressableItemsPage::getButtons);
    }

    private static @NonNull List<Item> getButtons() {
        List<PressRecipe> sortedRecipes = PressRecipe.RECIPE_TYPE.getRecipes()
                .stream()
                .sorted(PressableItemsPage::compareRecipes)
                .toList();
        List<Item> buttons = new ArrayList<>();
        for (PressRecipe recipe : sortedRecipes) {
            ItemStack stack = ItemStackBuilder.of(recipe.input().getRepresentativeItem().clone())
                    .lore(Component.translatable("pylon.guide.pressable_items").arguments(
                            RebarArgument.of("plant-oil", UnitFormat.MILLIBUCKETS.format(recipe.oilAmount()))
                    ))
                    .build();
            buttons.add(new ItemButton(stack));
        }
        return buttons;
    }

    private static int compareRecipes(@NonNull PressRecipe l, @NonNull PressRecipe r) {
        return Double.compare(r.oilAmount(), l.oilAmount());
    }
}
