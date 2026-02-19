package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.VirtualInventoryLogisticSlot;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;


public class CargoMonitor extends RebarBlock implements
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarCargoBlock {

    private final VirtualInventory inventory = new VirtualInventory(1);

    public final int transferRate = getSettings().getOrThrow("transfer-rate", ConfigAdapter.INTEGER);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.LIGHT_GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder side1Stack = ItemStackBuilder.of(Material.BARREL)
            .addCustomModelDataString(getKey() + ":side1");
    public final ItemStackBuilder side2Stack = ItemStackBuilder.of(Material.BARREL)
            .addCustomModelDataString(getKey() + ":side2");
    public final ItemStackBuilder inputStack = ItemStackBuilder.of(Material.LIME_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":input");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");
    public final ItemStackBuilder projectorStack = ItemStackBuilder.of(Material.PINK_STAINED_GLASS)
            .addCustomModelDataString(getKey() + ":projector");

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
    public CargoMonitor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing(), "input");
        addCargoLogisticGroup(getFacing().getOppositeFace(), "output");
        setCargoTransferRate(transferRate);

        addEntity("main", new ItemDisplayBuilder()
                .itemStack(mainStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.6)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(side1Stack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.45, 0.45, 0.65)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(side2Stack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(Math.PI / 2, Math.PI / 2, 0)
                        .scale(0.65, 0.45, 0.45)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("projector", new ItemDisplayBuilder()
                .itemStack(projectorStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.25, 0)
                        .rotate(0, Math.PI / 4, 0)
                        .scale(0.3, 0.3, 0.3)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("amount", new TextDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.62, 0.0))
                        .scale(0.6, 0.6, 0.6)
                )
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .text(Component.text(0))
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.53, 0.0))
                        .scale(0.15, 0.15, 0.15)
                )
                .itemStack(new ItemStack(Material.BARRIER))
                .billboard(Display.Billboard.VERTICAL)
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("input", new ItemDisplayBuilder()
                .itemStack(inputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, 0.125)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("output", new ItemDisplayBuilder()
                .itemStack(outputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.125)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public CargoMonitor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("input", LogisticGroupType.INPUT, new VirtualInventoryLogisticSlot(inventory, 0));
        createLogisticGroup("output", LogisticGroupType.OUTPUT, new VirtualInventoryLogisticSlot(inventory, 0));
        inventory.addPostUpdateHandler(event -> {
            ItemStack newStack = event.getNewItem();
            if (newStack != null && !newStack.isEmpty()) {
                getHeldEntityOrThrow(ItemDisplay.class, "item").setItemStack(event.getNewItem());
                getHeldEntityOrThrow(TextDisplay.class, "amount").text(Component.text(event.getNewItem().getAmount()));
            } else {
                getHeldEntityOrThrow(ItemDisplay.class, "item").setItemStack(new ItemStack(Material.BARRIER));
                getHeldEntityOrThrow(TextDisplay.class, "amount").text(Component.text(0));
            }
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # x # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', inventory)
                .build();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }
}
