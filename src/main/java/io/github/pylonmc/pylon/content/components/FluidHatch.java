package io.github.pylonmc.pylon.content.components;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.pylon.content.machines.fluid.multiblock.FluidTankCasingComponent;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class FluidHatch extends RebarBlock implements
        FluidBufferRebarBlock,
        SimpleRebarMultiblock,
        DirectionalRebarBlock {

    private static final NamespacedKey FLUID_KEY = pylonKey("fluid");
    public @Nullable RebarFluid fluid;

    public FluidHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        fluid = null;
        setFacing(context.getFacing());
        addEntity("fluid", new ItemDisplayBuilder()
                .transformation(new TransformBuilder().scale(0))
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
    }

    public FluidHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        fluid = pdc.get(FLUID_KEY, RebarSerializers.REBAR_FLUID);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, FLUID_KEY, RebarSerializers.REBAR_FLUID, fluid);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        components.put(new Vector3i(0, 1, 0), FluidTankCasingComponent.INSTANCE);
        return components;
    }

    @Override
    public boolean checkFormed() {
        boolean formed = SimpleRebarMultiblock.super.checkFormed();
        if (formed) {
            FluidTankCasing casing = BlockStorage.getAs(FluidTankCasing.class, getBlock().getRelative(BlockFace.UP));
            Preconditions.checkState(casing != null);
            Waila.addWailaOverride(casing.getBlock(), this::getWaila);
            if (fluid != null) {
                setFluidCapacity(fluid, casing.capacity);
            }
        }
        return formed;
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        SimpleRebarMultiblock.super.onMultiblockUnformed(partUnloaded);
        Waila.removeWailaOverride(getBlock().getRelative(BlockFace.UP));
        if (fluid != null) {
            setFluidCapacity(fluid, 0);
            getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                    .scale(0, 0, 0)
                    .buildForItemDisplay()
            );
        }
    }

    @Override
    public boolean setFluid(@NotNull RebarFluid fluid, double amount) {
        boolean result = FluidBufferRebarBlock.super.setFluid(fluid, amount);
        float scale = (float) (0.9 * fluidAmount(fluid) / fluidCapacity(fluid));
        if (scale < RebarUtils.FLUID_EPSILON) {
            getFluidDisplay().setItemStack(null);
        } else {
            getFluidDisplay().setItemStack(fluid.getItem());
        }
        getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                .translate(0.0, -0.45 + scale / 2, 0.0)
                .scale(0.9, scale, 0.9)
                .buildForItemDisplay()
        );
        return result;
    }

    public double fluidAmount() {
        return fluid == null
                ? 0.0
                : fluidAmount(fluid);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        
        if (!isFormedAndFullyLoaded()) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_casing"));
        } else if (fluid == null) {
            display.add(Component.translatable("pylon.message.fluid_hatch.no_fluid"));
        } else {
            display.add(ProgressBar.fluidContentsWithName(
                    fluid,
                    fluidCapacity(fluid),
                    fluidAmount(fluid)
            ));
        }
        
        return display;
    }

    public void setFluidType(@Nullable RebarFluid fluid) {
        if (Objects.equals(this.fluid, fluid)) {
            return;
        }
        
        if (this.fluid != null) {
            deleteFluidBuffer(this.fluid);
            getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                    .scale(0, 0, 0)
                    .buildForItemDisplay()
            );
        }
        this.fluid = fluid;
        if (fluid != null) {
            createFluidBuffer(fluid, 0, true, true);
        }
        if (isFormedAndFullyLoaded() && fluid != null) {
            FluidTankCasing casing = BlockStorage.getAs(FluidTankCasing.class, getBlock().getRelative(BlockFace.UP));
            Preconditions.checkState(casing != null);
            setFluidCapacity(fluid, casing.capacity);
        }
    }

    public @NotNull ItemDisplay getFluidDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "fluid");
    }

    @Override
    public void onPostBlockBreak(@NotNull BlockBreakContext context) {
        FluidBufferRebarBlock.super.onPostBlockBreak(context);
        Waila.removeWailaOverride(getBlock().getRelative(BlockFace.UP));
    }
}
