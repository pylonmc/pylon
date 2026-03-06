package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.waila.Waila;
import kotlin.Pair;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReinforcedGlassCasing extends RebarBlock {
    @Getter
    private Position position = Position.BOTTOM;

    @SuppressWarnings("unused")
    public ReinforcedGlassCasing(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public ReinforcedGlassCasing(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public enum Position {
        TOP,
        MIDDLE,
        BOTTOM
    }

    public void setPosition(Position position) {
        this.position = position;
        refreshBlockTextureItem();
    }

    @Override
    public @NotNull Map<String, Pair<String, Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("position", new Pair<>(position.name().toLowerCase(), FluidTankCasing.Shape.values().length));
        return properties;
    }

    public void reset() {
        setPosition(Position.BOTTOM);
        Waila.removeWailaOverride(getBlock());
    }
}
