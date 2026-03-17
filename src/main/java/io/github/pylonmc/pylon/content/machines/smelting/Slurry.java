package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.recipes.MixingPotRecipe;
import io.github.pylonmc.pylon.recipes.StrainingRecipe;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.registry.RegistryHandler;
import lombok.Builder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Slurry extends RebarFluid implements RegistryHandler {

    @Getter
    private final ItemStack slurryStack;

    @Getter
    private final int amount;


    public Slurry(@NotNull NamespacedKey key, @NotNull Component name, @NotNull ItemStack slurryStack, @NotNull List<RebarFluidTag> tags, int amount) {
        super(key, name, Material.LIGHT_GRAY_CONCRETE, tags);
        this.amount = amount;
        this.slurryStack = slurryStack;
    }
    
    public Slurry(@NotNull NamespacedKey key, @NotNull Component name, @NotNull ItemStack slurryStack, @NotNull List<RebarFluidTag> tags) {
        super(key, name, Material.LIGHT_GRAY_CONCRETE, tags);
        this.amount = 1000;
        this.slurryStack = slurryStack;
    }

    public Slurry(@NotNull NamespacedKey key, @NotNull ItemStack slurryStack, @NotNull RebarFluidTag @NotNull ... tags) {
        super(key, Material.LIGHT_GRAY_CONCRETE, tags);
        this.amount = 1000;
        this.slurryStack = slurryStack;
    }

    public Slurry(@NotNull NamespacedKey key, @NotNull ItemStack slurryStack, int amount, @NotNull RebarFluidTag @NotNull ... tags) {
        super(key, Material.LIGHT_GRAY_CONCRETE, tags);
        this.amount = amount;
        this.slurryStack = slurryStack;
    }

    @Override
    public void onRegister(@NotNull RebarRegistry<?> registry) {
        MixingPotRecipe.RECIPE_TYPE.addRecipe(new MixingPotRecipe(
                getKey(),
                List.of(RecipeInput.of(slurryStack)),
                RecipeInput.of(PylonFluids.SLURRY, amount),
                FluidOrItem.of(this, amount),
                false
        ));
        StrainingRecipe.RECIPE_TYPE.addRecipe(new StrainingRecipe(
                getKey(),
                RecipeInput.of(this, amount),
                PylonFluids.SLURRY,
                slurryStack
        ));
    }
}
