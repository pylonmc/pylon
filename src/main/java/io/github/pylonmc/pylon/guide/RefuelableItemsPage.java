package io.github.pylonmc.pylon.guide;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.hydraulics.HydraulicRefuelable;
import io.github.pylonmc.rebar.guide.button.ItemButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleDynamicGuidePage;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.item.Item;

import java.util.List;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class RefuelableItemsPage extends SimpleDynamicGuidePage {

    private static final RefuelableItemsPage INSTANCE = new RefuelableItemsPage();
    @Getter private static final Item button = new PageButton(
            ItemStackBuilder.of(PylonItems.HYDRAULIC_CANNON.clone())
                    .name(Component.translatable("pylon.guide.page.hydraulic_refuelable_items"))
                    .build(),
            INSTANCE
    );

    public RefuelableItemsPage() {
        super(pylonKey("hydraulic_refuelable_items"), RefuelableItemsPage::getButtons);
    }

    private static @NonNull List<Item> getButtons() {
        return RebarRegistry.ITEMS.stream()
                .filter(item -> RebarItem.fromStack(item.getItemStack()) instanceof HydraulicRefuelable)
                .map(item -> (Item) new ItemButton(item.getItemStack()))
                .toList();
    }
}
