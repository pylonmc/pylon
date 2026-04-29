package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricProducerBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class ElectricityOutputHatch extends RebarBlock implements RebarElectricProducerBlock {

    @SuppressWarnings("unused")
    public ElectricityOutputHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
    }

    @SuppressWarnings("unused")
    public ElectricityOutputHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }
}
