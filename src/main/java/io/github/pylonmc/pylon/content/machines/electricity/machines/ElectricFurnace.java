package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericMachine;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.FurnaceRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.recipe.vanilla.SmeltingRebarRecipe;
import io.github.pylonmc.rebar.recipe.vanilla.SmeltingRecipeType;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ElectricFurnace extends GenericMachine<SmeltingRebarRecipe> implements SimpleElectricRebarBlock, FurnaceRebarBlockHandler {

    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
    private final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100)),
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricFurnace(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(SmeltingRecipeType.INSTANCE);
        createSimpleElectricPort(NodeType.CONSUMER, getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricFurnace(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull SmeltingRebarRecipe recipe) {
        return (int) Math.ceil(recipe.getBukkitRecipe().getCookingTime() / speed);
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull SmeltingRebarRecipe recipe) {
        return List.of(recipe.getBukkitRecipe().getResult());
    }

    @Override
    public void onRecipeFinished(@NonNull SmeltingRebarRecipe recipe) {
        super.onRecipeFinished(recipe);
        Furnace furnaceData = (Furnace) getBlock().getBlockData();
        furnaceData.setLit(false);
        getBlock().setBlockData(furnaceData);
        refreshBlockTextureItem();
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(tickInterval);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Pair<@NotNull String, @NotNull Integer>> getBlockTextureProperties() {
        var props = super.getBlockTextureProperties();
        props.put("lit", new Pair<>(String.valueOf(isProcessingRecipe()), 2));
        return props;
    }
}