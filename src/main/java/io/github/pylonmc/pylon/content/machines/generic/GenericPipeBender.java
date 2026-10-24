package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.recipes.PipeBendingRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

public abstract class GenericPipeBender extends GenericMachine<PipeBendingRecipe> implements EntityHolderRebarBlock {

    public final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

    protected GenericPipeBender(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(PipeBendingRecipe.RECIPE_TYPE);
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .lookAlong(new Vector3d(0.0, 1.0, 0.0))
                        .scale(0.4))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
    }

    protected GenericPipeBender(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull PipeBendingRecipe recipe) {
        return (int) Math.round(recipe.timeTicks() / speed);
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull PipeBendingRecipe recipe) {
        return List.of(recipe.result());
    }

    @Override
    public void onRecipeFinished(@NotNull PipeBendingRecipe recipe) {
        super.onRecipeFinished(recipe);
        clearDisplayedItem();
    }

    public void setDisplayedItem(@NotNull ItemStack stack) {
        getHeldEntityOrThrow(ItemDisplay.class, "item").setItemStack(stack);
    }

    public void clearDisplayedItem() {
        getHeldEntityOrThrow(ItemDisplay.class, "item").setItemStack(null);
    }
}
