package io.github.pylonmc.pylon.util;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.util.WeightedSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

public class ElectricBolt {

    private static final int PARTICLES_PER_BLOCK = 10;
    private static final double POWER_SCALE = 100;

    private final Set<LivingEntity> avoid = new HashSet<>();

    private ElectricBolt() {
    }

    private @Nullable LivingEntity chooseNearestWeighted(Location location, double radius) {
        WeightedSet<LivingEntity> set = new WeightedSet<>();
        for (LivingEntity entity : location.getNearbyLivingEntities(radius)) {
            if (avoid.contains(entity)) continue;
            set.add(new WeightedSet.Element<>(entity, (float) entity.getBoundingBox().getCenter().distance(location.toVector())));
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
        int attempts = 0;
        do {
            if (attempts++ > 10) return;
            if (isLast) {
                direction = originToTarget;
            } else {
                direction = randomBiasedDirection(originToTarget, (float) (Math.PI / 6)).normalize();
            }
        } while (target.getWorld().rayTraceBlocks(origin, Vector.fromJOML(direction).normalize(), direction.length()) != null);
        if (power / POWER_SCALE < direction.length()) return;

        Vector step = Vector.fromJOML(direction).multiply(1.0 / PARTICLES_PER_BLOCK);
        Location pos = origin.clone();
        for (int i = 0; i < PARTICLES_PER_BLOCK; i++) {
            new ParticleBuilder(Particle.WAX_OFF)
                    .location(pos)
                    .offset(0, 0, 0)
                    .count(3)
                    .extra(0.1)
                    .receivers(32, true)
                    .spawn();
            prevPoints.add(pos.clone());
            pos.add(step);
        }

        if (isLast) {
            Vector3f finalDirection = direction;
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                if (!target.isValid()) return;
                for (Location point : prevPoints) {
                    new ParticleBuilder(Particle.WAX_OFF)
                            .location(point)
                            .offset(0.1, 0.1, 0.1)
                            .count(5)
                            .extra(0.8)
                            .receivers(32, true)
                            .spawn();
                }
                target.damage(power / POWER_SCALE * 4, DamageSource.builder(DamageType.LIGHTNING_BOLT).build());
                target.knockback(1, finalDirection.x, finalDirection.z);
            }, 1);
        } else {
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> spawnSection(power - POWER_SCALE, target, pos, prevPoints), 1);
            while (ThreadLocalRandom.current().nextFloat() < 0.25) {
                LivingEntity newTarget = chooseNearestWeighted(pos, power / POWER_SCALE);
                if (newTarget == null) break;
                avoid.add(newTarget);
                Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> spawnSection(power - POWER_SCALE, newTarget, pos, new ArrayList<>(prevPoints)), 1);
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
     * Spawns a single electric bolt targeting an entity within range, randomly chosen with a bias towards nearer entities
     * @param location the location to start at
     * @param power the power (in joules) to set the bolt to
     * @param avoid a set of entities to avoid hitting
     */
    public static void spawn(@NotNull Location location, double power, @NotNull Set<LivingEntity> avoid) {
        ElectricBolt bolt = new ElectricBolt();
        bolt.avoid.addAll(avoid);
        LivingEntity target = bolt.chooseNearestWeighted(location, power / POWER_SCALE);
        if (target == null) return;
        bolt.avoid.add(target);
        bolt.spawnSection(power, target, location, new ArrayList<>());
    }
}
