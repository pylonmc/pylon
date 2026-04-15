package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.util.RebarUtils;
import org.bukkit.entity.ItemDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;


/**
 * Represents a fluid tank block which displays its contents as an item display.
 *
 * Examples: fluid tanks, crucible, mixing pot.
 *
 * `createFluidDisplay` must be called in your constructor.
 */
public interface FluidTankWithDisplayEntity extends RebarFluidTank {

    default void createFluidDisplay() {
        addEntity("fluid", new ItemDisplayBuilder()
            .build(getBlock().getLocation().toCenterLocation().add(0, 0, 0))
        );
    }

    default @NotNull ItemDisplay getFluidDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "fluid");
    }

    @Override
    default void setFluidType(@Nullable RebarFluid fluid) {
        RebarFluidTank.super.setFluidType(fluid);
        getFluidDisplay().setItemStack(fluid == null ? null : fluid.getItem());
    }

    @Override
    default boolean setFluid(double amount) {
        double oldAmount = getFluidAmount();
        boolean wasFluidSet = RebarFluidTank.super.setFluid(amount);
        if (!wasFluidSet || Math.abs(oldAmount - amount) < RebarUtils.FLUID_EPSILON) {
            return false;
        }

        ItemDisplay fluidDisplay = getFluidDisplay();
        Vector3d translation = fluidDisplayTranslation();
        Vector3d scale = fluidDisplayScale();

        float proportion = (float) (amount / getFluidCapacity());

        fluidDisplay.setInterpolationDelay(0);
        fluidDisplay.setInterpolationDuration(RebarConfig.FLUID_TICK_INTERVAL);
        fluidDisplay.setTransformationMatrix(new TransformBuilder()
                .translate(translation.x, translation.y + scale.y * proportion / 2, translation.z)
                .scale(scale.x, scale.y * proportion, scale.z)
                .buildForItemDisplay()
        );

        return true;
    }

    default Vector3d fluidDisplayTranslation() {
        return new Vector3d(0, -0.45, 0);
    }

    default Vector3d fluidDisplayScale() {
        return new Vector3d(0.9, 0.9, 0.9);
    }
}
