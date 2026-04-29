package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class GasTurbine extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarTickingBlock,
        RebarDirectionalBlock {

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public GasTurbine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setMultiblockDirection(context.getFacing());
        setFacing(context.getFacing());
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public GasTurbine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {

    }

    private static final Vector3i FLUID_INPUT_HATCH = new Vector3i(0, 0, -2);
    private static final Vector3i FLUID_OUTPUT_HATCH = new Vector3i(0, 0, 2);
    private static final Vector3i ELECTRICITY_OUTPUT_HATCH = new Vector3i(0, -1, 2);

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        lineOfThree(0, 1, PylonKeys.REINFORCED_GLASS, components);
        lineOfThree(-1, 0, PylonKeys.REINFORCED_GLASS, components);
        lineOfThree(1, 0, PylonKeys.REINFORCED_GLASS, components);

        lineOfThree(0, -1, PylonKeys.STEEL_SUPPORT_BEAM, components);

        lineOfThree(0, -2, PylonKeys.BRONZE_FOUNDATION, components);
        lineOfThree(-1, -1, PylonKeys.BRONZE_FOUNDATION, components);
        lineOfThree(1, -1, PylonKeys.BRONZE_FOUNDATION, components);

        components.put(new Vector3i(0, -1, -2), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        components.put(FLUID_INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(FLUID_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        // TODO: electricity output hatch
        components.put(ELECTRICITY_OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        return components;
    }

    private static void lineOfThree(int x, int y, NamespacedKey key, Map<Vector3i, MultiblockComponent> components) {
        components.put(new Vector3i(x, y, 0), new RebarMultiblockComponent(key));
        components.put(new Vector3i(x, y, 1), new RebarMultiblockComponent(key));
        components.put(new Vector3i(x, y, -1), new RebarMultiblockComponent(key));
    }
}
