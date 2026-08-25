package io.github.pylonmc.pylon.content.machines.generic;

import io.github.pylonmc.pylon.recipes.TableSawRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class GenericTableSaw extends GenericMachine<TableSawRecipe> implements EntityHolderRebarBlock {

    protected final ItemStackBuilder sawStack = ItemStackBuilder.of(Material.IRON_BARS)
            .addCustomModelDataString(getKey() + ":saw");

    @SuppressWarnings("unused")
    public GenericTableSaw(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(TableSawRecipe.RECIPE_TYPE);
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .scale(0.3))
                .build(block.getLocation().toCenterLocation().add(0, 0.65, 0))
        );
        addEntity("saw", new ItemDisplayBuilder()
                .itemStack(sawStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.4, 0.4))
                .build(block.getLocation().toCenterLocation().add(0, 0.7, 0))
        );
    }

    @SuppressWarnings("unused")
    public GenericTableSaw(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull TableSawRecipe recipe) {
        return recipe.timeTicks();
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull TableSawRecipe recipe) {
        return List.of(recipe.result());
    }

    @Override
    public void onRecipeFinished(@NotNull TableSawRecipe recipe) {
        super.onRecipeFinished(recipe);
        getItemDisplay().setItemStack(null);
    }

    public ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }
}
