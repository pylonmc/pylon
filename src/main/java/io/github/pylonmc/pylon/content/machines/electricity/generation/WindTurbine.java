package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.SimpleElectricRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

public class WindTurbine extends RebarBlock implements
        SimpleRebarMultiblock,
        SimpleElectricRebarBlock,
        BlockBreakRebarBlockHandler,
        TickingRebarBlock {

    public static class Item extends RebarItem {

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return super.getPlaceholders();
        }
    }

    @SuppressWarnings("unused")
    public WindTurbine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());
    }

    @SuppressWarnings("unused")
    public WindTurbine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        for (Vector2i vane : VANES) {
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 0, 0), new Vector3f(vane.x(), 0, vane.y()));
            ItemDisplay barDisplay = getHeldEntityOrThrow(ItemDisplay.class, barName(vane));
            PylonUtils.animate(barDisplay, getTickInterval(), barTransform(barDisplay.getTicksLived() / (float) getTickInterval(), rotation));
            ItemDisplay vaneDisplay = getHeldEntityOrThrow(ItemDisplay.class, vaneName(vane));
            PylonUtils.animate(vaneDisplay, getTickInterval(), vaneTransform(vaneDisplay.getTicksLived() / (float) getTickInterval(), rotation));
        }
    }

    private final ItemStackBuilder barItem = ItemStackBuilder.of(Material.STONE)
            .addCustomModelDataString(getKey() + ":bar");

    private final ItemStackBuilder vaneItem = ItemStackBuilder.of(Material.IRON_BARS)
            .addCustomModelDataString(getKey() + ":vane");

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();

        Location center = getBlock().getLocation().add(0.5, 3, 0.5);
        for (Vector2i vane : VANES) {
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 0, 0), new Vector3f(vane.x(), 0, vane.y()));
            addEntity(barName(vane), new ItemDisplayBuilder()
                    .itemStack(barItem)
                    .transformation(barTransform(0, rotation))
                    .build(center)
            );
            addEntity(vaneName(vane), new ItemDisplayBuilder()
                    .itemStack(vaneItem)
                    .transformation(vaneTransform(0, rotation))
                    .build(center)
            );
        }

        // runs 1 tick later because the latest fence doesnt update for some reason if I don't do this
        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            for (Vector2i vane : VANES) {
                setVane(vane, Material.AIR);
            }
        });
    }

    private static final float ROTATION_PER_TICK = (float) (Math.PI / 2);

    private static Matrix4f barTransform(float time, Quaternionf initialRotation) {
        return new Matrix4f()
                .scaleLocal(0.5f, 0.1f, 0.1f)
                .translateLocal(new Vector3f(0.5f, 0, 0))
                .rotateLocal(initialRotation)
                .rotateLocal(new Quaternionf().rotateAxis(ROTATION_PER_TICK * time, new Vector3f(0, 1, 0)));
    }

    private static String barName(Vector2i vane) {
        return "turbine_display:bar:" + vane.x() + " " + vane.y();
    }

    private static Matrix4f vaneTransform(float time, Quaternionf initialRotation) {
        return new Matrix4f()
                .scaleLocal(1, 4, 1)
                .translateLocal(new Vector3f(1, 0, 0))
                .rotateLocal(initialRotation)
                .rotateLocal(new Quaternionf().rotateAxis(ROTATION_PER_TICK * time, new Vector3f(0, 1, 0)));
    }

    private static String vaneName(Vector2i vane) {
        return "turbine_display:vane:" + vane.x() + " " + vane.y();
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        SimpleRebarMultiblock.super.onMultiblockUnformed(partUnloaded);
        if (!partUnloaded) {
            reset();
        }
    }

    @Override
    public void onPostBlockBreak(@NotNull BlockBreakContext context) {
        reset();
    }

    private void reset() {
        for (String name : new ArrayList<>(getHeldEntities().keySet())) {
            if (name.startsWith("turbine_display:")) {
                tryRemoveEntity(name);
            }
        }
        for (Vector2i vane : VANES) {
            setVane(vane, Material.IRON_BARS);
        }
    }

    private void setVane(Vector2i offset, Material material) {
        for (int y = 1; y <= 4; y++) {
            Block block = getMultiblockBlock(new Vector3i(offset.x(), y, offset.y()));
            if (material != Material.AIR && !block.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), ItemStack.of(material));
            } else {
                block.setType(material);
            }
        }
    }

    private static final Vector2i[] VANES = {
            new Vector2i(0, 1),
            new Vector2i(0, -1),
            new Vector2i(1, 0),
            new Vector2i(-1, 0)
    };

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        addStack(components, MultiblockComponent.of(PylonKeys.STEEL_SUPPORT_BEAM), new Vector2i(0, 0));
        for (Vector2i vane : VANES) {
            addStack(components, MultiblockComponent.of(Material.IRON_BARS), vane);
        }
        addStack(components, MultiblockComponent.AIR, new Vector2i(-1, -1));
        addStack(components, MultiblockComponent.AIR, new Vector2i(1, -1));
        addStack(components, MultiblockComponent.AIR, new Vector2i(1, 1));
        addStack(components, MultiblockComponent.AIR, new Vector2i(-1, 1));
        components.put(new Vector3i(0, -1, 0), MultiblockComponent.of(PylonKeys.STEEL_FOUNDATION));
        return components;
    }

    private static void addStack(Map<Vector3i, MultiblockComponent> components, MultiblockComponent component, Vector2i offset) {
        for (int y = 1; y <= 4; y++) {
            components.put(new Vector3i(offset.x(), y, offset.y()), component);
        }
    }
}