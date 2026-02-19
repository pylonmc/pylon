package io.github.pylonmc.pylon.content.assembling;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarBlockInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class RedstoneSolderingIron extends RebarItem implements RebarBlockInteractor {

    public final String toolType = getSettings().get("tool-type", ConfigAdapter.STRING);
    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final int durability = getSettings().getOrThrow("durability", ConfigAdapter.INTEGER);

    public RedstoneSolderingIron(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0))
        );
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClickBlock(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isLeftClick()
            || event.getHand() != EquipmentSlot.HAND
            || event.useItemInHand() == Event.Result.DENY
            || !(BlockStorage.get(event.getClickedBlock()) instanceof AssemblyTable assemblyTable)) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        } else if (event.getPlayer().hasCooldown(getStack())) {
            return;
        }

        if (assemblyTable.useTool(toolType, event.getPlayer())) {
            getStack().damage(1, event.getPlayer());
            event.getPlayer().setCooldown(PylonKeys.REDSTONE_SOLDERING_IRON, cooldownTicks);
            new ParticleBuilder(Particle.SMOKE)
                    .location(assemblyTable.getWorkspaceCenter())
                    .extra(0)
                    .count(10)
                    .offset(assemblyTable.scale / 4, 0, assemblyTable.scale / 4)
                    .spawn();
        }
    }

    @Override
    public boolean respectCooldown() {
        return false;
    }
}
