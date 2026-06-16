package io.github.pylonmc.pylon.content.machines.petrochemicals;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class HydraulicFracture extends RebarBlock implements BlockBreakRebarBlockHandler {

    public static final NamespacedKey YIELD = PylonUtils.pylonKey("yield");

    public double yield;

    public HydraulicFracture(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public HydraulicFracture(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        yield = pdc.get(YIELD, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(YIELD, RebarSerializers.DOUBLE, yield);
    }

    @Override
    public boolean onPreBlockBreak(@NotNull BlockBreakContext context) {
        return context instanceof BlockBreakContext.PluginBreak;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        TextColor color = TextColor.color(HSVLike.hsvLike((float) (yield * 0.324F), 1.0F, 1.0F));
        return WailaDisplay.of(this, player)
                .add(UnitFormat.PERCENT.format(100.0 * yield)
                        .decimalPlaces(2)
                        .valueStyle(Style.style(color))
                );
    }
}
