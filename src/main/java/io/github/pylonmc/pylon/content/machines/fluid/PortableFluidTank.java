package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class PortableFluidTank extends RebarBlock implements FluidTankWithDisplayEntity, RebarInteractBlock {

    public static class Item extends RebarItem {
        public static final NamespacedKey FLUID_AMOUNT_KEY = pylonKey("fluid_amount");
        public static final NamespacedKey FLUID_TYPE_KEY = pylonKey("fluid_type");

        @Getter
        private final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);

        @Getter
        private final List<FluidTemperature> allowedTemperatures = getSettings().getOrThrow(
                "allowed-temperatures",
                ConfigAdapter.LIST.from(ConfigAdapter.FLUID_TEMPERATURE)
        );

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        public @Nullable RebarFluid getFluid() {
            return getStack().getPersistentDataContainer().get(FLUID_TYPE_KEY, RebarSerializers.REBAR_FLUID);
        }

        public double getAmount() {
            return getStack().getPersistentDataContainer().getOrDefault(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0);
        }

        public void setFluid(@Nullable RebarFluid fluid) {
            getStack().editPersistentDataContainer(pdc -> RebarUtils.setNullable(pdc, FLUID_TYPE_KEY, RebarSerializers.REBAR_FLUID, fluid));
        }

        public void setAmount(double amount) {
            getStack().editPersistentDataContainer(pdc -> pdc.set(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, amount));
        }

        @Override
        public @NotNull RebarBlock place(@NotNull BlockCreateContext context) {
            PortableFluidTank tank = (PortableFluidTank) getSchema().place(context);
            tank.setFluidType(getFluid());
            tank.setFluid(getAmount());
            return tank;
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("fluid", getFluid() == null
                            ? Component.translatable("pylon.fluid.none")
                            : getFluid().getName()
                    ),
                    RebarArgument.of("amount", Math.round(getAmount())),
                    RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(capacity)),
                    RebarArgument.of("temperatures", Component.join(
                            JoinConfiguration.separator(Component.text(", ")),
                            allowedTemperatures.stream()
                                    .map(FluidTemperature::getValueText)
                                    .collect(Collectors.toList())
                    ))
            );
        }
    }

    public final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);

    public final List<FluidTemperature> allowedTemperatures = getSettings().getOrThrow(
            "allowed-temperatures",
            ConfigAdapter.LIST.from(ConfigAdapter.FLUID_TEMPERATURE)
    );

    @SuppressWarnings("unused")
    public PortableFluidTank(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        createFluidDisplay();
        createFluidPoint(FluidPointType.INPUT, BlockFace.UP);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.DOWN);
        setCapacity(capacity);
    }

    @SuppressWarnings("unused")
    public PortableFluidTank(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return fluid.hasTag(FluidTemperature.class)
                && allowedTemperatures.contains(fluid.getTag(FluidTemperature.class));
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
    public @Nullable ItemStack getDropItem(@NotNull BlockBreakContext context) {
        return getPickItem();
    }

    @Override
    public @Nullable ItemStack getPickItem() {
        // TODO implement clone for RebarItem and just clone it
        ItemStack stack = RebarRegistry.ITEMS.getOrThrow(getKey()).getItemStack();

        Item item = new Item(stack);
        item.setFluid(getFluidType());
        item.setAmount(getFluidAmount());

        return stack;
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR }, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        PylonUtils.handleFluidTankRightClick(this, event, priority);
    }
}
