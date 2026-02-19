package io.github.pylonmc.pylon.content.machines.fluid;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.tools.WateringCan;
import io.github.pylonmc.pylon.content.tools.WateringSettings;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFlowerPot;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.RebarConfig;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.PreRebarBlockPlaceEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class Sprinkler extends RebarBlock
        implements RebarFluidBufferBlock, RebarTickingBlock, RebarFlowerPot {

    private static final Config settings = Settings.get(PylonKeys.SPRINKLER);
    public static final WateringSettings SETTINGS = WateringSettings.fromConfig(settings);
    public static final int TICK_INTERVAL = settings.getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public static final double WATER_PER_SECOND = settings.getOrThrow("water-per-second", ConfigAdapter.INTEGER);
    public static final double BUFFER = settings.getOrThrow("buffer", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("range", UnitFormat.BLOCKS.format(SETTINGS.horizontalRange())),
                    RebarArgument.of("buffer", UnitFormat.MILLIBUCKETS.format(BUFFER)),
                    RebarArgument.of("water_consumption", UnitFormat.MILLIBUCKETS_PER_SECOND.format(WATER_PER_SECOND))
            );
        }
    }

    @SuppressWarnings("unused")
    public Sprinkler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        setTickInterval(TICK_INTERVAL);
        createFluidPoint(FluidPointType.INPUT, BlockFace.UP, -0.15F);
        createFluidBuffer(PylonFluids.WATER, BUFFER, true, false);
    }

    @SuppressWarnings("unused")
    public Sprinkler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onFlowerPotManipulated(@NotNull PlayerFlowerPotManipulateEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override
    public void tick() {
        if (fluidAmount(PylonFluids.WATER) > WATER_PER_SECOND * RebarConfig.FLUID_TICK_INTERVAL / 20.0) {
            WateringCan.water(getBlock(), SETTINGS);
            removeFluid(PylonFluids.WATER, WATER_PER_SECOND * RebarConfig.FLUID_TICK_INTERVAL / 20.0);
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bars", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.WATER),
                        fluidCapacity(PylonFluids.WATER),
                        20,
                        NamedTextColor.BLUE
                ))
        ));
    }

    public static class SprinklerPlaceListener implements Listener {
        @EventHandler
        private static void handle(@NotNull PreRebarBlockPlaceEvent event) {
            if (event.getBlockSchema().getKey() != PylonKeys.SPRINKLER) {
                return;
            }

            int horizontalRadiusToCheck = 2 * SETTINGS.horizontalRange();
            int verticalRadiusToCheck = 2 * SETTINGS.verticalRange();
            for (int x = -horizontalRadiusToCheck; x <= horizontalRadiusToCheck; x++) {
                for (int z = -horizontalRadiusToCheck; z <= horizontalRadiusToCheck; z++) {
                    for (int y = -verticalRadiusToCheck; y <= verticalRadiusToCheck; y++) {
                        if (!(BlockStorage.get(event.getBlock().getRelative(x, y, z)) instanceof Sprinkler)) {
                            continue;
                        }

                        event.setCancelled(true);
                        if (event.getContext() instanceof BlockCreateContext.PlayerPlace context) {
                            context.getPlayer().sendMessage(Component.translatable(
                                    "pylon.message.sprinkler_too_close",
                                    RebarArgument.of("radius", horizontalRadiusToCheck)
                            ));
                        }
                        return;
                    }
                }
            }
        }
    }
}
