package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

public class ManualCoreDrill extends CoreDrill {

    public ManualCoreDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public ManualCoreDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 0, -1), MultiblockComponent.of(PylonKeys.MANUAL_CORE_DRILL_LEVER));

        components.put(new Vector3i(1, 0, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(1, -1, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(1, -2, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));

        components.put(new Vector3i(-1, 0, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(-1, -1, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(-1, -2, 0), MultiblockComponent.of(Material.COBBLESTONE_WALL));

        components.put(new Vector3i(0, 0, 1), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(0, -1, 1), MultiblockComponent.of(Material.COBBLESTONE_WALL));
        components.put(new Vector3i(0, -2, 1), MultiblockComponent.of(Material.COBBLESTONE_WALL));

        return components;
    }
}
