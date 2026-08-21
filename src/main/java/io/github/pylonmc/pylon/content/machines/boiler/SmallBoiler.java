package io.github.pylonmc.pylon.content.machines.boiler;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;


public class SmallBoiler extends AbstractSolidFuelBoiler {

    public ItemStackBuilder panelStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":panel");
    public ItemStackBuilder gratingStack = ItemStackBuilder.of(Material.COPPER_BARS)
            .addCustomModelDataString(getKey() + ":grating");

    public final double minFuelConsumption = getSettingOrThrow("min-fuel-consumption", ConfigAdapter.DOUBLE);
    public final double maxFuelConsumption = getSettingOrThrow("max-fuel-consumption", ConfigAdapter.DOUBLE);

    public static final Vector3i WATER_INPUT = new Vector3i(-1, 0, 0);
    public static final Vector3i STEAM_OUTPUT = new Vector3i(-1, 0, 1);
    public static final Vector3i FUEL_INPUT = new Vector3i(1, 0, 1);
    public static final Vector3i SMOKESTACK_CAP = new Vector3i(1, 4, 1);

    @SuppressWarnings("unused")
    public SmallBoiler(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        addEntity("vertical-panel", new ItemDisplayBuilder()
                .itemStack(panelStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing().getOppositeFace())
                        .translate(0, 0, 3)
                        .scale(0.8, 1.1, 4.9)
                )
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection()))
        );

        addEntity("horizontal-panel", new ItemDisplayBuilder()
                .itemStack(panelStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing().getOppositeFace())
                        .translate(0, 0, 3)
                        .rotate(0, 0, Math.PI / 2)
                        .scale(0.8, 1.1, 4.9)
                )
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection()))
        );

        addEntity("grating-1", new ItemDisplayBuilder()
                .itemStack(gratingStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0, -0.5)
                        .scale(0.8, 0.8, 0.199)
                )
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection()))
        );

        // im a genius omg
        addEntity("grating-2", new ItemDisplayBuilder()
                .itemStack(gratingStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing().getOppositeFace())
                        .translate(0, 0, 0.5)
                        .scale(0.8, 0.8, 0.2)
                )
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection()))
        );

        addEntity("fire", new TextDisplayBuilder()
                .text(Component.text(" "))
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.08, -0.25, -0.499)
                        .scale(4.0, 2.0, 0.001)
                )
                .backgroundColor(Color.BLACK)
                .brightness(15)
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection()))
        );
    }

    @SuppressWarnings("unused")
    public SmallBoiler(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public Vector3i waterInputPosition() {
        return WATER_INPUT;
    }

    @Override
    public Vector3i steamOutputPosition() {
        return STEAM_OUTPUT;
    }

    @Override
    public Vector3i fuelInputPosition() {
        return FUEL_INPUT;
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();

        components.put(WATER_INPUT, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        components.put(STEAM_OUTPUT, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));
        components.put(FUEL_INPUT, MultiblockComponent.of(PylonKeys.ITEM_INPUT_HATCH));
        components.put(new Vector3i(1, 1, 1), MultiblockComponent.of(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 2, 1), MultiblockComponent.of(PylonKeys.SMOKESTACK_RING));
        components.put(new Vector3i(1, 3, 1), MultiblockComponent.of(PylonKeys.SMOKESTACK_RING));
        components.put(SMOKESTACK_CAP, MultiblockComponent.of(PylonKeys.SMOKESTACK_CAP));
        components.put(new Vector3i(0, 0, 1), MultiblockComponent.of(Material.BRICKS));
        components.put(new Vector3i(0, 0, 2), MultiblockComponent.of(Material.BRICKS));
        components.put(new Vector3i(0, 0, 3), MultiblockComponent.of(Material.BRICKS));
        components.put(new Vector3i(0, 0, 4), MultiblockComponent.of(Material.BRICKS));

        return components;
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        super.update(minFuelConsumption, maxFuelConsumption, WATER_INPUT, STEAM_OUTPUT);

        if (fuelBurntLastUpdate > 0.01) {
            int count = random.nextInt(2 + (int) (10 * fuelBurntLastUpdate / maxFuelConsumption));
            for (int i = 0; i < count; i++) {
                double offset = 0.3 * random.nextDouble();
                new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                        .offset(0, 1, 0)
                        .count(0)
                        .extra(0.04)
                        .location(getMultiblockBlock(SMOKESTACK_CAP).getLocation().toCenterLocation().add(0, offset, 0))
                        .spawn();
            }
        }

        TextDisplay fire = getHeldEntityOrThrow(TextDisplay.class, "fire");
        fire.setInterpolationDelay(0);
        fire.setInterpolationDuration(getTickInterval());
        fire.setBackgroundColor(fireColor());
    }
}
