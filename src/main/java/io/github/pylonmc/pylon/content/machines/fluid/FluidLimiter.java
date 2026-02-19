package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class FluidLimiter extends RebarBlock implements RebarDirectionalBlock, RebarFluidTank, RebarGuiBlock {

    private static final NamespacedKey MAX_FLOW_RATE_KEY = pylonKey("amount");

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.WHITE_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder verticalStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":vertical");

    public final int buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int minAmount = getSettings().getOrThrow("min-amount", ConfigAdapter.INTEGER);
    public final int maxAmount = getSettings().getOrThrow("max-amount", ConfigAdapter.INTEGER);

    public int maxFlowRate;

    @SuppressWarnings("unused")
    public FluidLimiter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);

        setCapacity(buffer);
        setFacing(context.getFacing());

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.25, 0.25, 0.5)
                )
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("vertical", new ItemDisplayBuilder()
                .itemStack(verticalStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.2, 0.3, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.25F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.25F);

        maxFlowRate = minAmount;
    }

    @SuppressWarnings("unused")
    public FluidLimiter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(MAX_FLOW_RATE_KEY, RebarSerializers.INTEGER, maxFlowRate);
    }

    public static class Item extends RebarItem {

        public final int minAmount = getSettings().getOrThrow("min-amount", ConfigAdapter.INTEGER);
        public final int maxAmount = getSettings().getOrThrow("max-amount", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of(
                            "min-amount",
                            UnitFormat.MILLIBUCKETS.format(minAmount)
                    ),
                    RebarArgument.of(
                            "max-amount",
                            UnitFormat.MILLIBUCKETS.format(maxAmount)
                    )
            );
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                        getFluidAmount(),
                        getFluidCapacity(),
                        20,
                        TextColor.color(200, 255, 255)
                )),
                RebarArgument.of("fluid", getFluidType() == null
                        ? Component.translatable("pylon.fluid.none")
                        : getFluidType().getName()
                )
        ));
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true;
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        return Math.min(maxFlowRate, RebarFluidTank.super.fluidAmountRequested(fluid));
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # m # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', new MaxFlowRateItem())
                .build();
    }

    public class MaxFlowRateItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player player) {
            return ItemStackBuilder.of(Material.WHITE_CONCRETE)
                    .name(Component.translatable("pylon.gui.fluid_accumulator.name").arguments(
                            RebarArgument.of("amount", UnitFormat.MILLIBUCKETS_PER_SECOND.format(maxFlowRate))
                    ))
                    .lore(Component.translatable("pylon.gui.fluid_accumulator.lore"));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            int delta = clickType.isShiftClick() ? 100 : 10;
            if (clickType.isLeftClick()) {
                maxFlowRate += delta;
            } else if (clickType.isRightClick()) {
                maxFlowRate -= delta;
            }
            maxFlowRate = Math.clamp(maxFlowRate, minAmount, maxAmount);
            notifyWindows();
        }
    }
}
