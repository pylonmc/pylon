package io.github.pylonmc.pylon.content.machines.storage;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.logistics.LogisticGroup;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class Silo extends RebarBlock implements RebarLogisticBlock, RebarInteractBlock {

    public static final NamespacedKey STACK_KEY = pylonKey("stack");
    public static final NamespacedKey AMOUNT_KEY = pylonKey("amount");

    public final long capacityStacks = getSettings().getOrThrow("capacity-stacks", ConfigAdapter.LONG);
    public @Nullable ItemStack stack;
    public long amount;

    public static class Item extends RebarItem {

        public final long capacityStacks = getSettings().getOrThrow("capacity-stacks", ConfigAdapter.LONG);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            ItemStack stack = getStack().getPersistentDataContainer().get(STACK_KEY, RebarSerializers.ITEM_STACK);
            long amount = getStack().getPersistentDataContainer().getOrDefault(AMOUNT_KEY, RebarSerializers.LONG, 0L);

            return List.of(
                    RebarArgument.of("capacity", UnitFormat.STACKS.format(capacityStacks)),
                    RebarArgument.of("contents", stack == null
                            ? Component.translatable("pylon.message.silo.empty")
                            : Component.translatable("pylon.message.silo.not-empty")
                            .arguments(
                                    RebarArgument.of("item", stack.displayName()),
                                    RebarArgument.of("amount", amount)
                            )
                    )
            );
        }
    }

    public Silo(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        ItemStack item = context.getItem();
        if (item == null) {
            stack = null;
            amount = 0;
        } else {
            stack = item.getPersistentDataContainer().get(STACK_KEY, RebarSerializers.ITEM_STACK);
            amount = item.getPersistentDataContainer().getOrDefault(AMOUNT_KEY, RebarSerializers.LONG, 0L);
        }
    }

    public Silo(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        stack = pdc.get(STACK_KEY, RebarSerializers.ITEM_STACK);
        amount = pdc.get(AMOUNT_KEY, RebarSerializers.LONG);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, STACK_KEY, RebarSerializers.ITEM_STACK, stack);
        pdc.set(AMOUNT_KEY, RebarSerializers.LONG, amount);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("inventory", new LogisticGroup(LogisticGroupType.BOTH, new SiloLogisticSlot(this)));
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        if (event.getAction().isLeftClick()) {
            if (stack == null || amount == 0) {
                return;
            }

            int toTransfer = event.getPlayer().isSneaking()
                    ? (int) Math.min(amount, stack.getMaxStackSize())
                    : 1;

            amount -= toTransfer;
            event.getPlayer().give(stack.asQuantity(toTransfer));
            if (amount == 0) {
                stack = null;
            }
        }

        if (event.getAction().isRightClick()) {
            ItemStack stackInHand = event.getItem();
            if (stackInHand == null) {
                return;
            }

            if (stack == null) {
                stack = stackInHand.asOne();
                amount = stack.getAmount();
                return;
            }

            if (stack.asOne().equals(stackInHand.asOne()) && amount != getCapacityItems()) {
                int toTransfer = event.getPlayer().isSneaking()
                        ? (int) Math.min(getCapacityItems() - amount, stack.getAmount())
                        : 1;
                amount = Math.min(getCapacityItems(), amount + toTransfer);
            }
        }
    }

    @Override
    public @Nullable ItemStack getDropItem(@NotNull BlockBreakContext context) {
        ItemStack stack = super.getDropItem(context);
        stack.editPersistentDataContainer(pdc -> {
            pdc.set(STACK_KEY, RebarSerializers.ITEM_STACK, stack);
            pdc.set(AMOUNT_KEY, RebarSerializers.LONG, amount);
        });
        return stack;
    }

    public long getCapacityItems() {
        return stack == null ? 0 : capacityStacks * stack.getMaxStackSize();
    }
}
