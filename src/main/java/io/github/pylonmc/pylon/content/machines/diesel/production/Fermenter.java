package io.github.pylonmc.pylon.content.machines.diesel.production;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.content.components.ReinforcedGlassCasing;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fermenter extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarFluidBufferBlock {

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double ethanolPerSugarcane = getSettings().getOrThrow("ethanol-per-sugarcane", ConfigAdapter.DOUBLE);
    public final int sugarcaneCapacity = getSettings().getOrThrow("sugarcane-capacity", ConfigAdapter.INTEGER);
    public final double maxEthanolOutputRate = getSettings().getOrThrow("max-ethanol-output-rate", ConfigAdapter.DOUBLE);

    public static final Vector3i INPUT_HATCH = new Vector3i(0, 0, -1);
    public static final Vector3i OUTPUT_HATCH = new Vector3i(0, 0, 1);

    public static class Item extends RebarItem {

        public final double ethanolPerSugarcane = getSettings().getOrThrow("ethanol-per-sugarcane", ConfigAdapter.DOUBLE);
        public final int sugarcaneCapacity = getSettings().getOrThrow("sugarcane-capacity", ConfigAdapter.INTEGER);
        public final double maxEthanolOutputRate = getSettings().getOrThrow("max-ethanol-output-rate", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("ethanol-per-sugarcane", UnitFormat.MILLIBUCKETS.format(ethanolPerSugarcane)),
                    RebarArgument.of("sugarcane-capacity", UnitFormat.ITEMS.format(sugarcaneCapacity)),
                    RebarArgument.of("max-ethanol-output-rate", UnitFormat.MILLIBUCKETS_PER_SECOND.format(maxEthanolOutputRate))
            );
        }
    }

    @SuppressWarnings("unused")
    public Fermenter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(context.getFacing());
        setTickInterval(tickInterval);
        createFluidBuffer(PylonFluids.SUGARCANE, ethanolPerSugarcane * sugarcaneCapacity, false, false);
        addEntity("sugarcane", new ItemDisplayBuilder()
                .itemStack(PylonFluids.SUGARCANE.getItem())
                .transformation(new TransformBuilder()
                        .scale(0, 0, 0))
                .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
        );
    }

    @SuppressWarnings("unused")
    public Fermenter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(-1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(new Vector3i(1, 0, 0), new RebarMultiblockComponent(PylonKeys.BRONZE_FOUNDATION));
        components.put(INPUT_HATCH, new RebarMultiblockComponent(PylonKeys.ITEM_INPUT_HATCH));
        components.put(OUTPUT_HATCH, new RebarMultiblockComponent(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(new Vector3i(-1, 0, -1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(-1, 0, 1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 0, -1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));
        components.put(new Vector3i(1, 0, 1), new RebarMultiblockComponent(PylonKeys.STEEL_SUPPORT_BEAM));

        for (int x = -1; x <= 1; x++) {
            for (int y = 1 ; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    Vector3i position = new Vector3i(x, y, z);
                    if (x == 0 && z == 0) {
                        components.put(position, new RebarMultiblockComponent(PylonKeys.REINFORCED_GLASS));
                    } else {
                        components.put(position, new RebarMultiblockComponent(PylonKeys.REINFORCED_GLASS_CASING));
                    }
                }
            }
        }

        components.remove(new Vector3i(0, 1, 1));

        return components;
    }

    @Override
    public void onMultiblockFormed() {
        RebarSimpleMultiblock.super.onMultiblockFormed();
        onMultiblockRefreshed();
        getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH).setFluidType(PylonFluids.ETHANOL);
        getHeldEntityOrThrow(ItemDisplay.class, "sugarcane").setItemStack(PylonFluids.SUGARCANE.getItem());
    }

    @Override
    public void onMultiblockRefreshed() {
        for (int x = -1; x <= 1; x++) {
            for (int y = 1 ; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    Vector3i position = new Vector3i(x, y, z);
                    ReinforcedGlassCasing casing = getMultiblockComponent(ReinforcedGlassCasing.class, position);
                    if (casing == null) {
                        continue;
                    }

                    if (y == 1) {
                        casing.setPosition(ReinforcedGlassCasing.Position.BOTTOM);
                    } else if (y <= 3) {
                        casing.setPosition(ReinforcedGlassCasing.Position.MIDDLE);
                    } else {
                        casing.setPosition(ReinforcedGlassCasing.Position.TOP);
                    }
                }
            }
        }
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        RebarSimpleMultiblock.super.onMultiblockUnformed(partUnloaded);

        for (int x = -1; x <= 1; x++) {
            for (int y = 1 ; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    Vector3i position = new Vector3i(x, y, z);
                    ReinforcedGlassCasing casing = getMultiblockComponent(ReinforcedGlassCasing.class, position);
                    if (casing == null) {
                        continue;
                    }

                    casing.setPosition(ReinforcedGlassCasing.Position.BOTTOM);
                }
            }
        }

        getHeldEntityOrThrow(ItemDisplay.class, "sugarcane").setItemStack(null);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        ItemInputHatch inputHatch = getMultiblockComponentOrThrow(ItemInputHatch.class, INPUT_HATCH);
        FluidOutputHatch outputHatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, OUTPUT_HATCH);

        ItemStack sugarcane = inputHatch.inventory.getItem(0);
        if (sugarcane != null
                && RebarItem.fromStack(sugarcane) == null
                && sugarcane.getType() == Material.SUGAR_CANE
                && fluidSpaceRemaining(PylonFluids.SUGARCANE) > ethanolPerSugarcane
        ) {
            int max = (int) (fluidSpaceRemaining(PylonFluids.SUGARCANE) / ethanolPerSugarcane);
            int sugarcaneToConsume = Math.min(max, sugarcane.getAmount());
            addFluid(PylonFluids.SUGARCANE, sugarcaneToConsume * ethanolPerSugarcane);
            inputHatch.inventory.setItem(new MachineUpdateReason(), 0, sugarcane.subtract(sugarcaneToConsume));
        }

        double sugarcaneProportion = fluidAmount(PylonFluids.SUGARCANE) / fluidCapacity(PylonFluids.SUGARCANE);
        double outputSpaceRemaining = outputHatch.fluidSpaceRemaining(PylonFluids.ETHANOL);
        double ethanolToOutput = Math.min(outputSpaceRemaining, sugarcaneProportion * maxEthanolOutputRate * getTickInterval() / 20);
        if (ethanolToOutput > 1.0e-6) {
            removeFluid(PylonFluids.SUGARCANE, ethanolToOutput);
            outputHatch.addFluid(PylonFluids.ETHANOL, ethanolToOutput);
        }
    }

    @Override
    public boolean setFluid(@NotNull RebarFluid fluid, double amount) {
        boolean wasSet = RebarFluidBufferBlock.super.setFluid(fluid, amount);
        if (wasSet) {
            double sugarcaneProportion = fluidAmount(PylonFluids.SUGARCANE) / fluidCapacity(PylonFluids.SUGARCANE);
            getHeldEntityOrThrow(ItemDisplay.class, "sugarcane").setTransformationMatrix(
                    new TransformBuilder()
                            .scale(0.7, 4 * sugarcaneProportion, 0.7)
                            .translate(0, 0.5, 0)
                            .buildForItemDisplay()
            );
        }
        return wasSet;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        double sugarcaneProportion = fluidAmount(PylonFluids.SUGARCANE) / fluidCapacity(PylonFluids.SUGARCANE);
        int sugarcaneAmount = sugarcaneProportion < 1.0e-3
                ? 0
                : Math.min(sugarcaneCapacity, (int) (sugarcaneProportion * sugarcaneCapacity) + 1);
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("sugarcane-bar", PylonUtils.createBar(
                        sugarcaneProportion,
                        20, TextColor.color(163, 237, 45)
                )),
                RebarArgument.of("sugarcane-amount", sugarcaneAmount)
        ));
    }
}