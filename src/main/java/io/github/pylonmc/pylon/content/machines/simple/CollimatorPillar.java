package io.github.pylonmc.pylon.content.machines.simple;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class CollimatorPillar extends RebarBlock implements RebarEntityHolderBlock {

    public CollimatorPillar(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        addEntity("display", new ItemDisplayBuilder()
                .itemStack(
                        ItemStackBuilder.of(Material.YELLOW_TERRACOTTA)
                                .addCustomModelDataString(getKey().toString())
                )
                .transformation(new TransformBuilder()
                        .translate(0, 1.01, 0)
                        .scale(0.35, 2, 0.35))
                .build(getBlock().getLocation().toCenterLocation())
        );
    }

    public CollimatorPillar(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
}
