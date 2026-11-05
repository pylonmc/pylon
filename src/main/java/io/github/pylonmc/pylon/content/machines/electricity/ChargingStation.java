package io.github.pylonmc.pylon.content.machines.electricity;

import io.github.pylonmc.pylon.api.item.ChargeableRebarItem;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.nodes.ElectricAcceptorNode;
import io.github.pylonmc.rebar.electricity.nodes.ElectricPortSpec;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class ChargingStation extends RebarBlock implements
        GuiRebarBlock,
        VirtualInventoryRebarBlock,
        DirectionalRebarBlock,
        LogisticRebarBlock,
        ElectricRebarBlock,
        TickingRebarBlock {

    public static class Item extends RebarItem {

        @Getter
        private final double chargeRate = getSettingOrThrow("charge-rate", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("rate", UnitFormat.WATTS.format(chargeRate).decimalPlaces(2).selectPrefixAndRescale())
            );
        }
    }

    @Getter
    private final double chargeRate = getSettingOrThrow("charge-rate", ConfigAdapter.DOUBLE);

    private static final NamespacedKey BUFFER_KEY = pylonKey("buffer");
    private double buffer = 0;

    private final VirtualInventory inputInventory = new VirtualInventory(3);
    private final VirtualInventory outputInventory = new VirtualInventory(3);

    @SuppressWarnings("unused")
    public ChargingStation(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));

        addElectricPort(new ElectricPortSpec(new ElectricAcceptorNode("input", new BlockPosition(block)), getFacing()));
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public ChargingStation(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        buffer = pdc.get(BUFFER_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(BUFFER_KEY, RebarSerializers.DOUBLE, buffer);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        for (int i = 0; i < inputInventory.getSize(); i++) {
            inputInventory.setMaxStackSize(i, 1);
        }
        inputInventory.addPreUpdateHandler(event -> {
            if (event.getNewItem() == null) return;
            ChargeableRebarItem chargeable = RebarItem.fromStack(event.getNewItem(), ChargeableRebarItem.class);
            if (!(chargeable != null && chargeable.isRechargeable())) {
                event.setCancelled(true);
            }
        });

        getElectricNodeOrThrow("input", ElectricAcceptorNode.class).onAccept(power -> {
            if (inputInventory.isEmpty()) return 0;
            double newBuffer = Math.min(buffer + power, chargeRate * (getTickInterval() / 20.0));
            double taken = newBuffer - buffer;
            buffer = newBuffer;
            return taken;
        });
    }

    @Override
    public void tick() {
        for (int slot = 0; slot < inputInventory.getSize(); slot++) {
            ItemStack item = inputInventory.getItem(slot);
            ChargeableRebarItem chargeable = RebarItem.fromStack(item, ChargeableRebarItem.class);
            if (chargeable == null) continue;
            double usedCharge = chargeable.charge(buffer);
            buffer -= usedCharge;
            if (chargeable.isFull()) {
                if (outputInventory.addItem(new MachineUpdateReason(), item) == 0) {
                    inputInventory.setItem(new MachineUpdateReason(), slot, null);
                }
            } else {
                inputInventory.setItem(new MachineUpdateReason(), slot, item);
            }
            return;
        }
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("input", inputInventory, "output", outputInventory);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # I I I I I # #",
                        "# # I i i i I # #",
                        "# # I I I I I # #",
                        "# # O O O O O # #",
                        "# # O o o o O # #",
                        "# # O O O O O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('O', GuiItems.output())
                .addIngredient('i', inputInventory)
                .addIngredient('o', outputInventory)
                .build();
    }
}