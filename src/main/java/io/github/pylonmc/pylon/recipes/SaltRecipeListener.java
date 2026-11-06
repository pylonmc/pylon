package io.github.pylonmc.pylon.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

/**
 * Listens for the {@code pylon:salt} recipe to replace the empty bucket
 */
public class SaltRecipeListener implements Listener {

    private static final NamespacedKey SALT_RECIPE = pylonKey("internal_dummy_salt");

    @EventHandler
    private void onFinishSmelt(FurnaceSmeltEvent event) {
        CookingRecipe<?> recipe = event.getRecipe();
        if (recipe == null || !recipe.getKey().equals(SALT_RECIPE)) return;
        Furnace furnace = (Furnace) event.getBlock().getState(false);
        FurnaceInventory inventory = furnace.getInventory();
        inventory.setSmelting(ItemStack.of(Material.BUCKET));
        furnace.update();
    }
}
