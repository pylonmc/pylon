package io.github.pylonmc.pylon.api;

import io.github.pylonmc.rebar.fluid.RebarFluidProperty;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;


public record FlammableProperty(double secondsPerBucket) implements RebarFluidProperty {

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable("pylon.fluid.property.flammable").arguments(
                RebarArgument.of("burn-time", UnitFormat.SECONDS.format(secondsPerBucket)
                        .valueStyle(Style.style(NamedTextColor.WHITE))
                )
        );
    }
}
