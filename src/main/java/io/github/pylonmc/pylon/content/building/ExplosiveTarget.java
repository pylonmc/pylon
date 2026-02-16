package io.github.pylonmc.pylon.content.building;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarTargetBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.papermc.paper.event.block.TargetHitEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ExplosiveTarget extends RebarBlock implements RebarTargetBlock {

    public static class Item extends RebarItem {
        private final Double explosivePower = getSettings().getOrThrow("explosive-power", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("explosive-power", explosivePower));
        }
    }

    public final double explosivePower = getSettings().getOrThrow("explosive-power", ConfigAdapter.DOUBLE);
    public final boolean createsFire = getSettings().getOrThrow("creates-fire", ConfigAdapter.BOOLEAN);

    @SuppressWarnings("unused")
    public ExplosiveTarget(Block block, BlockCreateContext context) {
        super(block);
    }

    @SuppressWarnings("unused")
    public ExplosiveTarget(Block block, PersistentDataContainer pdc) {
        super(block);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(@NotNull TargetHitEvent event, @NotNull EventPriority priority) {
        Block hitBlock = event.getHitBlock();
        Preconditions.checkState(hitBlock != null);

        Boolean tntExplodes = hitBlock.getWorld().getGameRuleValue(GameRules.TNT_EXPLODES);
        if (tntExplodes != null && !tntExplodes) {
            return;
        }

        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            if (!hitBlock.getWorld().createExplosion(hitBlock.getLocation(), (float) explosivePower, createsFire)) {
                return;
            }

            BlockStorage.breakBlock(getBlock());
        });
    }
}
