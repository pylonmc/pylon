package io.github.pylonmc.pylon.content.tools;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BrickMold extends RebarItem implements RebarBlockInteractor {

    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public BrickMold(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick()
                || event.useItemInHand() == Event.Result.DENY
                || event.getPlayer().isSneaking()
                || !(BlockStorage.get(event.getClickedBlock()) instanceof Moldable moldable)) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        moldable.doMoldingClick();
        event.getPlayer().setCooldown(getStack(), cooldownTicks);
        new ParticleBuilder(Particle.BLOCK)
                .count(20)
                .offset(0.2, 0.2, 0.2)
                .location(event.getClickedBlock().getLocation().toCenterLocation())
                .data(event.getClickedBlock().getBlockData())
                .spawn();

        if (moldable.isMoldingFinished()) {
            BlockStorage.breakBlock(event.getClickedBlock(), new BlockBreakContext.PluginBreak(event.getClickedBlock(), false));
            event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), moldable.moldingResult());
        }
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0))
        );
    }
}
