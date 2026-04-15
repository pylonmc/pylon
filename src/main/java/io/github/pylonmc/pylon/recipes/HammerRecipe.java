package io.github.pylonmc.pylon.recipes;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.content.tools.Hammer;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.recipe.*;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MiningLevel;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * @param input  the input item (setting the itemstack to have an amount that's not 1 will have no effect)
 * @param result the output item (respects amount)
 * @param level  the minimum hammer mining level
 * @param chance the chance to succeed per attempt
 */
public record HammerRecipe(
        @NotNull NamespacedKey key,
        @NotNull RecipeInput.Item input,
        @NotNull ItemStack result,
        @NotNull MiningLevel level,
        int uses
) implements RebarRecipe {

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public static final RecipeType<HammerRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("hammer")) {

        private static final ConfigAdapter<MiningLevel> MINING_LEVEL_ADAPTER = ConfigAdapter.ENUM.from(MiningLevel.class);

        @Override
        protected @NotNull HammerRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new HammerRecipe(
                    key,
                    section.getOrThrow("input", ConfigAdapter.RECIPE_INPUT_ITEM),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.getOrThrow("mining-level", MINING_LEVEL_ADAPTER),
                    section.getOrThrow("uses", ConfigAdapter.INTEGER)
            );
        }
    };

    @Override
    public @NotNull List<RecipeInput> getInputs() {
        return List.of(input);
    }

    @Override
    public @NotNull List<FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # # i h o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('i', ItemButton.from(input))
                .addIngredient('h', new ItemButton(getHammers()))
                .addIngredient('o', ItemButton.from(result))
                .build();
    }

    private @NonNull List<ItemStack> getHammers() {
        List<ItemStack> hammers = new ArrayList<>();
        for (RebarItemSchema itemSchema : RebarRegistry.ITEMS.getValues()) {
            ItemStack stack = itemSchema.getItemStack();
            RebarItem item = RebarItem.fromStack(stack);
            if (item instanceof Hammer hammer) {
                if (!hammer.miningLevel.isAtLeast(level)) {
                    continue;
                }
                List<Component> lore = stack.lore();
                Preconditions.checkNotNull(lore);
                lore.add(Component.empty());
                lore.add(Component.translatable("pylon.guide.recipe.hammer",
                        RebarArgument.of("uses", uses)
                ));
                stack.lore(lore);
                hammers.add(stack);
            }
        }
        return hammers;
    }
}