package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
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
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class FluidValve extends RebarBlock
        implements RebarFluidTank, RebarInteractBlock, RebarDirectionalBlock {

    public static final NamespacedKey ENABLED_KEY = pylonKey("enabled");

    private boolean open;

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

    public final ItemStackBuilder closedStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":closed");
    public final ItemStackBuilder openStack = ItemStackBuilder.of(Material.WHITE_CONCRETE)
            .addCustomModelDataString(getKey() + ":open");

    @SuppressWarnings("unused")
    public FluidValve(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);

        setCapacity(buffer);
        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.25F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.25F);
        addEntity("main", new ItemDisplayBuilder()
                .itemStack(closedStack)
                .transformation(new TransformBuilder()
                        .lookAlong(RebarUtils.rotateFaceToReference(getFacing(), BlockFace.NORTH).getDirection().toVector3d())
                        .scale(0.2, 0.2, 0.5)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        open = false;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public FluidValve(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);

        open = pdc.get(ENABLED_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(ENABLED_KEY, RebarSerializers.BOOLEAN, open);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || event.getPlayer().isSneaking()) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);

        open = !open;

        getHeldEntityOrThrow(ItemDisplay.class, "main")
                .setItemStack((open ? openStack : closedStack).build());
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("status", Component.translatable(
                        "pylon.message.valve." + (open ? "open" : "closed")
                )),
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
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (!open) {
            return 0.0;
        }
        return RebarFluidTank.super.fluidAmountRequested(fluid);
    }

    @Override
    public @NotNull Map<@NotNull RebarFluid, @NotNull Double> getSuppliedFluids() {
        if (!open) {
            return Map.of();
        }
        return RebarFluidTank.super.getSuppliedFluids();
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true;
    }
}
