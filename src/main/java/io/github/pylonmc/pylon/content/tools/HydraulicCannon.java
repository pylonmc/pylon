package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicRefuelable;
import io.github.pylonmc.pylon.util.DisplayProjectile;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HydraulicCannon extends RebarItem implements InteractRebarItemHandler, HydraulicRefuelable {

    public static final double HYDRAULIC_FLUID_CAPACITY = ConfigSection.fromSettings(PylonKeys.PORTABLE_FLUID_TANK_COPPER)
            .getOrThrow("capacity", ConfigAdapter.DOUBLE);
    public static final double DIRTY_HYDRAULIC_FLUID_CAPACITY = ConfigSection.fromSettings(PylonKeys.PORTABLE_FLUID_TANK_COPPER)
            .getOrThrow("capacity", ConfigAdapter.DOUBLE);

    public final int cooldownTicks = getSettingOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final double recoilVelocity = getSettingOrThrow("recoil-velocity", ConfigAdapter.DOUBLE);
    public final double hydraulicFluidPerShot = getSettingOrThrow("hydraulic-fluid-per-shot", ConfigAdapter.DOUBLE);
    public final Material projectileMaterial = getSettingOrThrow("projectile.material", ConfigAdapter.MATERIAL);
    public final float projectileThickness = getSettingOrThrow("projectile.thickness", ConfigAdapter.FLOAT);
    public final float projectileLength = getSettingOrThrow("projectile.length", ConfigAdapter.FLOAT);
    public final float projectileSpeedBlocksPerSecond = getSettingOrThrow("projectile.speed-blocks-per-second", ConfigAdapter.FLOAT);
    public final double projectileDamage = getSettingOrThrow("projectile.damage", ConfigAdapter.DOUBLE);
    public final int projectileTickInterval = getSettingOrThrow("projectile.tick-interval", ConfigAdapter.INTEGER);
    public final int projectileLifetimeTicks = getSettingOrThrow("projectile.lifetime-ticks", ConfigAdapter.INTEGER);
    public final RandomizedSound sound = getSettingOrThrow("sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound emptySound = getSettingOrThrow("empty-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound fullSound = getSettingOrThrow("full-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound noAmmoSound = getSettingOrThrow("no-ammo-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound hitSound = getSettingOrThrow("hit-sound", ConfigAdapter.RANDOMIZED_SOUND);
    public final RandomizedSound playerHitSound = getSettingOrThrow("player-hit-sound", ConfigAdapter.RANDOMIZED_SOUND);

    @SuppressWarnings("unused")
    public HydraulicCannon(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("damage", UnitFormat.HEARTS.format(projectileDamage)),
                RebarArgument.of("cooldown", UnitFormat.SECONDS.format(cooldownTicks / 20.0)),
                RebarArgument.of("range", UnitFormat.BLOCKS.format(Math.round(projectileSpeedBlocksPerSecond * projectileLifetimeTicks / 20.0))),
                RebarArgument.of("speed", UnitFormat.BLOCKS_PER_SECOND.format(projectileSpeedBlocksPerSecond)),
                RebarArgument.of("hydraulic-fluid-per-shot", UnitFormat.MILLIBUCKETS.format(hydraulicFluidPerShot)),
                RebarArgument.of("hydraulic-fluid", ProgressBar.fluidContents(
                        PylonFluids.HYDRAULIC_FLUID,
                        HYDRAULIC_FLUID_CAPACITY,
                        getHydraulicFluid()
                )),
                RebarArgument.of("dirty-hydraulic-fluid", ProgressBar.fluidContents(
                        PylonFluids.DIRTY_HYDRAULIC_FLUID,
                        DIRTY_HYDRAULIC_FLUID_CAPACITY,
                        getDirtyHydraulicFluid()
                ))
        );
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        Location source = player.getEyeLocation();
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        if (getHydraulicFluid() < hydraulicFluidPerShot) {
            player.sendMessage(Component.translatable("pylon.message.hydraulic-cannon.empty"));
            source.getWorld().playSound(emptySound.create(), source.getX(), source.getY(), source.getZ());
            return;
        }

        if (getDirtyHydraulicFluidSpace() < hydraulicFluidPerShot) {
            player.sendMessage(Component.translatable("pylon.message.hydraulic-cannon.full"));
            source.getWorld().playSound(fullSound.create(), source.getX(), source.getY(), source.getZ());
            return;
        }

        ItemStack projectile = null;
        for (ItemStack stack : event.getPlayer().getInventory()) {
            RebarItemSchema schema = RebarItemSchema.fromStack(stack);
            if (schema != null && schema.getKey().equals(PylonKeys.TIN_PROJECTILE)) {
                projectile = stack;
                break;
            }
        }

        if (projectile == null) {
            player.sendMessage(Component.translatable("pylon.message.hydraulic-cannon.no-ammo"));
            source.getWorld().playSound(noAmmoSound.create(), source.getX(), source.getY(), source.getZ());
            return;
        }

        projectile.subtract();
        setHydraulicFluid(getHydraulicFluid() - hydraulicFluidPerShot);
        setDirtyHydraulicFluid(getDirtyHydraulicFluid() + hydraulicFluidPerShot);

        player.setCooldown(getStack(), cooldownTicks);
        Vector direction = player.getEyeLocation().getDirection();
        EntityStorage.add(new DisplayProjectile(
                player,
                projectileMaterial,
                source,
                direction,
                projectileThickness,
                projectileLength,
                projectileSpeedBlocksPerSecond,
                projectileDamage,
                projectileTickInterval,
                projectileLifetimeTicks,
                hitSound.create(),
                playerHitSound.create()
        ));

        player.getWorld().spawnParticle(
                Particle.FLAME,
                source.subtract(0, 0.4, 0).add(direction.clone().multiply(0.25)),
                6,
                0.3, 0.3, 0.3,
                0.01
        );

        source.getWorld().playSound(sound.create(), source.getX(), source.getY(), source.getZ());
        player.setVelocity(player.getVelocity().subtract(direction.clone().multiply(recoilVelocity)));
    }

    @Override
    public double getHydraulicFluid() {
        return getStack().getPersistentDataContainer().get(PylonFluids.HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE);
    }

    @Override
    public double getDirtyHydraulicFluid() {
        return getStack().getPersistentDataContainer().get(PylonFluids.DIRTY_HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE);
    }

    @Override
    public void setHydraulicFluid(double amount) {
        getStack().editPersistentDataContainer(pdc -> {
            pdc.set(PylonFluids.HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, amount);
        });
    }

    @Override
    public void setDirtyHydraulicFluid(double amount) {
        getStack().editPersistentDataContainer(pdc -> {
            pdc.set(PylonFluids.DIRTY_HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, amount);
        });
    }

    @Override
    public double getHydraulicFluidCapacity() {
        return HYDRAULIC_FLUID_CAPACITY;
    }

    @Override
    public double getDirtyHydraulicFluidCapacity() {
        return DIRTY_HYDRAULIC_FLUID_CAPACITY;
    }
}
