package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.recipes.PcbConfiguration;
import io.github.pylonmc.pylon.util.RecipeSelectionGui;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.item.interfaces.InteractRebarItemHandler;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.RebarUtils;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PrintedCircuitBoard extends RebarItem implements InteractRebarItemHandler {

    @SuppressWarnings("unused")
    public PrintedCircuitBoard(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        new RecipeSelectionGui<>(PcbConfiguration.RECIPE_TYPE)
                .displayFunction(config -> ItemStackBuilder.copyOf(config.item()).name(config.name()).lore(Collections.emptyList()))
                .onSelected((config, player) -> {
                    player.closeInventory();
                    getStack().subtract();
                    ItemStack item;
                    if (config == null) {
                        item = PylonItems.PRINTED_CIRCUIT_BOARD.clone();
                    } else {
                        item = RebarRegistry.ITEMS.getOrThrow(config.getKey()).createNewItemStack();
                    }
                    RebarUtils.addToInventoryOrDrop(player, true, item);
                })
                .open(event.getPlayer());
    }

    @Override
    public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
        PcbConfiguration config = PcbConfiguration.RECIPE_TYPE.getRecipe(getKey());
        return List.of(RebarArgument.of("config", config == null ? Component.translatable("pylon.gui.none") : config.name()));
    }
}
