package io.github.pylonmc.pylon.guide;

import io.github.pylonmc.pylon.content.machines.smelting.SmelteryBurner;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class SmelteryBurnerFuelsPage extends SimpleDynamicGuidePage {

    private static final SmelteryBurnerFuelsPage INSTANCE = new SmelteryBurnerFuelsPage();
    @Getter private static final Item button = new PageButton(
            ItemStackBuilder.of(Material.COAL)
                    .name(Component.translatable("pylon.guide.page.smeltery_burner_fuels"))
                    .build(),
            INSTANCE
    );

    public SmelteryBurnerFuelsPage() {
        super(pylonKey("smeltery_burner_fuels"), SmelteryBurnerFuelsPage::getButtons);
    }

    private static @NotNull List<Item> getButtons() {
        List<SmelteryBurner.Fuel> sortedFuels = SmelteryBurner.FUELS.getValues().stream()
                .sorted(SmelteryBurnerFuelsPage::compareFuels)
                .toList();
        List<Item> buttons = new ArrayList<>();
        for (SmelteryBurner.Fuel fuel : sortedFuels) {
            ItemStack stack = ItemStackBuilder.of(fuel.material().clone())
                    .lore(Component.translatable("pylon.guide.smeltery_burner_fuels").arguments(
                            RebarArgument.of("duration", UnitFormat.SECONDS.format(fuel.burnTimeSeconds())),
                            RebarArgument.of("temperature", UnitFormat.CELSIUS.format(fuel.temperature()))
                    ))
                    .build();
            buttons.add(new ItemButton(stack));
        }
        return buttons;
    }

    private static int compareFuels(SmelteryBurner.@NonNull Fuel l, SmelteryBurner.@NonNull Fuel r) {
        return Double.compare(r.temperature(), l.temperature());
    }
}
