package io.github.pylonmc.pylon.util;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.entity.RebarEntity;
import io.github.pylonmc.rebar.entity.base.RebarTickingEntity;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.LineBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;


public final class DisplayProjectile extends RebarEntity<ItemDisplay> implements RebarTickingEntity {
    private final Player player;
    private final float thickness;
    private final double damage;
    private final Vector locationStep;
    private int remainingLifetimeTicks;

    public DisplayProjectile(
            Player player,
            Material material,
            Location source,
            @NotNull Vector direction,
            float thickness,
            float length,
            float speedBlockPerSecond,
            double damage,
            int tickInterval,
            int remainingLifetimeTicks
    ) {
        super(PylonKeys.DISPLAY_PROJECTILE, new ItemDisplayBuilder()
            .transformation(new LineBuilder()
                .from(new Vector3d(0, 0, 0))
                .to(direction.clone().multiply(length).toVector3d())
                .thickness(thickness)
                .build()
            )
            .material(material)
            .build(source)
        );
        this.player = player;
        this.thickness = thickness;
        this.damage = damage;
        this.locationStep = direction.clone().multiply(speedBlockPerSecond * tickInterval / 20.0);
        this.remainingLifetimeTicks = remainingLifetimeTicks;
        setTickInterval(tickInterval);
        getEntity().setPersistent(false);
    }

    @Override
    public void tick() {
        ItemDisplay entity = getEntity();

        if (remainingLifetimeTicks <= 0) {
            entity.remove();
            return;
        }

        remainingLifetimeTicks -= getTickInterval();

        entity.setTeleportDuration(getTickInterval());

        Location teleportTo = entity.getLocation().add(locationStep);
        if (!teleportTo.getBlock().isPassable()) {
            teleportTo.getWorld().spawnParticle(
                    Particle.CRIT,
                    teleportTo,
                    15
            );
            entity.remove();
            return;
        }
        getEntity().teleport(teleportTo);

        List<Entity> nearbyEntities = getEntity().getNearbyEntities(thickness * 1.5, thickness * 1.5, thickness * 1.5);
        if (nearbyEntities.isEmpty()) {
            return;
        }

        Optional<Damageable> maybeHitEntity = nearbyEntities.stream()
                .filter(e -> Damageable.class.isInstance(e) && !e.getUniqueId().equals(player.getUniqueId()))
                .map(Damageable.class::cast)
                .findFirst();
        maybeHitEntity.ifPresent(hitEntity -> {
            EntityDamageEvent event = new EntityDamageEvent(
                    hitEntity,
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                    DamageSource.builder(DamageType.ARROW)
                            .withCausingEntity(player)
                            .withDirectEntity(hitEntity)
                            .build(),
                    damage
            );
            if (event.callEvent()) {
                hitEntity.damage(damage);
                hitEntity.setVelocity(locationStep.clone().normalize().multiply(0.2));
            }
            entity.remove();
        });
    }
}
