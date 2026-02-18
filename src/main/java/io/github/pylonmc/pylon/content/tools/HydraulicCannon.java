package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicRefuelable;
import io.github.pylonmc.pylon.util.DisplayProjectile;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HydraulicCannon extends RebarItem implements RebarInteractor, HydraulicRefuelable {

    public static final double HYDRAULIC_FLUID_CAPACITY = Settings.get(PylonKeys.PORTABLE_FLUID_TANK_COPPER).getOrThrow("capacity", ConfigAdapter.DOUBLE);
    public static final double DIRTY_HYDRAULIC_FLUID_CAPACITY = Settings.get(PylonKeys.PORTABLE_FLUID_TANK_COPPER).getOrThrow("capacity", ConfigAdapter.DOUBLE);


    private final Config settings = getSettings();

    public final int cooldownTicks = settings.getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);
    public final double hydraulicFluidPerShot = settings.getOrThrow("hydraulic-fluid-per-shot", ConfigAdapter.DOUBLE);
    public final Material projectileMaterial = settings.getOrThrow("projectile.material", ConfigAdapter.MATERIAL);
    public final float projectileThickness = settings.getOrThrow("projectile.thickness", ConfigAdapter.FLOAT);
    public final float projectileLength = settings.getOrThrow("projectile.length", ConfigAdapter.FLOAT);
    public final float projectileSpeedBlocksPerSecond = settings.getOrThrow("projectile.speed-blocks-per-second", ConfigAdapter.FLOAT);
    public final double projectileDamage = settings.getOrThrow("projectile.damage", ConfigAdapter.DOUBLE);
    public final int projectileTickInterval = settings.getOrThrow("projectile.tick-interval", ConfigAdapter.INTEGER);
    public final int projectileLifetimeTicks = settings.getOrThrow("projectile.lifetime-ticks", ConfigAdapter.INTEGER);

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
                RebarArgument.of("hydraulic-fluid", PylonUtils.createFluidAmountBar(
                        getHydraulicFluid(),
                        HYDRAULIC_FLUID_CAPACITY,
                        20,
                        TextColor.fromHexString("#212d99")
                )),
                RebarArgument.of("dirty-hydraulic-fluid", PylonUtils.createFluidAmountBar(
                        getDirtyHydraulicFluid(),
                        DIRTY_HYDRAULIC_FLUID_CAPACITY,
                        20,
                        TextColor.fromHexString("#48459b")
                ))
        );
    }

    @Override
    public void onUsedToRightClick(@NotNull PlayerInteractEvent event) {
        if (getHydraulicFluid() < hydraulicFluidPerShot || getDirtyHydraulicFluidSpace() < hydraulicFluidPerShot) {
            return;
        }

        boolean projectileFound = false;
        for (ItemStack stack : event.getPlayer().getInventory()) {
            if (PylonItems.TIN_PROJECTILE.isSimilar(stack)) {
                stack.subtract();
                projectileFound = true;
                break;
            }
        }
        if (!projectileFound) {
            return;
        }

        setHydraulicFluid(getHydraulicFluid() - hydraulicFluidPerShot);
        setDirtyHydraulicFluid(getDirtyHydraulicFluid() + hydraulicFluidPerShot);

        Player player = event.getPlayer();
        player.setCooldown(getStack(), cooldownTicks);
        Vector direction = player.getEyeLocation().getDirection();
        Location source = player.getEyeLocation()
                .subtract(0, 0.5, 0)
                .add(direction.clone().multiply(1.5));
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
                projectileLifetimeTicks
        ));
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
