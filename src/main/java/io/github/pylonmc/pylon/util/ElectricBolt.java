package io.github.pylonmc.pylon.util;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.util.WeightedSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ElectricBolt {

    private static final int PARTICLES_PER_BLOCK = 5;

    private Predicate<LivingEntity> predicate;

    private final @Nullable LivingEntity cause;

    private void addAnotherPredicate(Predicate<LivingEntity> predicate) {
        this.predicate = this.predicate.and(predicate);
    }

    private @Nullable LivingEntity chooseNearestWeighted(Location location, double radius) {
        WeightedSet<LivingEntity> set = new WeightedSet<>();
        for (LivingEntity entity : location.getNearbyLivingEntities(radius)) {
            if (!predicate.test(entity)) continue;
            Location entityLoc = entity.getBoundingBox().getCenter().toLocation(location.getWorld());
            Vector interveningVector = entityLoc.toVector().subtract(location.toVector());
            if (location.getWorld().rayTraceBlocks(location, interveningVector, interveningVector.length()) != null)
                continue;
            set.add(new WeightedSet.Element<>(entity, (float) entityLoc.distance(location)));
        }
        if (set.isEmpty()) return null;
        return set.getRandom();
    }

    private void spawnSection(double power, LivingEntity target, Location origin, List<Location> prevPoints) {
        if (!target.isValid()) return;
        Location targetLocation = target.getBoundingBox().getCenter().toLocation(target.getWorld());
        if (targetLocation.getWorld() != origin.getWorld()) return;

        Vector3f originToTarget = targetLocation.toVector().toVector3f().sub(origin.toVector().toVector3f());
        boolean isLast = originToTarget.lengthSquared() <= 1;
        Vector3f direction;
        float directionLength;
        int attempts = 0;
        do {
            if (attempts++ > 10) return;
            if (isLast) {
                direction = originToTarget;
            } else {
                direction = randomBiasedDirection(originToTarget, (float) (Math.PI / 6)).normalize();
            }
            directionLength = direction.length();
        } while (target.getWorld().rayTraceBlocks(origin, Vector.fromJOML(direction).normalize(), directionLength) != null);
        if (power < directionLength) return;

        Vector step = Vector.fromJOML(direction).multiply(1.0 / PARTICLES_PER_BLOCK);
        Location pos = origin.clone();
        for (int i = 0; i < PARTICLES_PER_BLOCK; i++) {
            new ParticleBuilder(Particle.WAX_OFF)
                    .location(pos)
                    .offset(0, 0, 0)
                    .count(2)
                    .speed(0.1)
                    .receivers(32, true)
                    .spawn();
            prevPoints.add(pos.clone());
            pos.add(step);
        }

        float finalDirectionLength = directionLength;
        if (isLast) {
            Vector3f finalDirection = direction;
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                if (!target.isValid()) return;
                for (Location point : prevPoints) {
                    new ParticleBuilder(Particle.WAX_OFF)
                            .location(point)
                            .offset(0.1, 0.1, 0.1)
                            .count(5)
                            .speed(0.8)
                            .receivers(32, true)
                            .spawn();
                }
                DamageSource.Builder source = DamageSource.builder(DamageType.LIGHTNING_BOLT);
                if (cause != null) {
                    source.withCausingEntity(cause);
                }
                target.damage(power * 2, source.build());
                target.knockback(1, finalDirection.x, finalDirection.z);

                if (power > 0 && target.isDead() && ThreadLocalRandom.current().nextBoolean()) {
                    LivingEntity newTarget = chooseNearestWeighted(pos, power);
                    if (newTarget != null) {
                        addAnotherPredicate(e -> !newTarget.equals(e));
                        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> spawnSection(power - finalDirectionLength, newTarget, pos, new ArrayList<>()), 1);
                    }
                }
            }, 1);
        } else {
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> spawnSection(power - finalDirectionLength, target, pos, prevPoints), 1);
            while (ThreadLocalRandom.current().nextFloat() < 0.25) {
                LivingEntity newTarget = chooseNearestWeighted(pos, power);
                if (newTarget == null) break;
                addAnotherPredicate(e -> !newTarget.equals(e));
                Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> spawnSection(power - finalDirectionLength, newTarget, pos, new ArrayList<>(prevPoints)), 1);
            }
        }
    }

    private static Vector3f randomBiasedDirection(Vector3f direction, float maxAngleRadians) {
        Random rng = ThreadLocalRandom.current();

        Vector3f dir = new Vector3f(direction).normalize();

        float u = rng.nextFloat();
        float v = rng.nextFloat();

        float cosTheta = 1.0f - u * (1.0f - (float) Math.cos(maxAngleRadians));
        float sinTheta = (float) Math.sqrt(1.0f - cosTheta * cosTheta);
        float phi = (float) (2.0 * Math.PI * v);

        Vector3f helper = Math.abs(dir.y) < 0.99f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);

        Vector3f right = helper.cross(dir, new Vector3f()).normalize();
        Vector3f up = dir.cross(right, new Vector3f());

        return new Vector3f(dir)
                .mul(cosTheta)
                .add(right.mul(sinTheta * (float) Math.cos(phi)))
                .add(up.mul(sinTheta * (float) Math.sin(phi)));
    }

    /**
     * Spawns the specified amount of electric bolts targeting entities within range, randomly chosen with a bias towards nearer entities
     *
     * @param location  the location to start at
     * @param count     the number of bolts to spawn
     * @param power     the power to set the bolt to
     * @param cause     the entity that is the ultimate cause of the bolt
     * @param predicate only entities matching this predicate will be considered for electrocution
     */
    public static void spawn(@NotNull Location location, int count, double power, @Nullable LivingEntity cause, @Nullable Predicate<LivingEntity> predicate) {
        ElectricBolt bolt = new ElectricBolt(predicate, cause);
        for (int i = 0; i < count; i++) {
            LivingEntity target = bolt.chooseNearestWeighted(location, power);
            if (target == null) return;
            bolt.addAnotherPredicate(e -> !target.equals(e));
            bolt.spawnSection(power, target, location, new ArrayList<>());
        }
    }
}
