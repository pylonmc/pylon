package io.github.pylonmc.pylon.guide;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.api.FlammableTag;
import io.github.pylonmc.pylon.recipes.PressRecipe;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.guide.button.FluidButton;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class LiquidFuelsPage extends SimpleDynamicGuidePage {

    private static final LiquidFuelsPage INSTANCE = new LiquidFuelsPage();
    @Getter private static final Item button = new PageButton(
            ItemStackBuilder.of(PylonFluids.BIODIESEL.getItem())
                    .name(Component.translatable("pylon.guide.page.liquid_fuels"))
                    .build(),
            INSTANCE
    );

    public LiquidFuelsPage() {
        super(pylonKey("liquid_fuels"), LiquidFuelsPage::getButtons);
    }

    private static @NonNull List<Item> getButtons() {
        List<Item> buttons = new ArrayList<>();
        for (RebarFluid fluid : RebarRegistry.FLUIDS) {
            if (fluid.hasTag(FlammableTag.class)) {
                buttons.add(FluidButton.of(fluid));
            }
        }
        return buttons;
    }
}
