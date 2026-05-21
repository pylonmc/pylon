package io.github.pylonmc.pylon.util;

import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;

public class BurnerProgressItem extends ProgressItem {

    private static final ItemStackBuilder NOT_BURNING = ItemStackBuilder.of(Material.CHARCOAL)
            .name(Component.translatable("pylon.gui.burning.not_burning"))
            .addCustomModelDataString("pylon:burner_progress/not_burning");
    private static final ItemStackBuilder BURNING = ItemStackBuilder.of(Material.BLAZE_POWDER)
            .name(Component.translatable("pylon.gui.burning.burning"))
            .addCustomModelDataString("pylon:burner_progress/burning");

    public BurnerProgressItem() {
        super(NOT_BURNING);
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        if (getTotalTime() != null) {
            setItem(BURNING);
        } else {
            setItem(NOT_BURNING);
        }
        return super.getItemProvider(viewer);
    }
}
