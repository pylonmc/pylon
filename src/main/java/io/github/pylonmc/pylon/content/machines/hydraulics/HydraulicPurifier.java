package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.event.RebarRegisterEvent;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public interface HydraulicPurifier extends RebarTickingBlock {
    double getPurificationSpeed();
    double getPurificationEfficiency();

    record PurificationRecipe(
            NamespacedKey key
    ) implements RebarRecipe {

        public static final RecipeType<PurificationRecipe> RECIPE_TYPE = new RecipeType<>(pylonKey("hydraulic_purification"));

        @Override
        public @NotNull List<@NotNull RecipeInput> getInputs() {
            return List.of(RecipeInput.of(PylonFluids.DIRTY_HYDRAULIC_FLUID, 1));
        }

        @Override
        public @NotNull List<@NotNull FluidOrItem> getResults() {
            return List.of(FluidOrItem.of(PylonFluids.HYDRAULIC_FLUID, 1));
        }

        @Override
        public @NotNull Gui display() {
            return Gui.builder()
                    .setStructure(
                            "# # # # # # # # #",
                            "# # # # # # # # #",
                            "# d # # x # # h #",
                            "# # # # # # # # #",
                            "# # # # # # # # #"
                    )
                    .addIngredient('#', GuiItems.backgroundBlack())
                    .addIngredient('d', new FluidButton(PylonFluids.DIRTY_HYDRAULIC_FLUID))
                    .addIngredient('x', ItemButton.from(RebarRegistry.ITEMS.get(key).getItemStack()))
                    .addIngredient('h', new FluidButton(PylonFluids.HYDRAULIC_FLUID))
                    .build();
        }

        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }

        public static class Registrar implements Listener {
            @EventHandler
            public void onRegister(RebarRegisterEvent registerEvent) {
                if (!registerEvent.getRegistry().equals(RebarRegistry.BLOCKS)) return;
                var keyed = registerEvent.getValue();
                if (!(keyed instanceof RebarBlockSchema schema)) return;

                var clazz = schema.getBlockClass();
                if (!HydraulicPurifier.class.isAssignableFrom(clazz)) return;
                RECIPE_TYPE.addRecipe(new PurificationRecipe(schema.getKey()));
            }
        }
    }
}
