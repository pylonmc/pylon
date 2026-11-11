package io.github.pylonmc.pylon.content.machines.electricity;

import io.github.pylonmc.pylon.api.item.BatteryRebarItem;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.electricity.nodes.ElectricAcceptorNode;
import io.github.pylonmc.rebar.electricity.nodes.ElectricPortSpec;
import io.github.pylonmc.rebar.electricity.nodes.ElectricProducerNode;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.Map;
import kotlin.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.UpdateReason;

public class BatteryBank extends RebarBlock implements
        ElectricRebarBlock,
        DirectionalRebarBlock,
        InteractRebarBlockHandler,
        VirtualInventoryRebarBlock,
        LogisticRebarBlock {

    private final VirtualInventory batteriesInventory = new VirtualInventory(6);
    private final @Nullable BatteryRebarItem @NotNull [] handles = new BatteryRebarItem[6];

    private ElectricProducerNode output;

    @SuppressWarnings("unused")
    public BatteryBank(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addElectricPort(new ElectricPortSpec(new ElectricAcceptorNode("input", new BlockPosition(block)), RebarUtils.rotateFaceToReference(getFacing(), BlockFace.EAST)));
        addElectricPort(new ElectricPortSpec(new ElectricProducerNode("output", new BlockPosition(block), 0), RebarUtils.rotateFaceToReference(getFacing(), BlockFace.WEST)));
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public BatteryBank(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("batteries", LogisticGroupType.BOTH, batteriesInventory);

        batteriesInventory.addPreUpdateHandler(e -> {
            ItemStack item = e.getNewItem();
            if (item != null && !RebarItem.isRebarItem(item, BatteryRebarItem.class)) {
                e.setCancelled(true);
            }
        });
        batteriesInventory.addPostUpdateHandler(e -> {
            if (e.getUpdateReason() instanceof BatteryItemSyncReason) return;

            ItemStack newItem = e.getNewItem();
            handles[e.getSlot()] = RebarItem.fromStack(newItem, BatteryRebarItem.class);

            ChiseledBookshelf state = (ChiseledBookshelf) getBlock().getState(false);
            state.getInventory().setItem(e.getSlot(), newItem == null ? null : ItemStack.of(Material.BOOK));

            output.setPower(getStoredEnergy());
        });

        for (int i = 0; i < 6; i++) {
            batteriesInventory.setMaxStackSize(i, 1);
            handles[i] = RebarItem.fromStack(batteriesInventory.getItem(i), BatteryRebarItem.class);
        }

        ElectricAcceptorNode input = (ElectricAcceptorNode) getElectricNodeOrThrow("input");
        input.onAccept(this::charge);

        output = (ElectricProducerNode) getElectricNodeOrThrow("output");
        output.onPowerTake(this::discharge);
        output.setPriority(Integer.MAX_VALUE);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("batteries", batteriesInventory);
    }

    @Override
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND) return;

        Location point = event.getInteractionPoint();
        if (point == null) return;
        Vector offset = point.toVector().subtract(getBlock().getLocation().toVector());

        ChiseledBookshelf state = (ChiseledBookshelf) getBlock().getState();
        int slot = state.getSlot(offset);
        ItemStack item = event.getItem();
        if (item == null) {
            if (batteriesInventory.hasItem(slot)) {
                event.getPlayer().getInventory().setItemInMainHand(batteriesInventory.getItem(slot));
                batteriesInventory.setItem(new MachineUpdateReason(), slot, null);
            }
        } else if (RebarItem.isRebarItem(item, BatteryRebarItem.class) && !batteriesInventory.hasItem(slot)) {
            batteriesInventory.setItem(new MachineUpdateReason(), slot, item.asOne());
            item.subtract();
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * @return the amount of energy actually charged. Strictly less than or equal to the param
     */
    public double charge(double energy) {
        double energyLeft = energy;
        for (int i = 0; i < handles.length; i++) {
            BatteryRebarItem battery = handles[i];
            if (battery == null || !battery.isRechargeable()) continue;

            double usedEnergy = Math.min(energyLeft, battery.getCapacity() - battery.getCharge());
            energyLeft -= usedEnergy;
            battery.charge(usedEnergy);
            batteriesInventory.setItem(new BatteryItemSyncReason(), i, battery.getStack());

            if (energyLeft <= 0) break;
        }
        output.setPower(getStoredEnergy());
        return energy - energyLeft;
    }

    /**
     * @return the amount of energy actually discharged. Strictly less than or equal to the param
     */
    public double discharge(double energy) {
        double energyLeft = energy;
        for (int i = 0; i < handles.length; i++) {
            BatteryRebarItem battery = handles[i];
            if (battery == null) continue;

            double usedEnergy = Math.min(energyLeft, battery.getCharge());
            energyLeft -= usedEnergy;
            battery.discharge(usedEnergy);
            batteriesInventory.setItem(new BatteryItemSyncReason(), i, battery.getStack());

            if (energyLeft <= 0) break;
        }
        output.setPower(getStoredEnergy());
        return energy - energyLeft;
    }

    public double getCapacity() {
        double capacity = 0;
        for (BatteryRebarItem item : handles) {
            capacity += item != null ? item.getCapacity() : 0;
        }
        return capacity;
    }

    public double getStoredEnergy() {
        double energy = 0;
        for (BatteryRebarItem item : handles) {
            energy += item != null ? item.getCharge() : 0;
        }
        return energy;
    }

    private static final TextColor BAR_COLOR = TextColor.fromHexString("#f3c600");

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        double storedEnergy = getStoredEnergy();
        double capacity = getCapacity();
        ComponentLike fmtStoredEnergy = UnitFormat.JOULES.format(storedEnergy)
                .selectPrefixAndRescale()
                .decimalPlaces(2);
        ComponentLike fmtCapacity = UnitFormat.JOULES.format(capacity)
                .selectPrefixAndRescale()
                .decimalPlaces(1);
        return WailaDisplay.of(this, player).add(new ProgressBar()
                .barColor(BAR_COLOR)
                .proportion(capacity == 0 ? 0 : storedEnergy / capacity)
                .suffix(Component.text(" ")
                        .append(fmtStoredEnergy)
                        .append(Component.text("/"))
                        .append(fmtCapacity)));
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Pair<@NotNull String, @NotNull Integer>> getBlockTextureProperties() {
        var props = super.getBlockTextureProperties();
        for (int slot = 0; slot < 6; slot++) {
            props.put("slot_" + slot, new Pair<>(String.valueOf(batteriesInventory.hasItem(slot)), 2));
        }
        return props;
    }

    private static class BatteryItemSyncReason implements UpdateReason {
    }
}
