package io.github.pylonmc.pylon.api;

import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.RebarFluidTag;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


public record FlammableTag(double secondsPerBucket) implements RebarFluidTag {

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable("pylon.fluid.tag.flammable").arguments(
                RebarArgument.of("burn-time", UnitFormat.SECONDS.format(secondsPerBucket)
                        .valueStyle(Style.style(NamedTextColor.WHITE))
                )
        );
    }

    public static @NonNull List<RebarFluid> getFlammableFluids() {
        List<RebarFluid> fluids = new ArrayList<>();
        for (RebarFluid fluid : RebarRegistry.FLUIDS) {
            if (fluid.hasTag(FlammableTag.class)) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }
}
