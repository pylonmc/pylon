package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarMultiblock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public abstract class Quarry extends RebarBlock implements RebarMultiblock, RebarProcessor {

    public static final NamespacedKey POSITION_KEY = pylonKey("position");
    public static final NamespacedKey Y_KEY = pylonKey("y");
    public static final NamespacedKey CHUNK_POSITIONS_KEY = pylonKey("chunk_positions");

    public final int radius = getSettings().getOrThrow("radius", ConfigAdapter.INTEGER);

    protected int y;
    protected boolean platformReady = false;
    protected final Set<ChunkPosition> chunkPositions;
    protected Location position;

    @SuppressWarnings("unused")
    public Quarry(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        World world = block.getWorld();
        WorldBorder border = world.getWorldBorder();
        y = block.getY() - 1;
        position = new Location(world, block.getX() - radius, y, block.getZ() - radius);
        chunkPositions = new HashSet<>();
        for (int x = -radius; x <= radius; x += 16) {
            for (int z = -radius; z <= radius; z += 16) {
                Location location = new Location(world, x, y, z);
                if (border.isInside(location)) {
                    BlockPosition blockPosition = new BlockPosition(location);
                    chunkPositions.add(blockPosition.getChunk());
                }
            }
        }
    }

    @SuppressWarnings({"DataFlowIssue", "unused"})
    public Quarry(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        position = pdc.get(POSITION_KEY, RebarSerializers.LOCATION);
        y = pdc.get(Y_KEY, RebarSerializers.INTEGER);
        chunkPositions = pdc.get(CHUNK_POSITIONS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.CHUNK_POSITION));
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(POSITION_KEY, RebarSerializers.LOCATION, position);
        pdc.set(Y_KEY, RebarSerializers.INTEGER, y);
        pdc.set(CHUNK_POSITIONS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.CHUNK_POSITION), chunkPositions);
    }

    protected void updateMiner() {
        Block block = getBlock();
        if (y == block.getWorld().getMinHeight()) {
            // Finished mining; do one more pass in case something has changed in the meantime
            y = block.getY() - 1;
            platformReady = false;
        }
    }

    @Override
    public @NotNull Set<ChunkPosition> getChunksOccupied() {
        return chunkPositions;
    }

    @Override
    public boolean checkFormed() {
        return true;
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        Vector relative = getBlock().getLocation().subtract(otherBlock.getLocation()).toVector();
        return Math.abs(relative.getBlockX()) <= radius
                && Math.abs(relative.getBlockZ()) <= radius
                && Math.abs(relative.getBlockY()) <= radius;
    }

    @Override
    public void onMultiblockRefreshed() {
        updateMiner();
    }

    protected abstract @Nullable Integer getBreakTicks(@NotNull Block block);
}
