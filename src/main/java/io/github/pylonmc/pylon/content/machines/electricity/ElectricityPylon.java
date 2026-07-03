package io.github.pylonmc.pylon.content.machines.electricity;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.util.RebarUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public final class ElectricityPylon extends RebarBlock implements SimpleElectricRebarBlock {

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            createSimpleElectricPort(NodeType.CONNECTOR, face);
        }
    }

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
}
