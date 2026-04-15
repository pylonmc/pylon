package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarMultiblock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.util.position.ChunkPosition;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class SolarLens extends RebarBlock implements RebarMultiblock {

    private final Set<BlockPosition> adjacentBlocks = new HashSet<>();

    public SolarLens(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public SolarLens(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    {
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            adjacentBlocks.add(new BlockPosition(getBlock()).getRelative(face));
        }
    }

    @Override
    public @NotNull Set<@NotNull ChunkPosition> getChunksOccupied() {
        return adjacentBlocks.stream()
                .map(BlockPosition::getChunk)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean checkFormed() {
        return true;
    }

    @Override
    public boolean isPartOfMultiblock(@NotNull Block otherBlock) {
        return adjacentBlocks.contains(new BlockPosition(otherBlock));
    }

    @Override
    public void onMultiblockRefreshed() {
        refreshBlockTextureItem();
    }
}
