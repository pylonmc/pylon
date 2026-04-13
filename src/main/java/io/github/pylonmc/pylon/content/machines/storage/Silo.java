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
import io.github.pylonmc.rebar.waila.WailaDisplay;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
    @Getter @Setter private @Nullable ItemStack stack;
    @Getter @Setter private long amount;

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
                                    RebarArgument.of("item", stack.effectiveName()),
                                    RebarArgument.of("amount", amount) ,
                                    RebarArgument.of("capacity", capacityStacks * stack.getMaxStackSize())
                            )
                    )
            );
        }

        public @Nullable ItemStack getSiloStack() {
            return getStack().getPersistentDataContainer().get(STACK_KEY, RebarSerializers.ITEM_STACK);
        }

        public @Nullable Long getSiloAmount() {
            return getStack().getPersistentDataContainer().get(AMOUNT_KEY, RebarSerializers.LONG);
        }

        public void setSiloStack(ItemStack stack) {
            getStack().editPersistentDataContainer(pdc -> {
                pdc.set(STACK_KEY, RebarSerializers.ITEM_STACK, stack);
            });
        }

        public void setSiloAmount(long amount) {
            getStack().editPersistentDataContainer(pdc -> {
                pdc.set(AMOUNT_KEY, RebarSerializers.LONG, amount);
            });
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

        ItemStack stackInHand = event.getItem();
        if (event.getAction().isLeftClick() && (stackInHand == null || !getBlock().isPreferredTool(stackInHand))) {
            if (stack == null || stack.isEmpty() || amount == 0) {
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
            if (stackInHand == null) {
                return;
            }

            if (stack == null || stack.isEmpty()) {
                stack = stackInHand.asOne();
                amount = 0;
            }

            if (stack.isSimilar(stackInHand) && amount != getCapacityItems()) {
                int toTransfer = event.getPlayer().isSneaking()
                        ? (int) Math.min(getCapacityItems() - amount, stackInHand.getAmount())
                        : 1;
                amount += toTransfer;
                stackInHand.subtract(toTransfer);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    @Override
    public @Nullable ItemStack getDropItem(@NotNull BlockBreakContext context) {
        ItemStack stack = super.getDropItem(context);
        stack.editPersistentDataContainer(pdc -> {
            if (this.stack != null) {
                pdc.set(STACK_KEY, RebarSerializers.ITEM_STACK, this.stack);
                pdc.set(AMOUNT_KEY, RebarSerializers.LONG, amount);
            }
        });
        return stack;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("contents", stack == null
                        ? Component.translatable("pylon.message.silo.empty")
                        : Component.translatable("pylon.message.silo.not-empty")
                        .arguments(
                                RebarArgument.of("item", stack.effectiveName()),
                                RebarArgument.of("amount", amount),
                                RebarArgument.of("capacity", getCapacityItems())
                        )
                )
        ));
    }

    public long getCapacityItems() {
        return stack == null ? 0 : capacityStacks * stack.getMaxStackSize();
    }
}
