package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.recipes.CastingRecipe;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class SmelteryCaster extends SmelteryComponent implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock {

    private static final NamespacedKey AUTO_CAST_KEY = pylonKey("auto_cast");

    private boolean autoCast = false;

    private @Nullable RebarFluid bottomFluid = null;

    @SuppressWarnings("unused")
    public SmelteryCaster(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(SmelteryController.TICK_INTERVAL);
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public SmelteryCaster(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        this.autoCast = pdc.get(AUTO_CAST_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("output", LogisticGroupType.OUTPUT, inventory);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(AUTO_CAST_KEY, RebarSerializers.BOOLEAN, autoCast);
    }

    private final VirtualInventory inventory = new VirtualInventory(1);
    private final CastItem castItem = new CastItem();

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # a # # # #",
                        "# # # # c # # # #",
                        "# # # # x # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('a', Item.builder()
                        .setItemProvider(viewer -> ItemStackBuilder.of(autoCast ? Material.GREEN_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE)
                                .name(Component.translatable("pylon.gui.smeltery_caster.auto_cast",
                                        RebarArgument.of("status", autoCast ? Component.translatable("pylon.gui.status.on") : Component.translatable("pylon.gui.status.off"))
                                ))
                        )
                        .addClickHandler(click -> autoCast = !autoCast)
                        .updateOnClick()
                )
                .addIngredient('c', castItem)
                .addIngredient('x', inventory)
                .addIngredient('#', GuiItems.background())
                .build();
    }

    private class CastItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            SmelteryController controller = getController();
            CastingRecipe recipe = (bottomFluid == null) ? null : CastingRecipe.getCastRecipeFor(bottomFluid);
            if (controller == null || bottomFluid == null || recipe == null) {
                return ItemStackBuilder.of(Material.BARRIER)
                        .name(Component.translatable("pylon.gui.smeltery_caster.cannot_cast"));
            }
            ItemStack result = recipe.result();
            Component name = result.effectiveName();
            double temperature = recipe.temperature();
            if (controller.getTemperature() < temperature) {
                return ItemStackBuilder.of(result.getType())
                        .name(Component.translatable("pylon.gui.smeltery_caster.cannot_cast"))
                        .lore(Component.translatable("pylon.gui.smeltery_caster.too_cold",
                                RebarArgument.of("item", name),
                                RebarArgument.of("temperature", temperature)));
            }
            double bottomAmount = controller.getFluidAmount(bottomFluid);
            if (bottomAmount < recipe.input().amountMillibuckets()) {
                return ItemStackBuilder.of(result.getType())
                        .name(Component.translatable("pylon.gui.smeltery_caster.cannot_cast"))
                        .lore(Component.translatable("pylon.gui.smeltery_caster.not_enough",
                                RebarArgument.of("fluid", bottomFluid.getName()),
                                RebarArgument.of("needed", UnitFormat.MILLIBUCKETS.format(recipe.input().amountMillibuckets())),
                                RebarArgument.of("amount", UnitFormat.MILLIBUCKETS.format(bottomAmount)
                                        .decimalPlaces(1))));
            }
            return ItemStackBuilder.of(result.getType())
                    .name(Component.translatable("pylon.gui.smeltery_caster.cast"))
                    .lore(Component.translatable("pylon.gui.smeltery_caster.click_to_cast",
                            RebarArgument.of("amount", UnitFormat.MILLIBUCKETS.format(bottomAmount)),
                            RebarArgument.of("needed", UnitFormat.MILLIBUCKETS.format(recipe.input().amountMillibuckets())),
                            RebarArgument.of("fluid", bottomFluid.getName())));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            cast();
        }
    }

    private void cast() {
        SmelteryController controller = getController();
        if (controller == null || bottomFluid == null) return;

        CastingRecipe recipe = CastingRecipe.getCastRecipeFor(bottomFluid);
        if (recipe == null || controller.getTemperature() < recipe.temperature() || controller.getFluidAmount(bottomFluid) < recipe.input().amountMillibuckets())
            return;

        ItemStack result = recipe.result();
        if (!inventory.canHold(result)) {
            return;
        }

        inventory.addItem(null, result);

        controller.removeFluid(bottomFluid, recipe.input().amountMillibuckets());
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("output", inventory);
    }

    @Override
    public void tick() {
        SmelteryController controller = getController();
        if (controller == null) {
            bottomFluid = null;
        } else {
            Pair<RebarFluid, Double> bottomFluid = controller.getBottomFluid();
            if (bottomFluid == null) {
                this.bottomFluid = null;
            } else {
                this.bottomFluid = bottomFluid.getFirst();
            }
            if (autoCast) {
                cast();
            }
        }
        castItem.notifyWindows();
    }
}
