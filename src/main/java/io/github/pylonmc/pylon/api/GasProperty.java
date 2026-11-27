package io.github.pylonmc.pylon.api;

import io.github.pylonmc.rebar.fluid.RebarFluidProperty;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public class GasProperty implements RebarFluidProperty {

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable("pylon.fluid.property.gas");
    }
}
