package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericMachine;
import io.github.pylonmc.pylon.recipes.HammerRecipe;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.math.BigDecimal;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ElectricCompressor extends GenericMachine<HammerRecipe> implements SimpleElectricRebarBlock {

    private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);
    private final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettings().getOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage)),
                    RebarArgument.of("speed", BigDecimal.valueOf(speed).stripTrailingZeros().toPlainString())
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricCompressor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(HammerRecipe.RECIPE_TYPE);
        createSimpleElectricPort(NodeType.CONSUMER, getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricCompressor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull HammerRecipe recipe) {
        return Math.max(1, (int) Math.round(recipe.uses() / speed));
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull HammerRecipe recipe) {
        return List.of(recipe.result().clone());
    }

    @Override
    public void tick() {
        if (!isPowered() || !isProcessingRecipe()) return;
        progressRecipe(getTickInterval());
    }
}
