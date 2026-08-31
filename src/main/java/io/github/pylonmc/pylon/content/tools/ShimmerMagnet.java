package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.InventoryTickerRebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class ShimmerMagnet extends RebarItem implements InteractRebarItemHandler, InventoryTickerRebarItem {
    private static final NamespacedKey ENABLED_KEY = pylonKey("shimmer_magnet_toggler");

    @Getter
    private final double pickupDistance = getSettingOrThrow("pickup-distance", ConfigAdapter.DOUBLE);
    @Getter
    private final double attractForce = getSettingOrThrow("attract-force", ConfigAdapter.DOUBLE);

    public ShimmerMagnet(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("pickup-distance", UnitFormat.BLOCKS.format(pickupDistance)));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        Player player = event.getPlayer();
        getStack().editPersistentDataContainer(pdc -> {
            boolean enabled = !isEnabled();
            player.sendMessage(Component.translatable("pylon.message.shimmer_magnet." + (enabled ? "enabled" : "disabled")));
            pdc.set(ENABLED_KEY, PersistentDataType.BOOLEAN, enabled);
        });

        ItemStackBuilder.of(getStack()).editCustomModelData(data -> {
            data.getFlags().clear();
            data.addFlag(isEnabled());
        });
    }

    /**
     * Checks if a shimmer magnet is enabled
     *
     * @return true is enabled else otherwise
     */
    public boolean isEnabled() {
        return getStack().getPersistentDataContainer().getOrDefault(ENABLED_KEY, PersistentDataType.BOOLEAN, true);
    }

    @Override
    public void onTick(@NotNull Player player) {
        if (!isEnabled()) {
            return;
        }

        Location location = player.getLocation();
        Collection<Item> nearbyItems = location.getNearbyEntitiesByType(
                Item.class,
                getPickupDistance()
        );

        Vector position = location.toVector();
        for (Item item : nearbyItems) {
            if (item.getPickupDelay() > 0) continue;

            Vector direction = position.clone().subtract(item.getLocation().toVector()).normalize();

            // it is near enough
            if (direction.distanceSquared(position) < 0.25) continue;

            Vector toMove = direction.multiply(getAttractForce());
            item.setVelocity(toMove);
        }
    }

    @Override
    public long getBaseTickInterval() {
        return 1;
    }
}
