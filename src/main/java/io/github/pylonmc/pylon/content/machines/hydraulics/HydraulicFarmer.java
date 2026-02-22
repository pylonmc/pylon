package io.github.pylonmc.pylon.content.machines.hydraulics;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class HydraulicFarmer extends RebarBlock implements
        RebarTickingBlock,
        RebarFluidBufferBlock,
        RebarDirectionalBlock {

    private static final Set<Material> CROPS_TO_BREAK = EnumSet.of(
            Material.PUMPKIN,
            Material.MELON,
            Material.CARROTS,
            Material.POTATOES,
            Material.WHEAT,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.SUGAR_CANE
    );

    // farmland -> crop item -> block
    private static final Map<Material, Map<Material, Material>> CROP_TO_PLANT = Map.of(
            Material.FARMLAND, Map.of(
                    Material.CARROT, Material.CARROTS,
                    Material.POTATO, Material.POTATOES,
                    Material.WHEAT_SEEDS, Material.WHEAT,
                    Material.BEETROOT_SEEDS, Material.BEETROOTS,
                    Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM,
                    Material.MELON_SEEDS, Material.MELON_STEM
            ),
            Material.SOUL_SAND, Map.of(Material.NETHER_WART, Material.NETHER_WART)
    );

    public final int radius = getSettings().getOrThrow("radius", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double hydraulicFluidUsage = getSettings().getOrThrow("hydraulic-fluid-usage", ConfigAdapter.DOUBLE);
    public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final int radius = getSettings().getOrThrow("radius", ConfigAdapter.INTEGER);
        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
        public final double hydraulicFluidUsage = getSettings().getOrThrow("hydraulic-fluid-usage", ConfigAdapter.DOUBLE);
        public final double buffer = getSettings().getOrThrow("buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("radius", UnitFormat.BLOCKS.format(radius)),
                    RebarArgument.of("tick-interval", UnitFormat.SECONDS.format(tickInterval / 20.0)),
                    RebarArgument.of("hydraulic-fluid-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(hydraulicFluidUsage)),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(buffer))
            );
        }
    }

    @SuppressWarnings("unused")
    public HydraulicFarmer(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setFacing(context.getFacing());

        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);

        int seconds = getTickInterval() / 20;
        createFluidBuffer(PylonFluids.HYDRAULIC_FLUID, buffer, true, false);
        createFluidBuffer(PylonFluids.DIRTY_HYDRAULIC_FLUID, buffer, false, true);
    }

    @SuppressWarnings("unused")
    public HydraulicFarmer(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }


    @Override
    public void tick() {
        double hydraulicFluidUsed = hydraulicFluidUsage * getTickInterval() / 20.0;

        if (fluidAmount(PylonFluids.HYDRAULIC_FLUID) < hydraulicFluidUsed
                || fluidSpaceRemaining(PylonFluids.DIRTY_HYDRAULIC_FLUID) < hydraulicFluidUsed
        ) {
            return;
        }

        var tiles = getFarmingTiles();
        // Attempt to break crops
        for (var tile : tiles) {
            Block cropBlock = tile.getCropBlock();
            Material cropType = cropBlock.getType();

            if (!CROPS_TO_BREAK.contains(cropType)) continue;

            if (cropBlock.getType() != Material.SUGAR_CANE && cropBlock.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    cropBlock.breakNaturally();
                    removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidUsed);
                    addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidUsed);
                    return;
                }
            } else {
                cropBlock.breakNaturally();
                removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidUsed);
                addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidUsed);
                return;
            }
        }

        List<ItemStack> stacks = getItemsFromChest();
        Map<Material, ItemStack> tileToCrop = new HashMap<>();

        for (ItemStack stack : stacks) {
            if (stack == null) continue;
            if (tileToCrop.size() == 2) break;

            Material type = stack.getType();
            Material farmland = getFarmlandFromItem(type);
            if (farmland != null) {
                if (tileToCrop.containsKey(farmland)) continue;
                tileToCrop.put(farmland, stack);
            }
        }

        if (tileToCrop.isEmpty()) {
            return;
        }

        // Attempt plant crops
        for (var tile : tiles) {
            Block cropBlock = tile.getCropBlock();
            Material type = tile.getType();

            if (!isValidFarmland(type)) {
                continue;
            }

            if (!cropBlock.isEmpty()) {
                continue;
            }

            ItemStack planted = tileToCrop.get(type);
            if (planted == null) {
                continue;
            }

            Material blockPlant = CROP_TO_PLANT.get(type).get(planted.getType());
            cropBlock.setType(blockPlant);

            planted.subtract();
            removeFluid(PylonFluids.HYDRAULIC_FLUID, hydraulicFluidUsed);
            addFluid(PylonFluids.DIRTY_HYDRAULIC_FLUID, hydraulicFluidUsed);
            return;
        }
    }

    private static class FarmingTile {
        @Getter
        private final Block cropBlock;
        private Material type = null;

        private FarmingTile(Block cropBlock) {
            this.cropBlock = cropBlock;
        }

        // lazy access
        public Material getType() {
            if (type == null) {
                Block farmlandBlock = cropBlock.getRelative(BlockFace.DOWN);
                this.type = farmlandBlock.getType();
            }

            return type;
        }
    }

    private static boolean isValidFarmland(Material type) {
        return type == Material.SOUL_SAND || type == Material.FARMLAND;
    }

    private static Material getFarmlandFromItem(Material crop) {
        for (var entry : CROP_TO_PLANT.entrySet()) {
            if (entry.getValue().containsKey(crop)) return entry.getKey();
        }

        return null;
    }

    private List<FarmingTile> getFarmingTiles() {
        int diameter = 2 * radius + 1;
        ArrayList<FarmingTile> tiles = new ArrayList<>(diameter * diameter);

        WorldBorder border = getBlock().getWorld().getWorldBorder();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block cropBlock = getBlock().getRelative(x, 0, z);
                if (border.isInside(cropBlock.getLocation())) {

                    tiles.add(
                            new FarmingTile(
                                    cropBlock
                            )
                    );
                }
            }
        }

        return tiles;
    }

    private List<ItemStack> getItemsFromChest() {
        BlockState stateAbove = getBlock().getRelative(0, 1, 0).getState();
        if (!(stateAbove instanceof BlockInventoryHolder blockInventoryHolder)) {
            return Collections.emptyList();
        }

        ArrayList<ItemStack> stacks = new ArrayList<>(54);
        InventoryHolder holder = blockInventoryHolder.getInventory().getHolder();
        if (holder == null) {
            return Collections.emptyList();
        }


        if (holder instanceof DoubleChest doubleChest) {
            InventoryHolder leftSide = doubleChest.getLeftSide();
            if (leftSide != null) {
                for (ItemStack item : leftSide.getInventory()) {
                    stacks.add(item);
                }
            }

            InventoryHolder rightSide = doubleChest.getRightSide();
            if (rightSide != null) {
                for (ItemStack item : rightSide.getInventory()) {
                    stacks.add(item);
                }
            }
        } else {
            for (ItemStack item : holder.getInventory()) {
                stacks.add(item);
            }
        }
        return stacks;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("input-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#212d99")
                )),
                RebarArgument.of("output-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        fluidCapacity(PylonFluids.DIRTY_HYDRAULIC_FLUID),
                        20,
                        TextColor.fromHexString("#48459b")
                ))
        ));
    }
}
