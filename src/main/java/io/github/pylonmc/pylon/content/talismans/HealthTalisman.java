package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HealthTalisman extends AttributeTalisman {

    private static final NamespacedKey HEALTH_TALISMAN_KEY = PylonUtils.pylonKey("health_talisman");

    public HealthTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return HEALTH_TALISMAN_KEY;
    }

    @Override
    protected Attribute getAttribute() {
        return Attribute.MAX_HEALTH;
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        return List.of(RebarArgument.of("health-boost", UnitFormat.HEARTS.format(attrBonus/2).decimalPlaces(1)));
    }
}
