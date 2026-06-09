package io.github.pylonmc.pylon.content.machines.storage;

import io.github.pylonmc.rebar.recipe.slot.item.ItemSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SiloItemSlot implements ItemSlot {

    private final Silo silo;

    public SiloItemSlot(Silo silo) {
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
