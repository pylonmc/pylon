package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.pylon.content.building.sponge.PowerfulWaterSponge;
import io.github.pylonmc.pylon.content.building.sponge.WetPowerfulWaterSponge;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ConfigurableRecipeType;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * Represents converting a **BLOCK** into another **BLOCK** by immersing it in a fluid.
 * This recipe is for display ONLY.
 * Example: immersing a {@link PowerfulWaterSponge} in water results in a {@link WetPowerfulWaterSponge}.
 * The actual logic lives in {@link PowerfulWaterSponge#updateSponge(Block)}.
 *
 * @param ingredient the ingredient **BLOCK**
 * @param fluid      the fluid
 * @param result     the result **BLOCK**
 * @param chance     the chance of the result, from 0.0 to 1.0
 * @author balugaq
 */
public record ImmerseRecipe(
        @NotNull NamespacedKey key,
        @NotNull ItemChoice ingredient,
        @NotNull RebarFluid fluid,
        @NotNull ItemStack result,
        float chance
) implements RebarRecipe {
    public static final ConfigurableRecipeType<ImmerseRecipe> RECIPE_TYPE = new ConfigurableRecipeType<>(pylonKey("immerse")) {
        @Override
        protected @NotNull ImmerseRecipe loadRecipe(@NotNull NamespacedKey key, @NotNull ConfigSection section) {
            return new ImmerseRecipe(
                    key,
                    section.getOrThrow("ingredient", ConfigAdapter.ITEM_CHOICE),
                    section.getOrThrow("fluid", ConfigAdapter.REBAR_FLUID),
                    section.getOrThrow("result", ConfigAdapter.ITEM_STACK),
                    section.get("chance", ConfigAdapter.FLOAT, 1.0f)
            );
        }
    };

    @Override
    public @NotNull Gui display() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # # # # # #",
                        "# # # i f o # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.backgroundBlack())
                .addIngredient('f', ItemStackBuilder.of(Material.BUCKET)
                        .name(Component.translatable(
                                "pylon.pylon.guide.recipe.immerse.name",
                                RebarArgument.of("fluid", Component.translatable("pylon.pylon.fluid." + fluid.getKey().getKey()))
                        ))
                        .lore(Component.translatable(
                                "pylon.pylon.guide.recipe.immerse.chance",
                                RebarArgument.of("chance", UnitFormat.PERCENT.format(Math.round(chance * 100)))
                        ))
                )
                .addIngredient('i', ItemButton.of(ingredient))
                .addIngredient('o', ItemButton.of(result))
                .build();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return List.of(ingredient);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return List.of(FluidOrItem.of(result));
    }
}
