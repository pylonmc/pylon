package io.github.pylonmc.pylon.content.tools;

import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class SoulboundRune extends Rune {
    private static final TranslatableComponent SOULBIND_MSG = Component.translatable("pylon.message.soulbound_rune.soulbind-message");
    private static final TranslatableComponent TOOLTIP = Component.translatable("pylon.message.soulbound_rune.tooltip");
    private static final NamespacedKey SOULBOUND_KEY = pylonKey("soulbound");

    public SoulboundRune(ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean isApplicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        return !RebarItem.isRebarItem(target, SoulboundRune.class) && !hasRuneApplied(target);
    }

    @Override
    public void onContactItem(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune, @NotNull ItemStack target) {
        int consume = Math.min(rune.getAmount(), target.getAmount());

        ItemStack soulboundItem = applyRune(target, consume);

        // (N)Either left runes or targets
        int leftRunes = rune.getAmount() - consume;
        int leftTargets = target.getAmount() - consume;

        Location dropLocation = event.getItemDrop().getLocation();
        World world = dropLocation.getWorld();
        if (leftRunes > 0) {
            world.dropItemNaturally(dropLocation, rune.asQuantity(leftRunes)).setGlowing(true);
        }
        if (leftTargets > 0) {
            world.dropItemNaturally(dropLocation, target.asQuantity(leftTargets)).setGlowing(true);
        }
        world.dropItemNaturally(dropLocation, soulboundItem).setGlowing(true);

        target.setAmount(0);
        rune.setAmount(0);
        event.getPlayer().sendMessage(SOULBIND_MSG);
    }

    public static ItemStack applyRune(@NotNull ItemStack itemStack, int amount) {
        return ItemStackBuilder.of(itemStack.asQuantity(amount))
                .editPdc(pdc -> pdc.set(SOULBOUND_KEY, RebarSerializers.BOOLEAN, true))
                .lore(TOOLTIP)
                .build();
    }

    /**
     * Checks if the target already has the soulbound rune applied
     *
     * @return true if the soulbound rune has been used on the item, false otherwise
     */
    public static boolean hasRuneApplied(@NotNull ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().getOrDefault(SOULBOUND_KEY, RebarSerializers.BOOLEAN, false);
    }

   public static class SoulboundRuneListener extends BlockRuneListener {
        @EventHandler(ignoreCancelled = true)
        public void onPlayerDeath(PlayerDeathEvent event) { // exception being generated
            Iterator<ItemStack> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                ItemStack drop = drops.next();
                if (drop != null && hasRuneApplied(drop)) {
                    event.getItemsToKeep().add(drop);
                    drops.remove();
                }
            }
        }
        
        @Override
        protected @NotNull String getKeyPrefix() {
            return "soulbound";
        }

        @Override
        protected boolean hasRuneApplied(@NotNull ItemStack stack) {
            return SoulboundRune.hasRuneApplied(stack);
        }

        @Override
        protected @NotNull ItemStack applyRune(@NotNull ItemStack stack, int amount) {
            return SoulboundRune.applyRune(stack, amount);
        }
    }
}
