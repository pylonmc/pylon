package io.github.pylonmc.pylon.content.combat;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReactivatedWitherSkull extends RebarItem implements RebarInteractor {
    private final boolean chargedSkulls = getSettings().getOrThrow("charged-skulls", ConfigAdapter.BOOLEAN);
    private final double skullSpeed = getSettings().getOrThrow("skull-speed", ConfigAdapter.DOUBLE);
    private final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    private static final TranslatableComponent trueCharged = Component.translatable("pylon.item.reactivated_wither_skull.charged.true");
    private static final TranslatableComponent falseCharged = Component.translatable("pylon.item.reactivated_wither_skull.charged.false");

    public ReactivatedWitherSkull(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick() || player.isSneaking() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            getStack().damage(1, player);
            player.setCooldown(getStack(), cooldownTicks);
        }

        player.launchProjectile(WitherSkull.class, player.getEyeLocation().getDirection().multiply(skullSpeed / 20.0), witherSkull -> {
            witherSkull.setCharged(chargedSkulls);
        });
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("chargedskulls", chargedSkulls ? trueCharged : falseCharged),
                RebarArgument.of("skullspeed", UnitFormat.BLOCKS_PER_SECOND.format(skullSpeed))
        );
    }
}
