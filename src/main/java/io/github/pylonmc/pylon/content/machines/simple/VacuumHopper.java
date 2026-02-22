package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.Inventory;
import xyz.xenondevs.invui.inventory.ReferencingInventory;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class VacuumHopper extends RebarBlock implements
        RebarTickingBlock,
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarLogisticBlock {

    public static class Item extends RebarItem {
        public final int radius = getSettings().getOrThrow("radius-blocks", ConfigAdapter.INTEGER);
        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("radius", UnitFormat.BLOCKS.format(radius)),
                    RebarArgument.of("tick_interval", UnitFormat.SECONDS.format(tickInterval / 20.0))
            );
        }
    }

    public static final NamespacedKey WHITELIST_KEY = pylonKey("whitelist");
    // if whitelist is true behaves like a whitelist, otherwise like a blacklist
    public boolean whitelist;

    @Getter
    private final @NotNull Inventory hopperInventory;

    @Getter
    private final @NotNull VirtualInventory filterInventory = new VirtualInventory(9);

    public final int radius = getSettings().getOrThrow("radius-blocks", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    public final ItemStackBuilder filterGuiStack = ItemStackBuilder.gui(Material.PINK_STAINED_GLASS_PANE, getKey() + "filter")
            .name(Component.translatable("pylon.gui.filter"));

    @SuppressWarnings("unused")
    public VacuumHopper(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        this.whitelist = false;
        setTickInterval(tickInterval);
    }

    @SuppressWarnings("unused")
    public VacuumHopper(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        this.whitelist = pdc.get(WHITELIST_KEY, RebarSerializers.BOOLEAN);
    }

    {
        var hopper = (org.bukkit.block.Hopper) getBlock().getState(false);
        this.hopperInventory = ReferencingInventory.fromStorageContents(hopper.getInventory());
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(WHITELIST_KEY, RebarSerializers.BOOLEAN, whitelist);
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("filter", filterInventory);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        for (ItemStack item : hopperInventory.getItems()) {
            if (item != null) {
                drops.add(item);
            }
        }

        for (ItemStack item : filterInventory.getItems()) {
            if (item != null) {
                drops.add(item);
            }
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # x x x x x # #",
                        "# # # # # # # # #",
                        "w # f y y y y y f",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', hopperInventory)
                .addIngredient('f', filterGuiStack)
                .addIngredient('y', filterInventory)
                .addIngredient('w', new WhitelistItem())
                .build();
    }

    @Override
    public void tick() {
        Block block = getBlock();
        Hopper hopper = (Hopper) block.getBlockData();
        if (!hopper.isEnabled()) {
            return; // don't vacuum if powered
        }

        // loops item entities to add
        for (Entity entity : block.getLocation().toCenterLocation().getNearbyEntities(radius + 0.5, radius + 0.5, radius + 0.5)) {
            if (!(entity instanceof org.bukkit.entity.Item item)) {
                continue;
            }

            ItemStack stack = item.getItemStack();
            if (!isFiltered(stack)) {
                continue;
            }

            int surplus = hopperInventory.addItem(new MachineUpdateReason(), stack);
            if (surplus == stack.getAmount()) {
                // Could not add any items in the stack
                continue;
            }

            if (surplus == 0) {
                // Stack fully added
                new ParticleBuilder(Particle.WITCH)
                        .location(item.getLocation())
                        .spawn();
                item.remove();
                break;
            }

            // Stack partially added
            stack.setAmount(surplus);
            new ParticleBuilder(Particle.WITCH)
                    .location(item.getLocation())
                    .spawn();
        }
    }

    public boolean isFiltered(@NotNull ItemStack item) {
        for (ItemStack filterStack : filterInventory.getItems()) {
            if (item.isSimilar(filterStack)) {
                return whitelist;
            }
        }
        return !whitelist;
    }

    public final class WhitelistItem extends AbstractBoundItem {

        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return ItemStackBuilder.of(whitelist ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE)
                    .name(Component.translatable("pylon.gui.vacuum_hopper." + (whitelist ? "whitelist" : "blacklist")));
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            whitelist = !whitelist;
            notifyWindows();
        }
    }
}
