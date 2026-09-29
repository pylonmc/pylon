package io.github.pylonmc.pylon.api;

import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public class GasTag implements RebarFluidTag {

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable("pylon.fluid.tag.gas");
    }
}
