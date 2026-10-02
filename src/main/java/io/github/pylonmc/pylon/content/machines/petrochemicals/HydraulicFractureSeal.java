package io.github.pylonmc.pylon.content.machines.petrochemicals;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TNTRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;


public class HydraulicFractureSeal extends RebarBlock implements VirtualInventoryRebarBlock, GuiRebarBlock, TNTRebarBlockHandler {

    public static class Item extends RebarItem {

        private final int gravelAmount = getSettingOrThrow("gravel-amount", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("gravel", gravelAmount)
            );
        }
    }

    private final int gravelAmount = getSettingOrThrow("gravel-amount", ConfigAdapter.INTEGER);
    private final int explosionPower = getSettingOrThrow("explosion-power", ConfigAdapter.INTEGER);

    private final VirtualInventory inventory = new VirtualInventory(1);

    @SuppressWarnings("unused")
    public HydraulicFractureSeal(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings("unused")
    public HydraulicFractureSeal(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        inventory.addPostUpdateHandler(event -> {
            HydraulicFracture fracture = BlockStorage.getAs(HydraulicFracture.class, getBlock().getRelative(BlockFace.DOWN));
            if (ItemTypeWrapper.of(Material.GRAVEL).matches(event.getNewItem())
                    && event.getNewItem().getAmount() >= gravelAmount
                    && fracture != null
                    && BlockStorage.breakBlock(fracture) != null
            ) {
                BlockStorage.breakBlock(this);

                getBlock().getWorld().createExplosion(getBlock().getLocation(), explosionPower);

                new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .count(200)
                        .extra(0.1)
                        .location(getBlock().getLocation().toCenterLocation().add(0, -1, 0))
                        .spawn();
            }
        });
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # x # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', inventory)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("inventory", inventory);
    }

    @Override
    public void onBlockBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        if (context instanceof BlockBreakContext.PluginBreak) {
            drops.clear();
            return;
        }
        VirtualInventoryRebarBlock.super.onBlockBreak(drops, context);
    }

    @Override
    public void onTNTPrime(@NotNull TNTPrimeEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }
}
