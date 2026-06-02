package io.github.pylonmc.pylon.util;

import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

@Builder
public class NumberInputButton extends AbstractBoundItem {

    private final Material material;
    private final Component name;

    private final int increment;
    private final int shiftIncrement;

    private final @Nullable Integer min;
    private final @Nullable Integer max;

    private final Supplier<Integer> valueGetter;
    private final Consumer<Integer> valueSetter;

    @lombok.Builder.Default
    private final Function<Integer, ComponentLike> valueFormatter = Component::text;

    @lombok.Builder.Default
    private final Consumer<Player> reopenWindow = _unused -> {};

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        int value = valueGetter.get();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable(
                "pylon.gui.number-button.lore",
                RebarArgument.of("increment", valueFormatter.apply(increment)),
                RebarArgument.of("increment-shift", valueFormatter.apply(shiftIncrement))
        ));
        if (min != null) {
            lore.add(Component.translatable("pylon.gui.number-button.min", RebarArgument.of("min", valueFormatter.apply(min))));
        }
        if (max != null) {
            lore.add(Component.translatable("pylon.gui.number-button.max", RebarArgument.of("max", valueFormatter.apply(max))));
        }
        return ItemStackBuilder.gui(material, pylonKey("number_input_button"))
                .name(Component.translatable(
                        "pylon.gui.number-button.name",
                        RebarArgument.of("name", name),
                        RebarArgument.of("value", valueFormatter.apply(value))
                ))
                .lore(lore);
    }

    private void setValue(int value) {
        if (min != null) {
            value = Math.max(value, min);
        }
        if (max != null) {
            value = Math.min(value, max);
        }
        valueSetter.accept(value);
        notifyWindows();
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
        int value = valueGetter.get();
        int increment = clickType.isShiftClick() ? shiftIncrement : this.increment;
        if (clickType.isLeftClick()) {
            value += increment;
        } else if (clickType.isRightClick()) {
            value -= increment;
        }
        setValue(value);
    }
}
