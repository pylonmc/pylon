package io.github.pylonmc.pylon.content.science;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.item.research.Research;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class ResearchPack extends RebarItem implements RebarInteractor {

    public final int points = getSettings().getOrThrow("points", ConfigAdapter.INTEGER);
    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);

    public ResearchPack(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick()
                || (event.hasBlock() && event.useInteractedBlock() == Event.Result.ALLOW)
                || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        long originalPoints = Research.getResearchPoints(player);
        Research.setResearchPoints(player, originalPoints + points);

        int happeningNum = ThreadLocalRandom.current().nextInt(4);
        Component happening = Component.translatable(
                "pylon.message.research_pack.happening." + happeningNum
        );
        player.sendMessage(Component.translatable(
                "pylon.message.research_pack.message",
                RebarArgument.of("happening", happening)
        ));
        player.sendMessage(Component.translatable(
                "pylon.message.gained_research_points",
                RebarArgument.of("points", points),
                RebarArgument.of("total", Research.getResearchPoints(player))
        ));

        event.getPlayer().setCooldown(getStack(), cooldownTicks);
        event.getItem().subtract();
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("points", UnitFormat.RESEARCH_POINTS.format(points)),
                RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0))
        );
    }
}
