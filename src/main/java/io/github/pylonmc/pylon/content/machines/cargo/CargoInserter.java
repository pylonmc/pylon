package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityCulledBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.cargo.CargoDuct;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.RebarCargoConnectEvent;
import io.github.pylonmc.rebar.event.RebarCargoDisconnectEvent;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroup;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class CargoInserter extends CargoInteractor implements
        RebarCargoBlock,
        RebarGuiBlock,
        RebarEntityCulledBlock
{

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder ductStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":duct");

    public static class Item extends RebarItem {

        public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of(
                            "transfer-rate",
                            UnitFormat.ITEMS_PER_SECOND.format(RebarCargoBlock.cargoItemsTransferredPerSecond(transferRate))
                    )
            );
        }
    }

    @SuppressWarnings("unused")
    public CargoInserter(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing(), "input");
        for (BlockFace face : RebarUtils.perpendicularImmediateFaces(getFacing())) {
            addCargoLogisticGroup(face, "input");
        }
        setCargoTransferRate(transferRate);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.4)
                        .scale(0.65, 0.65, 0.2)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("input", new ItemDisplayBuilder()
                .itemStack(inputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.3)
                        .scale(0.45, 0.45, 0.05)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("duct", new ItemDisplayBuilder()
                .itemStack(ductStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.0625)
                        .scale(0.35, 0.35, 0.475)
                )
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public CargoInserter(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
    }

    @Override
    public void onDuctConnected(@NotNull RebarCargoConnectEvent event) {
        // Remove all faces that aren't to the connected block - this will make sure only
        // one duct is connected at a time
        for (BlockFace face : getCargoLogisticGroups().keySet()) {
            if (!getBlock().getRelative(face).equals(event.getBlock1().getBlock()) && !getBlock().getRelative(face).equals(event.getBlock2().getBlock())) {
                removeCargoLogisticGroup(face);
            }
        }
    }

    @Override
    public void onDuctDisconnected(@NotNull RebarCargoDisconnectEvent event) {
        // Allow connecting to all faces now that there are zero connections
        List<BlockFace> faces = RebarUtils.perpendicularImmediateFaces(getFacing());
        faces.add(getFacing());
        for (BlockFace face : faces) {
            addCargoLogisticGroup(face, Objects.requireNonNullElse(targetLogisticGroup, "placeholder_unused_kind_of_a_hack"));
        }
        for (BlockFace face : faces) {
            if (BlockStorage.get(getBlock().getRelative(face)) instanceof CargoDuct duct) {
                duct.updateConnectedFaces();
            }
        }
    }

    @Override
    public @NotNull Map<String, LogisticGroup> getLogisticGroups() {
        return targetGroups;
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # i # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', new InventoryCycleItem())
                .build();
    }

    @Override
    public void setTargetLogisticGroup(String targetLogisticGroup) {
        this.targetLogisticGroup = targetLogisticGroup;
        for (BlockFace face : getCargoLogisticGroups().keySet()) {
            removeCargoLogisticGroup(face);
            // Slight hack: Set the logistic group to 'none' (which should not exist)
            addCargoLogisticGroup(face, targetLogisticGroup == null ? "none" : targetLogisticGroup);
        }
    }

    @Override
    public boolean isValidGroup(@NotNull LogisticGroup group) {
        return group.getSlotType() == LogisticGroupType.BOTH || group.getSlotType() == LogisticGroupType.INPUT;
    }

    @Override
    public @NotNull Iterable<UUID> getCulledEntityIds() {
        return getHeldEntities().values();
    }
}
