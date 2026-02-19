package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.persistence.PersistentDataContainerView;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ShimmerMagnet extends RebarItem implements RebarInteractor {
    @Getter
    private final double pickupDistance = getSettings().getOrThrow("pickup-distance", ConfigAdapter.DOUBLE);
    @Getter
    private final double attractForce = getSettings().getOrThrow("attract-force", ConfigAdapter.DOUBLE);

    private static final NamespacedKey ENABLED_KEY = PylonUtils.pylonKey("shimmer_magnet_toggler");

    public ShimmerMagnet(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("pickup-distance", UnitFormat.BLOCKS.format(pickupDistance)));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        Player player = event.getPlayer();
        getStack().editPersistentDataContainer(pdc -> {
            boolean enabled;
            if (!pdc.has(ENABLED_KEY)) {
                enabled = false;
            } else {
                enabled = pdc.get(ENABLED_KEY, PersistentDataType.BOOLEAN) != Boolean.TRUE;
            }

            String targetKey = enabled ? "enabled" : "disabled";
            player.sendMessage(Component.translatable("pylon.message.shimmer_magnet." + targetKey));

            pdc.set(ENABLED_KEY, PersistentDataType.BOOLEAN, enabled);
        });

        CustomModelData data = getStack().getDataOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().build());
        CustomModelData newData = CustomModelData.customModelData()
                .addStrings(data.strings())
                .addFloats(data.floats())
                .addColors(data.colors())
                .addFlag(isEnabled())
                .build();
        getStack().setData(DataComponentTypes.CUSTOM_MODEL_DATA, newData);
    }

    /**
     * Checks if a shimmer magnet is enabled
     *
     * @return true is enabled else otherwise
     */
    public boolean isEnabled() {
        PersistentDataContainerView pdc = getStack().getPersistentDataContainer();

        if (!pdc.has(ENABLED_KEY)) {
            return true;
        }

        return pdc.get(ENABLED_KEY, PersistentDataType.BOOLEAN) == Boolean.TRUE;
    }

    public static class Ticker extends BukkitRunnable {

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Vector playerPosition = player.getLocation().toVector();
                for (var stack : player.getInventory()) {
                    RebarItem rebarItem = fromStack(stack);
                    if (!(rebarItem instanceof ShimmerMagnet shimmerMagnet)) continue;

                    if (!shimmerMagnet.isEnabled()) continue;

                    Collection<Item> nearbyItems = player.getLocation().getNearbyEntitiesByType(
                            Item.class,
                            shimmerMagnet.getPickupDistance()
                    );


                    for (Item item : nearbyItems) {
                        if (item.getPickupDelay() > 0) continue;

                        Vector direction = playerPosition.clone().subtract(item.getLocation().toVector()).normalize();

                        // it is near enough
                        if (direction.distanceSquared(playerPosition) < 0.25) continue;

                        Vector toMove = direction.multiply(shimmerMagnet.getAttractForce());
                        item.setVelocity(toMove);
                    }

                    break;
                }
            }
        }
    }
}
