package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CoalFiredPurificationTower extends RebarBlock implements
        RebarFluidBufferBlock,
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarProcessor,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarLogisticBlock,
        RebarTickingBlock {

    public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
    public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public static final NamespacedKey FUELS_KEY = pylonKey("coal_fired_purification_tower_fuels");
    public static final RebarRegistry<Fuel> FUELS = new RebarRegistry<>(FUELS_KEY);

    // TODO display fuels
    public record Fuel(
            @NotNull NamespacedKey key,
            @NotNull ItemStack stack,
            int burnTimeSeconds
    ) implements Keyed {
        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }

    static {
        RebarRegistry.addRegistry(FUELS);
        FUELS.register(new Fuel(
                pylonKey("coal"),
                new ItemStack(Material.COAL),
                15
        ));
        FUELS.register(new Fuel(
                pylonKey("coal_block"),
                new ItemStack(Material.COAL_BLOCK),
                135
        ));
        FUELS.register(new Fuel(
                pylonKey("charcoal"),
                new ItemStack(Material.CHARCOAL),
                10
        ));
        FUELS.register(new Fuel(
                pylonKey("charcoal_block"),
                PylonItems.CHARCOAL_BLOCK,
                90
        ));
    }

    private final ItemStackBuilder idleProgressItem = ItemStackBuilder.of(Material.BLAZE_POWDER)
            .name(Component.translatable("pylon.item.coal_fired_purification_tower.progress_item.idle"));
    private final ItemStackBuilder runningProgressItem = ItemStackBuilder.of(Material.BLAZE_POWDER)
            .name(Component.translatable("pylon.item.coal_fired_purification_tower.progress_item.running"));

    private final VirtualInventory inventory = new VirtualInventory(1);

    public static class Item extends RebarItem {

        public final double purificationSpeed = getSettings().getOrThrow("purification-speed", ConfigAdapter.INTEGER);
        public final double purificationEfficiency = getSettings().getOrThrow("purification-efficiency", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("purification_speed", UnitFormat.MILLIBUCKETS_PER_SECOND.format(purificationSpeed)),
                    RebarArgument.of("purification_efficiency", UnitFormat.PERCENT.format(purificationEfficiency * 100)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public CoalFiredPurificationTower(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        setProcessProgressItem(new ProgressItem(GuiItems.background()));
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public CoalFiredPurificationTower(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("fuel",  LogisticGroupType.INPUT, inventory);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # f # # # #",
                        "# # # # x # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('f', getProcessProgressItem())
                .addIngredient('x', inventory)
                .addIngredient('#', GuiItems.background())
                .build();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(new Vector3i(0, 1, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 2, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 3, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 4, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 5, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_GLASS));
        components.put(new Vector3i(0, 6, 0), new RebarMultiblockComponent(PylonKeys.PURIFICATION_TOWER_CAP));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        progressProcess(tickInterval);

        if (!isProcessing()) {
            ItemStack item = inventory.getUnsafeItem(0);
            for (Fuel fuel : FUELS) {
                if (item == null || !item.isSimilar(fuel.stack)) {
                    continue;
                }

                inventory.setItemAmount(null, 0, item.getAmount() - 1);
                getProcessProgressItem().setItem(runningProgressItem);
                startProcess(fuel.burnTimeSeconds * 20);
                break;
            }
        }

        if (!isRunning()) {
            return;
        }

        double toPurify = Math.min(
                // maximum amount of dirty hydraulic fluid that can be purified this tick
                purificationSpeed * getTickInterval() / 20.0,
                Math.min(
                        // amount of dirty hydraulic fluid available
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        // how much dirty hydraulic fluid can be converted without overflowing the hydraulic fluid buffer
                        fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID) / purificationEfficiency
                )
        );

        removeFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, toPurify);
        addFluid(PylonFluids.HYDRAULIC_FLUID, toPurify * purificationEfficiency);
    }

    @Override
    public void onProcessFinished() {
        getProcessProgressItem().setItem(idleProgressItem);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                ))
        ));
    }

    public boolean isRunning() {
        return isProcessing()
                // input buffer not empty
                && fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID) > 1.0e-3
                // output buffer not full
                && fluidSpaceRemaining(PylonFluids.HYDRAULIC_FLUID) > 1.0e-3;
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarFluidBufferBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }
}
