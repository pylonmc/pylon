package io.github.pylonmc.pylon.content.resources;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarInventoryTicker;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class IronBloom extends RebarItem implements RebarInventoryTicker {

    public static final long DAMAGE_INTERVAL = Settings.get(PylonKeys.IRON_BLOOM).getOrThrow("damage-interval", ConfigAdapter.LONG);
    public static final int UNPROTECTED_DAMAGE = Settings.get(PylonKeys.IRON_BLOOM).getOrThrow("unprotected-damage", ConfigAdapter.INTEGER);

    private static final NamespacedKey TEMPERATURE_KEY = pylonKey("temperature");
    public static final int MAX_TEMPERATURE = 12;

    private static final NamespacedKey WORKING_KEY = pylonKey("working");
    public static final int MIN_WORKING = -15;
    public static final int MAX_WORKING = 15;

    public IronBloom(@NotNull ItemStack stack) {
        super(stack);
    }

    /**
     * Returns a temperature between 0 and 12 inclusive.
     */
    public int getTemperature() {
        return getStack().getPersistentDataContainer().getOrDefault(TEMPERATURE_KEY, RebarSerializers.INTEGER, 0);
    }

    /**
     * @param temperature the temperature to set, must be between 0 and 12 inclusive.
     */
    @SuppressWarnings("UnstableApiUsage")
    public void setTemperature(int temperature) {
        Preconditions.checkArgument(temperature >= 0 && temperature <= MAX_TEMPERATURE, "Temperature must be between 0 and 12 inclusive.");
        ItemStack stack = getStack();
        stack.editPersistentDataContainer((pdc) -> pdc.set(TEMPERATURE_KEY, RebarSerializers.INTEGER, temperature));
        CustomModelData data = stack.getDataOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().build());
        CustomModelData newData = CustomModelData.customModelData()
                .addStrings(data.strings())
                .addFlags(data.flags())
                .addColors(data.colors())
                .addFloat(temperature)
                .build();
        stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, newData);
    }

    /**
     * Returns a working between -15 and 15 inclusive.
     */
    public int getWorking() {
        return getStack().getPersistentDataContainer().getOrDefault(WORKING_KEY, RebarSerializers.INTEGER, 0);
    }

    /**
     * @param working the working to set, must be between -15 and 15 inclusive.
     */
    public void setWorking(int working) {
        Preconditions.checkArgument(working >= MIN_WORKING && working <= MAX_WORKING, "Working must be between 0 and 15 inclusive.");
        getStack().editPersistentDataContainer((pdc) -> pdc.set(WORKING_KEY, RebarSerializers.INTEGER, working));
    }

    public void setDisplayGlowOn(@NotNull ItemDisplay display) {
        int temperature = getTemperature();
        if (temperature == 0) {
            display.setGlowing(false);
        } else {
            display.setGlowColorOverride(PylonUtils.colorFromTemperature(600 + (temperature - 1) * 300));
            display.setGlowing(true);
        }
    }

    @Override
    public long getTickInterval() {
        return DAMAGE_INTERVAL;
    }

    @Override
    public void onTick(@NotNull Player player) {
        if (player.getInventory().getItemInMainHand().isSimilar(PylonItems.TONGS) ||
                player.getInventory().getItemInOffHand().isSimilar(PylonItems.TONGS)) {
            return;
        }
        player.damage(UNPROTECTED_DAMAGE, DamageSource.builder(DamageType.HOT_FLOOR).build());
    }
}
