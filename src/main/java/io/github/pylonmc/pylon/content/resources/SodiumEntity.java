package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.entity.RebarEntity;
import io.github.pylonmc.rebar.entity.interfaces.TickingRebarEntity;
import io.github.pylonmc.rebar.item.RebarItem;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class SodiumEntity extends RebarEntity<Item> implements TickingRebarEntity {

    @Getter
    private final SodiumItem item;

    public SodiumEntity(@NotNull SodiumItem item, @NonNull Item entity) {
        super(PylonKeys.SODIUM_ENTITY, entity);
        this.item = item;
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
    }

    @SuppressWarnings("unused")
    public SodiumEntity(@NonNull Item entity) {
        super(entity);
        this.item = RebarItem.fromStack(entity.getItemStack(), SodiumItem.class);
    }

    @Override
    public void tick() {
        Item entity = getEntity();
        if (!(entity.isInWater() || entity.isInRain())) return;
        double probability = 1 - Math.pow(1 - item.getExplosionChancePerSecond(), getTickInterval() / 20.0);
        if (ThreadLocalRandom.current().nextDouble() > probability) return;
        entity.getWorld().createExplosion(entity, entity.getLocation(), item.getExplosionPower(), true, true);
        remove();
    }

    public static class CreationListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onItemSpawn(ItemSpawnEvent event) {
            SodiumItem item = RebarItem.fromStack(event.getEntity().getItemStack(), SodiumItem.class);
            if (item == null) return;
            EntityStorage.add(new SodiumEntity(item, event.getEntity()));
        }
    }
}
