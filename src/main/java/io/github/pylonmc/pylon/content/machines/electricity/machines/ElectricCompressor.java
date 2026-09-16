package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.content.machines.generic.GenericMachine;
import io.github.pylonmc.pylon.recipes.HammerRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformUtil;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public class ElectricCompressor extends GenericMachine<HammerRecipe> implements SimpleElectricRebarBlock {

    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
    private final double hammerTime = getSettingOrThrow("hammer-time", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double hammerTime = getSettingOrThrow("hammer-time", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage)),
                    RebarArgument.of("hammer-time", UnitFormat.SECONDS.format(hammerTime))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricCompressor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setRecipeType(HammerRecipe.RECIPE_TYPE);
        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());
        setRequiredPower(powerUsage);

        addEntity("shaft", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.NETHERITE_BLOCK).addCustomModelDataString(getKey() + ":shaft"))
                .transformation(new TransformBuilder()
                        .scale(0.3, 0.7, 0.3)
                        .translate(0, 0.4, 0)
                )
                .build(block.getLocation().toCenterLocation().add(0, 0.51, 0))
        );
    }

    @SuppressWarnings("unused")
    public ElectricCompressor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected int getRecipeTicks(@NonNull HammerRecipe recipe) {
        return Math.max(1, (int) Math.round(recipe.uses() * hammerTime * 20));
    }

    @Override
    protected @NotNull List<ItemStack> getResults(@NonNull HammerRecipe recipe) {
        return List.of(recipe.result());
    }

    @Override
    public void tick() {
        if (!isPowered() || !isProcessingRecipe()) return;
        progressRecipe(getTickInterval());
    }

    @Override
    protected boolean tryStartRecipe(HammerRecipe recipe, ItemStack stack) {
        boolean started = super.tryStartRecipe(recipe, stack);
        if (!started) return false;

        ItemDisplay display = getHeldEntityOrThrow(ItemDisplay.class, "shaft");
        Matrix4f startTransform = TransformUtil.transformationToMatrix(display.getTransformation());

        int ticks = getRecipeTicks(recipe);
        if (ticks <= 5) return true;
        PylonUtils.animate(display, ticks - 5, startTransform.translate(0, -0.7f, 0, new Matrix4f()));
        Bukkit.getScheduler().runTaskLater(
                Pylon.getInstance(),
                () -> PylonUtils.animate(display, 5, startTransform),
                ticks - 5
        );

        return true;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (!isPowered()) {
            display.add(Component.translatable("pylon.message.no_power"));
        }
        return display;
    }
}
