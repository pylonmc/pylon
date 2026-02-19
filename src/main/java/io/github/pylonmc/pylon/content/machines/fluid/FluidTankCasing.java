package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.Waila;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FluidTankCasing extends RebarBlock implements RebarInteractBlock {

    public final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);
    public final List<FluidTemperature> allowedTemperatures = getSettings().getOrThrow(
            "allowed-temperatures",
            ConfigAdapter.LIST.from(ConfigAdapter.FLUID_TEMPERATURE)
    );
    public Shape shape = Shape.SINGLE;
    public FluidTank tank;

    public static class Item extends RebarItem {

        public final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);
        public final List<FluidTemperature> allowedTemperatures = getSettings().getOrThrow(
                "allowed-temperatures",
                ConfigAdapter.LIST.from(ConfigAdapter.FLUID_TEMPERATURE)
        );

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
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

    public FluidTankCasing(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public FluidTankCasing(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<String, Pair<String, Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("shape", new Pair<>(shape.name().toLowerCase(), Shape.values().length));
        return properties;
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.useInteractedBlock() != Event.Result.DENY && tank != null) {
            PylonUtils.handleFluidTankRightClick(tank, event, priority);
        }
    }

    public void setShape(@NotNull Shape shape) {
        this.shape = shape;
        refreshBlockTextureItem();
    }

    public void reset() {
        setShape(Shape.SINGLE);
        Waila.removeWailaOverride(getBlock());
    }

    public enum Shape {
        SINGLE,
        BOTTOM,
        MIDDLE,
        TOP
    }
}
