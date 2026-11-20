package io.github.pylonmc.pylon.recipes;

import io.github.pylonmc.rebar.recipe.RebarRecipe;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItemChoice;
import java.util.List;
import java.util.function.Supplier;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

public class SingleRecipe extends RecipeType<RebarRecipe> implements RebarRecipe {
    public final NamespacedKey key;
    public final @NotNull List<? extends @NotNull FluidOrItemChoice> inputs;
    public final @NotNull List<@NotNull FluidOrItem> outputs;
    public final @NotNull Supplier<@Nullable Gui> displaySupplier;

    public SingleRecipe(
            NamespacedKey key,
            @NotNull FluidOrItemChoice input,
            @NotNull FluidOrItem output,
            @NotNull Supplier<@Nullable Gui> displaySupplier
    ) {
        this(key, List.of(input), List.of(output), displaySupplier);
    }

    public SingleRecipe(
            NamespacedKey key,
            @NotNull List<? extends @NotNull FluidOrItemChoice> inputs,
            @NotNull List<@NotNull FluidOrItem> outputs,
            @NotNull Supplier<@Nullable Gui> displaySupplier
    ) {
        super(key);
        this.key = key;
        this.inputs = inputs;
        this.outputs = outputs;
        this.displaySupplier = displaySupplier;
        addRecipe(this);
    }

    @Override
    public @NotNull List<@NotNull FluidOrItemChoice> getInputs() {
        return inputs.stream()
                .map(choice -> (FluidOrItemChoice) choice)
                .toList();
    }

    @Override
    public @NotNull List<@NotNull FluidOrItem> getResults() {
        return outputs;
    }

    @Override
    public @Nullable Gui display() {
        return displaySupplier.get();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
