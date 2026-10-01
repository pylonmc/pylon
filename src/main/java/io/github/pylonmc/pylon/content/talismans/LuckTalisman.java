package io.github.pylonmc.pylon.content.talismans;

import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LuckTalisman extends AttributeTalisman {
    public static final NamespacedKey LUCK_TALISMAN_KEY = PylonUtils.pylonKey("luck_talisman");

    public LuckTalisman(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    protected Attribute getAttribute() {
        return Attribute.LUCK;
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("bonus_luck", Component.text(attrBonus))
        );
    }

    @Override
    public NamespacedKey getTalismanKey() {
        return LUCK_TALISMAN_KEY;
    }
}
