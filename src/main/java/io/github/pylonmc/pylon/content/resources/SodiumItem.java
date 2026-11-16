package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.interfaces.InventoryTickerRebarItem;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class SodiumItem extends RebarItem implements InventoryTickerRebarItem {

    @Getter
    private final float explosionPower = getSettingOrThrow("explosion-power", ConfigAdapter.FLOAT);

    @Getter
    private final double explosionChancePerSecond = getSettingOrThrow("explosion-chance-per-second", ConfigAdapter.DOUBLE);

    public SodiumItem(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onTick(@NotNull Player player) {
        if (player.getGameMode().isInvulnerable()) return;
        PlayerInventory playerInv = player.getInventory();
        if (RebarItem.isRebarItem(playerInv.getItemInMainHand(), PylonKeys.TONGS) || RebarItem.isRebarItem(playerInv.getItemInOffHand(), PylonKeys.TONGS))
            return;

        double probability = 1 - Math.pow(1 - explosionChancePerSecond, getBaseTickInterval() * RebarConfig.INVENTORY_TICKER_BASE_RATE / 20.0);
        if (ThreadLocalRandom.current().nextDouble() > probability) return;
        player.getWorld().createExplosion(player.getLocation(), explosionPower, true, true);
        getStack().subtract();
    }

    @Override
    public long getBaseTickInterval() {
        return 1;
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("power", explosionPower));
    }
}
