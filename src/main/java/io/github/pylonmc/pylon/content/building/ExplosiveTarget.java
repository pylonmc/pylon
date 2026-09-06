package io.github.pylonmc.pylon.content.building;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TargetRebarBlockHandler;
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


public class ExplosiveTarget extends RebarBlock implements TargetRebarBlockHandler {

    public static class Item extends RebarItem {
        private final Double explosivePower = getSettingOrThrow("explosive-power", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("explosive-power", explosivePower));
        }
    }

    public final double explosivePower = getSettingOrThrow("explosive-power", ConfigAdapter.DOUBLE);
    public final boolean createsFire = getSettingOrThrow("creates-fire", ConfigAdapter.BOOLEAN);

    @SuppressWarnings("unused")
    public ExplosiveTarget(Block block, BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public ExplosiveTarget(Block block, PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTargetHit(@NotNull TargetHitEvent event, @NotNull EventPriority priority) {
        if (!getBlock().getWorld().getGameRuleValue(GameRules.TNT_EXPLODES)) {
            return;
        }

        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            if (!getBlock().getWorld().createExplosion(getBlock().getLocation(), (float) explosivePower, createsFire)) {
                return;
            }

            BlockStorage.breakBlock(getBlock());
        });
    }
}
