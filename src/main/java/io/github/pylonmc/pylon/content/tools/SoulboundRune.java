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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class SoulboundRune extends Rune {
    private static final TranslatableComponent SOULBIND_MSG = Component.translatable("pylon.message.soulbound_rune.soulbind-message");
    private static final TranslatableComponent TOOLTIP = Component.translatable("pylon.message.soulbound_rune.tooltip");

    public static final NamespacedKey SOULBOUND_KEY = pylonKey("soulbound");

    public SoulboundRune(ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean isRuneApplicable(@NotNull Player player, @NotNull Item runeItem, @NotNull Item item) {
        ItemStack targetItemStack = item.getItemStack();
        return !RebarItem.isRebarItem(targetItemStack, SoulboundRune.class) && !targetItemStack.getPersistentDataContainer().has(SOULBOUND_KEY);
    }

    @Override
    public void onRuneApply(@NotNull Player player, @NotNull Item runeItem, @NotNull Item targetItem) {
        ItemStack targetItemStack = targetItem.getItemStack();
        int consumed = Math.min(getStack().getAmount(), targetItemStack.getAmount());

        ItemStack soulbound = ItemStackBuilder.of(targetItemStack.asQuantity(consumed))
                .editPdc(pdc -> pdc.set(SOULBOUND_KEY, RebarSerializers.BOOLEAN, true))
                .lore(TOOLTIP)
                .build();

        int remainingRunes = getStack().getAmount() - consumed;
        int remainingTargets = targetItemStack.getAmount() - consumed;

        Location dropLocation = runeItem.getLocation();
        World world = dropLocation.getWorld();
        if (remainingRunes > 0) {
            world.dropItemNaturally(dropLocation, getStack().asQuantity(remainingRunes)).setGlowing(true);
        }
        if (remainingTargets > 0) {
            world.dropItemNaturally(dropLocation, targetItemStack.asQuantity(remainingTargets)).setGlowing(true);
        }
        world.dropItemNaturally(dropLocation, soulbound).setGlowing(true);

        runeItem.remove();
        targetItem.remove();
        player.sendMessage(SOULBIND_MSG);
    }

    public static class SoulboundRuneListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlayerDeath(PlayerDeathEvent event) {
            Iterator<ItemStack> drops = event.getDrops().iterator();
            while (drops.hasNext()) {
                ItemStack drop = drops.next();
                if (drop != null && drop.getPersistentDataContainer().has(SOULBOUND_KEY)) {
                    event.getItemsToKeep().add(drop);
                    drops.remove();
                }
            }
        }
    }
}
