package io.github.pylonmc.pylon.content.machines.cargo;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCargoBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.LogisticSlot;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.Map;


public class CreativeItemSource extends RebarBlock
        implements RebarDirectionalBlock, RebarGuiBlock, RebarCargoBlock, RebarVirtualInventoryBlock {

    private final VirtualInventory inventory = new VirtualInventory(1);

    public final ItemStackBuilder mainStack = ItemStackBuilder.of(Material.PINK_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":main");
    public final ItemStackBuilder side1Stack = ItemStackBuilder.of(Material.BEDROCK)
            .addCustomModelDataString(getKey() + ":side1");
    public final ItemStackBuilder side2Stack = ItemStackBuilder.of(Material.BEDROCK)
            .addCustomModelDataString(getKey() + ":side2");
    public final ItemStackBuilder outputStack = ItemStackBuilder.of(Material.RED_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":output");

    @SuppressWarnings("unused")
    public CreativeItemSource(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addCargoLogisticGroup(getFacing().getOppositeFace(), "output");
        setCargoTransferRate(Integer.MAX_VALUE);

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

        addEntity("output", new ItemDisplayBuilder()
                .itemStack(outputStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.125)
                        .scale(0.4, 0.4, 0.4)
                )
                .build(block.getLocation().toCenterLocation())
        );

        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(new Vector3d(0.0, 0.53, 0.0))
                        .scale(0.15, 0.15, 0.15)
                )
                .billboard(Display.Billboard.VERTICAL)
                .build(block.getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public CreativeItemSource(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    private class InfiniteLogisticSlot implements LogisticSlot {

        @Override
        public @Nullable ItemStack getItemStack() {
            return inventory.getItem(0);
        }

        @Override
        public long getAmount() {
            ItemStack stack = inventory.getItem(0);
            return stack == null ? 0 : stack.getMaxStackSize();
        }

        @Override
        public long getMaxAmount(@NotNull ItemStack stack) {
            return stack.getMaxStackSize();
        }

        @Override
        public void set(@Nullable ItemStack stack, long amount) {
            // ignore hehe
        }
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
    public void postInitialise() {
        setDisableBlockTextureEntity(true);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, new InfiniteLogisticSlot());
        inventory.addPostUpdateHandler(event -> getHeldEntityOrThrow(ItemDisplay.class, "item").setItemStack(inventory.getItem(0)));
    }
}
