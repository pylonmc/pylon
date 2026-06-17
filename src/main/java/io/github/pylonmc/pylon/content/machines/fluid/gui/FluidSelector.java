package io.github.pylonmc.pylon.content.machines.fluid.gui;


import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public final class FluidSelector {

    private FluidSelector() {}

    public static Gui make(@NotNull Supplier<RebarFluid> getFluid, @NotNull Consumer<RebarFluid> setFluid) {
        AbstractItem currentItem = new CurrentItem(getFluid);
        Function<RebarFluid, AbstractItem> fluidItem = itemFluid -> new FluidItem(itemFluid, setFluid, currentItem);

        Gui gui = Gui.builder()
                .setStructure(
                        "x x b x x x c x x",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . ."
                )
                .addIngredient('x', GuiItems.background())
                .addIngredient('b', fluidItem.apply(null))
                .addIngredient('c', currentItem)
                .build();

        int i = 9;
        for (RebarFluid itemFluid : RebarRegistry.FLUIDS.getValues()) {
            gui.setItem(i, fluidItem.apply(itemFluid));
            i++;
        }

        return gui;
    }

    private static class CurrentItem extends AbstractItem {
        private final @NotNull Supplier<RebarFluid> getFluid;

        public CurrentItem(@NotNull Supplier<RebarFluid> getFluid) {
            super();
            this.getFluid = getFluid;
        }

        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
            RebarFluid fluid = getFluid.get();
            return ItemStackBuilder.of(fluid == null ? ItemStack.of(Material.BARRIER) : fluid.getItem())
                    .name(Component.translatable(
                            "pylon.message.fluid_selector.current_fluid",
                            RebarArgument.of(
                                    "fluid",
                                    Component.translatable((fluid == null
                                            ? "pylon.fluid.none"
                                            : "pylon.fluid." + fluid.getKey().getKey()
                                    ))
                            ))
                    );
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {}
    }

    private static class FluidItem extends AbstractItem {
        private final RebarFluid itemFluid;
        private final @NotNull Consumer<RebarFluid> setFluid;
        private final AbstractItem currentItem;

        public FluidItem(RebarFluid itemFluid, @NotNull Consumer<RebarFluid> setFluid, AbstractItem currentItem) {
            super();
            this.itemFluid = itemFluid;
            this.setFluid = setFluid;
            this.currentItem = currentItem;
        }

        @Override
        public @NonNull ItemProvider getItemProvider(@NotNull Player viewer) {
            if (itemFluid == null) {
                return ItemStackBuilder.of(Material.BARRIER)
                        .name(Component.translatable("pylon.message.fluid_selector.clear"));
            }
            return ItemStackBuilder.of(itemFluid.getItem())
                    .name(Component.translatable("pylon.fluid." + itemFluid.getKey().getKey()));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            setFluid.accept(itemFluid);
            currentItem.notifyWindows();
        }
    }
}
