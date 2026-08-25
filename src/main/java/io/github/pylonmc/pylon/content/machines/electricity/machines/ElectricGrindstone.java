package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericGrindstone;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNode;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricGrindstone extends GenericGrindstone implements SimpleElectricRebarBlock {

    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        public final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage)));
        }
    }

    @SuppressWarnings("unused")
    public ElectricGrindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createSimpleElectricPort(ElectricNode.Type.CONSUMER, getFacing());
        setRequiredPower(powerUsage);
    }

    @SuppressWarnings("unused")
    public ElectricGrindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || !isPowered()) return;
        progressRecipe(tickInterval);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (!isPowered()) {
            display.add(Component.translatable("pylon.message.no_power"));
        }
        return display;
    }
}
