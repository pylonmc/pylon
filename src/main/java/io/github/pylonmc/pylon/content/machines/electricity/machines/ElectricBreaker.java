package io.github.pylonmc.pylon.content.machines.electricity.machines;

import io.github.pylonmc.pylon.content.machines.generic.GenericBreaker;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DispenserRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.ElectricRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class ElectricBreaker extends GenericBreaker implements ElectricRebarBlock, DispenserRebarBlockHandler {

    private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);
        private final double speed = getSettingOrThrow("speed", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100.0)),
                    RebarArgument.of("power-usage", UnitFormat.WATTS.format(powerUsage))
            );
        }
    }

    @SuppressWarnings("unused")
    public ElectricBreaker(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.BRICKS)
                        .addCustomModelDataString(getKey() + ":side1"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, -0.1)
                        .scale(0.8, 0.8, 0.9))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.BRICKS)
                        .addCustomModelDataString(getKey() + ":side2"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("top", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.BLUE_GLAZED_TERRACOTTA)
                        .addCustomModelDataString(getKey() + ":top"))
                .transformation(new TransformBuilder()
                        .scale(0.55, 0.2, 0.55))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Material.YELLOW_CONCRETE)
                        .addCustomModelDataString(getKey() + ":drill"))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0.5)
                        .scale(0.6, 0.6, 0.2)
                        .rotate(0, 0, Math.PI / 4))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addElectricPort(
                new ElectricPort(new ElectricNode.Consumer("consumer", new BlockPosition(block), powerUsage), getFacing().getOppositeFace())
                        .radius(0.55)
        );
    }

    @SuppressWarnings("unused")
    public ElectricBreaker(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isProcessing() || !((ElectricNode.Consumer) getElectricNodeOrThrow("consumer")).isPowered()) return;
        progressProcess(tickInterval);
    }

    @Override
    public void onPreDispense(@NotNull BlockPreDispenseEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }
}