package io.github.pylonmc.pylon.content.machines.smelting;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

import io.github.pylonmc.pylon.recipes.CastingRecipe;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.OperationCategory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

public final class CastingUnit extends RebarBlock implements
        RebarFluidBlock,
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock {

    private static final NamespacedKey QUEUED_CASTS_KEY = pylonKey("queued_casts");
    private static final NamespacedKey AUTO_CAST_KEY = pylonKey("auto_cast");

    private static final NamespacedKey FLUID_TYPE_KEY = pylonKey("fluid_type");
    private static final NamespacedKey FLUID_AMOUNT_KEY = pylonKey("fluid_amount");

    private int queuedCasts;
    private boolean autoCast;

    private @Nullable RebarFluid fluidType;
    private double fluidAmount;

    private final VirtualInventory castInv = new VirtualInventory(1);
    private final VirtualInventory outputInv = new VirtualInventory(1);

    @SuppressWarnings("unused")
    public CastingUnit(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        setFacing(context.getFacing());

        queuedCasts = 0;
        autoCast = false;
        fluidType = null;
        fluidAmount = 0;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public CastingUnit(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        queuedCasts = pdc.get(QUEUED_CASTS_KEY, RebarSerializers.INTEGER);
        autoCast = pdc.get(AUTO_CAST_KEY, RebarSerializers.BOOLEAN);
        fluidType = pdc.get(FLUID_TYPE_KEY, RebarSerializers.REBAR_FLUID);
        fluidAmount = pdc.get(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        super.write(pdc);
        pdc.set(QUEUED_CASTS_KEY, RebarSerializers.INTEGER, queuedCasts);
        pdc.set(AUTO_CAST_KEY, RebarSerializers.BOOLEAN, autoCast);
        RebarUtils.setNullable(pdc, FLUID_TYPE_KEY, RebarSerializers.REBAR_FLUID, fluidType);
        pdc.set(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, fluidAmount);
    }

    @Override
    public void postInitialise() {
        castInv.addPreUpdateHandler(event -> {
            if (event.getNewItem() == null) return;
            for (CastingRecipe recipe : CastingRecipe.RECIPE_TYPE) {
                if (recipe.cast().isSimilar(event.getNewItem())) {
                    return;
                }
            }
            event.setCancelled(true);
        });
        outputInv.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        castInv.setGuiPriority(OperationCategory.ADD, 1);
        outputInv.setGuiPriority(OperationCategory.COLLECT, 1);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of(
                "cast", castInv,
                "output", outputInv
        );
    }

    private class CastingControlItem extends AbstractItem {

        @Override
        public @NotNull ItemProvider getItemProvider(@NonNull Player viewer) {
            return ItemStackBuilder.gui(queuedCasts > 0 || autoCast ? Material.LAVA_BUCKET : Material.BUCKET, pylonKey("casting_control"))
                    .name(Component.translatable("pylon.gui.casting-control.name"))
                    .lore(Component.translatable(
                            "pylon.gui.casting-control.lore",
                            RebarArgument.of("casting", fluidType == null ?
                                    Component.empty() :
                                    Component.translatable(
                                            "pylon.gui.casting-control.casting",
                                            RebarArgument.of("fluid", fluidType.getName()),
                                            RebarArgument.of("amount", UnitFormat.MILLIBUCKETS.format(fluidAmount).decimalPlaces(1))
                                    )
                            ),
                            RebarArgument.of("queued-casts", queuedCasts),
                            RebarArgument.of("auto-cast", Component.translatable(autoCast ? "pylon.gui.status.on" : "pylon.gui.status.off"))
                    ));
        }

        @Override
        public void handleClick(@NonNull ClickType clickType, @NonNull Player player, @NonNull Click click) {
            if (clickType == ClickType.LEFT && !autoCast) {
                queuedCasts++;
            } else if (clickType ==  ClickType.RIGHT && !autoCast && queuedCasts > 0) {
                queuedCasts--;
            } else if (clickType == ClickType.SHIFT_LEFT) {
                autoCast = !autoCast;
                queuedCasts = 0;
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                fluidType = null;
                fluidAmount = 0;
            }

            notifyWindows();
        }
    }

    private final CastingControlItem castingControlItem = new CastingControlItem();

    private static final ItemStack CAST_BORDER = ItemStackBuilder.gui(Material.GREEN_STAINED_GLASS_PANE, pylonKey("cast"))
            .name(Component.translatable("pylon.gui.cast-border"))
            .build();

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # c c c #",
                        "# # # i # c x c #",
                        "# # # # # c c c #",
                        "# # # o o o # # #",
                        "# # # o y o # # #",
                        "# # # o o o # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', castingControlItem)
                .addIngredient('c', CAST_BORDER)
                .addIngredient('o', GuiItems.output())
                .addIngredient('x', castInv)
                .addIngredient('y', outputInv)
                .build();
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (queuedCasts == 0 && !autoCast) return 0;
        if (fluidType != null && !fluidType.equals(fluid)) return 0;
        ItemStack castItem = castInv.getItem(0);
        if (castItem == null) return 0;

        for (CastingRecipe recipe : CastingRecipe.RECIPE_TYPE) {
            if (recipe.isInput(fluid) && castItem.isSimilar(recipe.cast())) {
                if (outputInv.simulateSingleAdd(recipe.result()) > 0) return 0;
                return recipe.input().amountMillibuckets() - fluidAmount;
            }
        }

        return 0;
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        if (queuedCasts == 0 && !autoCast) throw new AssertionError("Should not happen");
        if (fluidType != null && !fluidType.equals(fluid)) throw new AssertionError("Should not happen");
        ItemStack castItem = castInv.getItem(0);
        if (castItem == null) throw new AssertionError("Should not happen");

        for (CastingRecipe recipe : CastingRecipe.RECIPE_TYPE) {
            if (recipe.isInput(fluid) && castItem.isSimilar(recipe.cast())) {
                fluidType = fluid;
                fluidAmount += amount;
                if (Math.abs(fluidAmount - recipe.input().amountMillibuckets()) < 1e-6) {
                    fluidType = null;
                    fluidAmount = 0;
                    outputInv.addItem(new MachineUpdateReason(), recipe.result());
                    if (!autoCast) {
                        queuedCasts--;
                    }
                }
                castingControlItem.notifyWindows();
                break;
            }
        }
    }
}
