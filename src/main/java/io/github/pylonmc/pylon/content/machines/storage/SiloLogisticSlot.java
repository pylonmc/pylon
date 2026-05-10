package io.github.pylonmc.pylon.content.machines.storage;

import io.github.pylonmc.rebar.logistics.slot.LogisticSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SiloLogisticSlot implements LogisticSlot {

    private final Silo silo;

    public SiloLogisticSlot(Silo silo) {
        this.silo = silo;
    }

    @Override
    public @Nullable ItemStack getItemStack() {
        return silo.getStack();
    }

    @Override
    public long getAmount() {
        return silo.getAmount();
    }

    @Override
    public long getMaxAmount(@NotNull ItemStack stack) {
        return silo.capacityStacks * stack.getMaxStackSize();
    }

    @Override
    public void set(@Nullable ItemStack stack, long amount) {
        silo.setStack(stack);
        silo.setAmount(amount);
    }
}
