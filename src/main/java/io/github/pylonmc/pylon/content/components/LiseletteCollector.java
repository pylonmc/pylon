package io.github.pylonmc.pylon.content.components;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;


public class LiseletteCollector extends RebarBlock implements RebarEntityHolderBlock {

    public LiseletteCollector(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        addEntity("core", new ItemDisplayBuilder()
                .material(Material.BLACK_CONCRETE)
                .transformation(new TransformBuilder()
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.3, 0.8, 0.3)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("shell", new ItemDisplayBuilder()
                .material(Material.PURPLE_CONCRETE)
                .brightness(15)
                .transformation(new TransformBuilder()
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.5)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
    }

    public LiseletteCollector(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    protected void postLoad() {
        getHeldEntityOrThrow(ItemDisplay.class, "shell")
                .setBrightness(new Display.Brightness(15, 15));
    }
}
