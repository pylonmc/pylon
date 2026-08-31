package io.github.pylonmc.pylon.content.machines.petrochemicals;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import kotlin.Pair;
import lombok.Getter;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class HydraulicFracture extends RebarBlock implements BlockBreakRebarBlockHandler {

    public static final NamespacedKey YIELD = PylonUtils.pylonKey("yield");

    @Getter private double yield;

    @SuppressWarnings("unused")
    public HydraulicFracture(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public HydraulicFracture(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        yield = pdc.get(YIELD, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(YIELD, RebarSerializers.DOUBLE, yield);
    }

    @Override
    public @Nullable ItemStack getDropItem(@NotNull BlockBreakContext context) {
        return null;
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Pair<@NotNull String, @NotNull Integer>> getBlockTextureProperties() {
        return Map.of("yield", new Pair<>(String.valueOf(Math.ceil(yield * 8)), 8)); // 0-7 inclusive
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

    public void setYield(double yield) {
        this.yield = yield;
        refreshBlockTextureItem();
    }
}
