package io.github.pylonmc.pylon.content.machines.diesel.machines;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.diesel.DieselRefuelable;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.UpdateReason;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class DieselRefuelingStation extends RebarBlock implements
        RebarFluidBlock,
        RebarDirectionalBlock,
        RebarGuiBlock,
        RebarLogisticBlock,
        RebarVirtualInventoryBlock {

    private final VirtualInventory containerInventory = new VirtualInventory(1);
    public final ItemStackBuilder containerStack = ItemStackBuilder
            .gui(Material.LIME_STAINED_GLASS_PANE, getKey() + ":container")
            .name(Component.translatable("pylon.gui.container"));

    @SuppressWarnings("unused")
    public DieselRefuelingStation(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        addEntity("casing", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.GRAY_STAINED_GLASS)
                        .addCustomModelDataString(getKey() + ":casing"))
                .transformation(new TransformBuilder()
                        .translate(0, 0.1, 0)
                        .scale(0.7))
                .build(getBlock().getLocation().toCenterLocation()));
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(0, 0.25, 0)
                        .scale(0.4))
                .build(getBlock().getLocation().toCenterLocation()));
    }

    @SuppressWarnings("unused")
    public DieselRefuelingStation(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        containerInventory.addPreUpdateHandler(event -> updateDisplayItem(event.getNewItem()));
        containerInventory.addPostUpdateHandler(event -> updateDisplayItem(event.getNewItem()));
        createLogisticGroup(
                "tool",
                LogisticGroupType.BOTH,
                containerInventory);
    }

    public @NotNull ItemDisplay getDisplayItem() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    public void updateDisplayItem(ItemStack newItem) {
        if (RebarItem.fromStack(newItem) instanceof DieselRefuelable) {
            getDisplayItem().setItemStack(newItem.asOne());
            return;
        }
        getDisplayItem().setItemStack(null);
    }

    public @Nullable DieselRefuelable getHeldRefuelableItem() {
        if (RebarItem.fromStack(containerInventory.getItem(0)) instanceof DieselRefuelable refuelable) {
            return refuelable;
        }
        return null;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        DieselRefuelable refuelable = getHeldRefuelableItem();
        if (refuelable == null) {
            return new WailaDisplay(
                    getDefaultWailaTranslationKey().arguments(RebarArgument.of("extra", "")));
        }

        return new WailaDisplay(
                getDefaultWailaTranslationKey().arguments(
                        RebarArgument.of(
                                "extra",
                                Component.translatable("pylon.message.diesel_refueling_station.extra").arguments(
                                        RebarArgument.of("diesel-bar", PylonUtils.createFluidAmountBar(
                                                refuelable.getDiesel(),
                                                refuelable.getDieselCapacity(),
                                                20,
                                                TextColor.fromHexString("#eaa627")))))));
    }

    @Override
    public double fluidAmountRequested(@NotNull RebarFluid fluid) {
        if (!fluid.equals(PylonFluids.BIODIESEL)) {
            return 0.0;
        }
        DieselRefuelable refuelable = getHeldRefuelableItem();
        if (refuelable == null) {
            return 0.0;
        }
        return refuelable.getDieselFluidSpace();
    }

    @Override
    public void onFluidAdded(@NotNull RebarFluid fluid, double amount) {
        DieselRefuelable refuelable = getHeldRefuelableItem();
        refuelable.setDiesel(refuelable.getDiesel() + amount);
        containerInventory.setItem(UpdateReason.SUPPRESSED, 0, ((RebarItem) refuelable).getStack());
    }

    @Override
    public void onBreak(List<ItemStack> drops, BlockBreakContext context) {
        RebarFluidBlock.super.onBreak(drops, context);
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("container", containerInventory);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # H # # # #",
                        "# # # # x # # # #",
                        "# # # # H # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('H', containerStack)
                .addIngredient('x', containerInventory)
                .build();
    }
}
