package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.electricity.ElectricNetwork;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public final class ElectricityPylon extends RebarBlock implements
        RebarElectricBlock,
        RebarTickingBlock {

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(10);
        ElectricNode.Connector centralNode = addElectricNode(new ElectricNode.Connector(new BlockPosition(block)));
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            ElectricNode.Connector port = addElectricPort(face, new ElectricNode.Connector(new BlockPosition(block)));
            centralNode.connect(port);
        }
    }

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        ElectricNetwork network = getElectricNodes().getFirst().getNetwork();
        for (ElectricNode node : network.getNodes()) {
            Particle.DUST.builder()
                    .color(Color.fromARGB(network.hashCode()))
                    .location(node.getBlock().toLocation().toCenterLocation().add(0, 0.6, 0))
                    .receivers(32, true)
                    .spawn();
        }
    }
}
