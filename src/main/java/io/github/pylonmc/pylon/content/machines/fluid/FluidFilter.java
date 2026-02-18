package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.content.machines.fluid.gui.FluidSelector;
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
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class FluidFilter extends RebarBlock
        implements RebarFluidTank, RebarDirectionalBlock, RebarGuiBlock {

    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    public static final NamespacedKey FLUID_KEY = pylonKey("fluid");

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.WHITE_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStack noFluidStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":fluid:none")
            .build();

    protected @Nullable RebarFluid fluid;

    @SuppressWarnings("unused")
    public FluidFilter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);

        fluid = null;

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
        addEntity("fluid1", new ItemDisplayBuilder()
                .itemStack(noFluidStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.2, 0.3, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );
        addEntity("fluid2", new ItemDisplayBuilder()
                .itemStack(noFluidStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.3, 0.2, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.25F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.25F);
    }

    @SuppressWarnings("unused")
    public FluidFilter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
        fluid = pdc.get(FLUID_KEY, RebarSerializers.REBAR_FLUID);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, FLUID_KEY, RebarSerializers.REBAR_FLUID, fluid);
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
                RebarArgument.of("fluid", fluid == null
                        ? Component.translatable("pylon.fluid.none")
                        : fluid.getName()
                )
        ));
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return fluid.equals(this.fluid);
    }

    public void setFluid(RebarFluid fluid) {
        this.fluid = fluid;
        ItemStack stack = fluid == null ? noFluidStack : fluid.getItem();
        getHeldEntityOrThrow(ItemDisplay.class, "fluid1").setItemStack(stack);
        getHeldEntityOrThrow(ItemDisplay.class, "fluid2").setItemStack(stack);
    }

    @Override
    public @NotNull Gui createGui() {
        return (FluidSelector.make(() -> fluid, this::setFluid));
    }

    @Override
    public @NotNull Component getGuiTitle() {
        return Component.translatable("pylon.item.fluid_filter.gui");
    }
}
