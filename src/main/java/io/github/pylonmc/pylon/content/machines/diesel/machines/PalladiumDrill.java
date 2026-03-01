package io.github.pylonmc.pylon.content.machines.diesel.machines;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;


public class PalladiumDrill extends RebarBlock implements RebarSimpleMultiblock, RebarProcessor, RebarDirectionalBlock {

    public PalladiumDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(getFacing());
    }

    public PalladiumDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.ITEM_OUTPUT_HATCH));

        components.put(new Vector3i(-2, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(-2, 0, 2), new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(new Vector3i(-2, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(2, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(2, 0, 2), new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(2, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(-1, 0, 4), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(0, 0, 4), new RebarMultiblockComponent(PylonKeys.FLUID_INPUT_HATCH));
        components.put(new Vector3i(1, 0, 4), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));

        components.put(new Vector3i(1, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 1), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(1, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-1, 0, 3), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        components.put(new Vector3i(2, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(2, 0, 4), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));
        components.put(new Vector3i(-2, 0, 4), new RebarMultiblockComponent(PylonKeys.BRONZE_GRATING));

        return components;
    }
}
