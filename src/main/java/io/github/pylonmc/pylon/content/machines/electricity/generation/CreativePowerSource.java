package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class CreativePowerSource extends RebarBlock implements SimpleElectricRebarBlock, TickingRebarBlock {

    private static final float BLACK_HOLE_SIZE = 0.25f;

    @SuppressWarnings("unused")
    public CreativePowerSource(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(1);
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            createSimpleElectricPort(NodeType.PRODUCER, face);
        }
        addEntity("black_hole", new BlockDisplayBuilder()
                .material(Material.END_GATEWAY)
                .transformation(new TransformBuilder().scale(BLACK_HOLE_SIZE))
                .build(block.getLocation().toCenterLocation()));
        setPowerProduced(1e21);
    }

    @SuppressWarnings({"unused"})
    public CreativePowerSource(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    private int animationTicksLeft = 0;

    @Override
    public void tick() {
        if (animationTicksLeft-- > 0) return;
        Random random = ThreadLocalRandom.current();
        BlockDisplay blackHole = getHeldEntityOrThrow(BlockDisplay.class, "black_hole");

        int ticks = random.nextInt(1, 20);
        blackHole.setInterpolationDelay(0);
        blackHole.setInterpolationDuration(ticks);
        animationTicksLeft = ticks;

        Vector3f randomAxis = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
        blackHole.setTransformationMatrix(new TransformBuilder()
                .rotate(randomAxis, (float) (2 * Math.PI) * random.nextFloat())
                .scale(BLACK_HOLE_SIZE * random.nextFloat() + BLACK_HOLE_SIZE / 2)
                .buildForBlockDisplay());
    }
}
