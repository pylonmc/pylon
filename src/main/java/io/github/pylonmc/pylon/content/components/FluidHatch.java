package io.github.pylonmc.pylon.content.components;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.RebarItemSchema;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.waila.Waila;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.*;
import kotlin.Pair;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public abstract class FluidHatch extends RebarBlock implements
        RebarFluidBlock,
        RebarSimpleMultiblock,
        RebarDirectionalBlock {

    private static final NamespacedKey ALLOWED_FLUIDS_KEY = pylonKey("allowed_fluids");
    private static final NamespacedKey FLUID_KEY = pylonKey("fluid");
    private static final NamespacedKey FLUID_AMOUNT_KEY = pylonKey("fluid_amount");
    private static final NamespacedKey CAPACITY_KEY = pylonKey("capacity");

    private static MixedMultiblockComponent component = null;

    @Getter
    private Set<RebarFluid> allowedFluids = new HashSet<>();

    @Getter
    private @Nullable RebarFluid fluid;

    @Getter
    private double fluidAmount = 0;

    @Getter
    private double capacity = 0;

    static {
        // run on first tick after all addons registered
        Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            List<RebarMultiblockComponent> components = new ArrayList<>();
            for (RebarItemSchema schema : RebarRegistry.ITEMS) {
                if (RebarItem.fromStack(schema.createNewItem()) instanceof FluidTankCasing.Item) {
                    components.add(new RebarMultiblockComponent(schema.getKey()));
                }
            }
            component = new MixedMultiblockComponent(components);
        }, 0);
    }

    public FluidHatch(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        fluid = null;
        setFacing(context.getFacing());
        addEntity("fluid", new ItemDisplayBuilder()
                .transformation(new TransformBuilder().scale(0))
                .build(getBlock().getLocation().toCenterLocation().add(0, 1, 0))
        );
    }

    @SuppressWarnings("DataFlowIssue")
    public FluidHatch(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        allowedFluids = pdc.get(ALLOWED_FLUIDS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.REBAR_FLUID));
        fluid = pdc.get(FLUID_KEY, RebarSerializers.REBAR_FLUID);
        fluidAmount = pdc.get(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE);
        capacity = pdc.get(CAPACITY_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(ALLOWED_FLUIDS_KEY, RebarSerializers.SET.setTypeFrom(RebarSerializers.REBAR_FLUID), allowedFluids);
        RebarUtils.setNullable(pdc, FLUID_KEY, RebarSerializers.REBAR_FLUID, fluid);
        pdc.set(FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, fluidAmount);
        pdc.set(CAPACITY_KEY, RebarSerializers.DOUBLE, capacity);
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        components.put(new Vector3i(0, 1, 0), component);
        return components;
    }

    @Override
    public boolean checkFormed() {
        boolean formed = RebarSimpleMultiblock.super.checkFormed();
        if (formed) {
            FluidTankCasing casing = BlockStorage.getAs(FluidTankCasing.class, getBlock().getRelative(BlockFace.UP));
            Preconditions.checkState(casing != null);
            setCapacity(casing.capacity);
        }
        return formed;
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);
        Waila.removeWailaOverride(getBlock().getRelative(BlockFace.UP));
        if (fluid != null) {
            setCapacity(0);
            getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                    .scale(0, 0, 0)
                    .buildForItemDisplay()
            );
        }
    }

    @Override
    public @NotNull List<@NotNull Pair<RebarFluid, Double>> getSuppliedFluids() {
        if (fluid == null || fluidAmount <= 1e-6) {
            return List.of();
        }
        return List.of(new Pair<>(fluid, fluidAmount));
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (Objects.equals(this.fluid, fluid)) {
            return capacity - fluidAmount;
        } else if (allowedFluids.contains(fluid)) {
            return capacity;
        } else {
            return 0;
        }
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        setFluid(fluid, fluidAmount + amount);
    }

    @Override
    public void onFluidRemoved(@NotNull RebarFluid fluid, double amount) {
        setFluid(fluid, fluidAmount - amount);
    }

    public void addFluid(@NotNull RebarFluid fluid, double amount) {
        onFluidAdded(fluid, amount);
    }

    public void removeFluid(@NotNull RebarFluid fluid, double amount) {
        onFluidRemoved(fluid, amount);
    }

    public void setFluid(@NotNull RebarFluid fluid, double amount) {
        this.fluid = fluid;
        this.fluidAmount = Math.min(amount, capacity);
        float scale = (float) (0.9 * fluidAmount / capacity);
        if (scale < 1.0e-9) {
            this.fluid = null;
            this.fluidAmount = 0;
            getFluidDisplay().setItemStack(null);
        } else {
            getFluidDisplay().setItemStack(fluid.getItem());
        }
        getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                .translate(0.0, -0.45 + scale / 2, 0.0)
                .scale(0.9, scale, 0.9)
                .buildForItemDisplay()
        );
    }

    public double getFluidSpaceRemaining() {
        if (fluid == null) {
            return capacity;
        } else {
            return capacity - fluidAmount;
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        Component info;
        if (!isFormedAndFullyLoaded()) {
            info = Component.translatable("pylon.message.fluid_hatch.no_casing");
        } else if (fluid == null) {
            info = Component.translatable("pylon.message.fluid_hatch.empty");
        } else {
            info = Component.translatable("pylon.message.fluid_hatch.working")
                    .arguments(
                            RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                                    fluidAmount,
                                    capacity,
                                    20,
                                    TextColor.color(200, 255, 255)
                            )),
                            RebarArgument.of("fluid", fluid.getName())
                    );
        }
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("info", info)
        ));
    }

    public void setAllowedFluids(@NotNull RebarFluid @NotNull ... fluids) {
        setAllowedFluids(Set.of(fluids));
    }

    public void setAllowedFluids(@NotNull Set<RebarFluid> fluids) {
        this.allowedFluids = fluids;

        if (this.fluid != null && !fluids.contains(this.fluid)) {
            fluid = null;
            setCapacity(0);
            getFluidDisplay().setTransformationMatrix(new TransformBuilder()
                    .scale(0, 0, 0)
                    .buildForItemDisplay()
            );
            this.fluid = null;
        }
        checkFormed();
    }

    private void setCapacity(double capacity) {
        this.capacity = capacity;
        this.fluidAmount = Math.min(this.fluidAmount, capacity);
    }

    public @NotNull ItemDisplay getFluidDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "fluid");
    }

    @Override
    public void postBreak(@NotNull BlockBreakContext context) {
        Waila.removeWailaOverride(getBlock().getRelative(BlockFace.UP));
    }
}
