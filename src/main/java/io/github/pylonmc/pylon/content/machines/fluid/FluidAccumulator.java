package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
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
import org.bukkit.entity.BlockDisplay;
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
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class FluidAccumulator extends RebarBlock implements
        RebarDirectionalBlock,
        RebarFluidTank,
        RebarGuiBlock {

    public static final NamespacedKey IS_DISCHARGING_KEY = pylonKey("is_discharging");

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.WHITE_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");

    public final int minAmount = getSettings().getOrThrow("min-amount", ConfigAdapter.INTEGER);
    public final int maxAmount = getSettings().getOrThrow("max-amount", ConfigAdapter.INTEGER);

    private boolean isDischarging;

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

    @SuppressWarnings("unused")
    public FluidAccumulator(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);

        this.isDischarging = false;

        setCapacity(minAmount);
        setFacing(context.getFacing());

        addEntity("main", new ItemDisplayBuilder()
            .itemStack(mainStack)
            .transformation(new TransformBuilder()
                .lookAlong(getFacing())
                .scale(0.25, 0.25, 0.5)
            )
            .build(block.getLocation().toCenterLocation())
        );
        addEntity("lamp", new BlockDisplayBuilder()
                .blockData(Material.REDSTONE_LAMP.createBlockData())
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.2, 0.3, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.25F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.25F);
    }

    @SuppressWarnings("unused")
    public FluidAccumulator(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);

        this.isDischarging = pdc.get(IS_DISCHARGING_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(IS_DISCHARGING_KEY, RebarSerializers.BOOLEAN, isDischarging);
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
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # m # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('m', new AmountItem())
                .build();
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (getBlock().isBlockIndirectlyPowered()) {
            return 0.0;
        }

        if (getFluidAmount() < 1.0e-6) {
            isDischarging = false;
            getHeldEntityOrThrow(BlockDisplay.class, "lamp")
                .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=false]"));
        }

        if (isDischarging) {
            return 0.0;
        }

        return RebarFluidTank.super.fluidAmountRequested(fluid);
    }

    @Override
    public @NotNull Map<@NotNull RebarFluid, @NotNull Double> getSuppliedFluids() {
        if (getBlock().isBlockIndirectlyPowered()) {
            return RebarFluidTank.super.getSuppliedFluids();
        }

        if (getFluidSpaceRemaining() < 1.0e-6) {
            isDischarging = true;
            getHeldEntityOrThrow(BlockDisplay.class, "lamp")
                    .setBlock(Material.REDSTONE_LAMP.createBlockData("[lit=true]"));
        }

        if (isDischarging) {
            return RebarFluidTank.super.getSuppliedFluids();
        }

        return Map.of();
    }

    public class AmountItem extends AbstractItem {

        @Override
        public @NonNull ItemProvider getItemProvider(@NonNull Player player) {
            return ItemStackBuilder.of(Material.WHITE_CONCRETE)
                    .name(Component.translatable("pylon.gui.fluid_accumulator.name").arguments(
                            RebarArgument.of("amount", UnitFormat.MILLIBUCKETS.format(getFluidCapacity()))
                    ))
                    .lore(Component.translatable("pylon.gui.fluid_accumulator.lore"));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            double delta = clickType.isShiftClick() ? 100 : 10;
            double newAmount;
            if (clickType.isLeftClick()) {
                newAmount = getFluidCapacity() + delta;
            } else if (clickType.isRightClick()) {
                newAmount = getFluidCapacity() - delta;
            } else {
                newAmount = getFluidCapacity();
            }
            newAmount = Math.clamp(newAmount, minAmount, maxAmount);
            setCapacity(newAmount);
            notifyWindows();
        }
    }
}
