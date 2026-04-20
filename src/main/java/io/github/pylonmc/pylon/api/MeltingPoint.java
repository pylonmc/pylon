package io.github.pylonmc.pylon.api;

import net.kyori.adventure.text.Component;

import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;

public record MeltingPoint(double temperature) implements RebarFluidTag {
    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable(
                "pylon.fluid.tag.melting-point",
                RebarArgument.of("temperature", UnitFormat.CELSIUS.format(temperature).decimalPlaces(1))
        );
    }
}
