package io.github.pylonmc.pylon.content.machines.petrochemicals;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.EntityHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.VirtualInventoryRebarBlock;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.processor.RebarProcessor;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;


public class OilSampleDrill extends RebarBlock implements
        EntityHolderRebarBlock,
        TickingRebarBlock,
        GuiRebarBlock,
        DirectionalRebarBlock,
        VirtualInventoryRebarBlock {

    public static class Item extends RebarItem {

        public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
        public final int sampleTicks = getSettingOrThrow("sample-ticks", ConfigAdapter.INTEGER);
        public final double fuelBurnRate = getSettingOrThrow("fuel-burn-rate", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("sample-time", UnitFormat.SECONDS.format(tickInterval * sampleTicks / 20)),
                    RebarArgument.of("fuel-burn-rate", UnitFormat.PERCENT.format(100 * fuelBurnRate))
            );
        }
    }

    public static final NamespacedKey SAMPLE_PROCESSOR = PylonUtils.pylonKey("sample_processor");
    public static final NamespacedKey FUEL_PROCESSOR = PylonUtils.pylonKey("fuel_processor");

    public final int tickInterval = getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double radiansPerSecond = getSettingOrThrow("radians-per-second", ConfigAdapter.DOUBLE);
    public final int sampleTicks = getSettingOrThrow("sample-ticks", ConfigAdapter.INTEGER);
    public final double fuelBurnRate = getSettingOrThrow("fuel-burn-rate", ConfigAdapter.DOUBLE);

    public ItemStackBuilder drillStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":drill");
    public ItemStackBuilder screenStack = ItemStackBuilder.of(Material.BLACK_CONCRETE)
            .addCustomModelDataString(getKey() + ":screen");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");
    public ItemStackBuilder sideStack = ItemStackBuilder.of(Material.COPPER_BULB)
            .addCustomModelDataString(getKey() + ":side");

    private final VirtualInventory fuelInventory = new VirtualInventory(1);
    private final RebarProcessor sampleProcessor;
    private final RebarProcessor fuelProcessor;

    public OilSampleDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());
        setTickInterval(tickInterval);

        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(drillStack)
                .transformation(new TransformBuilder()
                        .rotate(0, 0, 0)
                        .scale(0.4, 2, 0.4)
                )
                .build(block.getLocation().toCenterLocation().add(0, 1, 0))
        );

        addEntity("screen", new ItemDisplayBuilder()
                .itemStack(screenStack)
                .transformation(new TransformBuilder()
                        .lookAlong(context.getFacing())
                        .translate(0, -0.5, 0.5)
                        .scale(0.6, 0.4, 0.1)
                )
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(chimneyStack)
                .transformation(new TransformBuilder()
                        .lookAlong(context.getFacing())
                        .translate(0, -0.1, -0.5)
                        .scale(0.2, 1.8, 0.2)
                )
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        addEntity("side", new ItemDisplayBuilder()
                .itemStack(sideStack)
                .transformation(new TransformBuilder()
                        .lookAlong(context.getFacing())
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8)
                )
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );

        sampleProcessor = new RebarProcessor();
        fuelProcessor = new RebarProcessor();

        sampleProcessor.start(sampleTicks);
    }

    public OilSampleDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        sampleProcessor = pdc.get(SAMPLE_PROCESSOR, RebarSerializers.PROCESSOR);
        fuelProcessor = pdc.get(FUEL_PROCESSOR, RebarSerializers.PROCESSOR);
    }

    @Override
    public void postInitialise() {
        sampleProcessor.onFinish(() -> {
            Material material;
            Double oil = OilService.getOilYield(getBlock());
            if (oil == null) {
                material = Material.RED_CONCRETE;
            } else if (oil < 1 / 3.0) {
                material = Material.ORANGE_CONCRETE;
            } else if (oil < 2 / 3.0) {
                material = Material.YELLOW_CONCRETE;
            } else {
                material = Material.LIME_CONCRETE;
            }

            getHeldEntityOrThrow(ItemDisplay.class, "screen").setItemStack(new ItemStack(material));
        });
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(SAMPLE_PROCESSOR, RebarSerializers.PROCESSOR, sampleProcessor);
        pdc.set(FUEL_PROCESSOR, RebarSerializers.PROCESSOR, fuelProcessor);
    }

    @Override
    public void tick() {
        if (fuelProcessor.isRunning()) {
            // Consume fuel until all fuel is burnt even if not sampling
            fuelProcessor.tick(getTickInterval());
        }

        if (!sampleProcessor.isRunning()) {
            return;
        }

        if (!fuelProcessor.isRunning()) {
            ItemStack stack = fuelInventory.getItem(0);
            if (stack == null || stack.isEmpty()) {
                return;
            }

            ItemType itemType = stack.getType().asItemType();
            if (itemType == null) {
                return;
            }

            fuelInventory.setItem(new MachineUpdateReason(), 0, stack.subtract());
            fuelProcessor.start((int) (itemType.getBurnDuration() / fuelBurnRate));
        }

        ItemDisplay drill = getHeldEntityOrThrow(ItemDisplay.class, "drill");
        drill.setInterpolationDelay(0);
        drill.setInterpolationDuration(getTickInterval());
        drill.setTransformationMatrix(new TransformBuilder()
                .rotate(0, sampleProcessor.getElapsedSeconds() * radiansPerSecond, 0)
                .scale(0.4, 2, 0.4)
                .build()
        );

        Vector smokePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0, 1.45, -0.5),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .count(20)
                .extra(0)
                .spawn();

        sampleProcessor.tick(getTickInterval());
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure("# # # # x # # # #")
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', fuelInventory)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of("fuel", fuelInventory);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display =  WailaDisplay.of(this, player);
        if (sampleProcessor.isRunning()) {
            display.add(ProgressBar.recipeProgress(sampleProcessor.getElapsedProportion()));
        }
        if (fuelProcessor.isRunning()) {
            display.add(ProgressBar.fuelRemaining(fuelProcessor.getDurationSeconds(), fuelProcessor.getRemainingSeconds()));
        }
        return display;
    }
}
