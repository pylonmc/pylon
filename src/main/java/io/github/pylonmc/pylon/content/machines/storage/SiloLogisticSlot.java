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
        return silo.stack;
    }

    @Override
    public long getAmount() {
        return silo.amount;
    }

    @Override
    public long getMaxAmount(@NotNull ItemStack stack) {
        return silo.capacityStacks * stack.getMaxStackSize();
    }

    @Override
    public void set(@Nullable ItemStack stack, long amount) {
        silo.stack = stack;
        silo.amount = amount;
    }
}
