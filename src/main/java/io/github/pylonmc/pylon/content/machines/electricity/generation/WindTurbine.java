package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

public class WindTurbine extends RebarBlock implements
        SimpleRebarMultiblock,
        SimpleElectricRebarBlock,
        BlockBreakRebarBlockHandler,
        TickingRebarBlock,
        UnloadRebarBlockHandler {

    @Getter
    private final double basePower = getSettingOrThrow("base-power", ConfigAdapter.DOUBLE);

    @Getter
    private final double extraPowerPerBlock = getSettingOrThrow("extra-power-per-block", ConfigAdapter.DOUBLE);

    public static class Item extends RebarItem {

        @Getter
        private final double basePower = getSettingOrThrow("base-power", ConfigAdapter.DOUBLE);

        @Getter
        private final double extraPowerPerBlock = getSettingOrThrow("extra-power-per-block", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("base", UnitFormat.WATTS.format(basePower).selectPrefixAndRescale().decimalPlaces(2)),
                    RebarArgument.of("extra", UnitFormat.WATTS.format(extraPowerPerBlock).selectPrefixAndRescale().decimalPlaces(2))
            );
        }
    }

    @SuppressWarnings("unused")
    public WindTurbine(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
        setFacing(context.getFacing());
        createSimpleElectricPort(ElectricNodeType.PRODUCER, getFacing());
    }

    @SuppressWarnings("unused")
    public WindTurbine(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) return;

        for (String name : getHeldEntities().keySet()) {
            if (!name.startsWith("turbine_display:")) continue;
            ItemDisplay display = getHeldEntityOrThrow(ItemDisplay.class, name);
            display.setInterpolationDuration(getTickInterval());
            display.setTeleportDuration(getTickInterval());
            Location loc = display.getLocation();
            loc.setYaw(loc.getYaw() + 90);
            display.teleportAsync(loc);
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
        Vector3f xVector = new Vector3f(1, 0, 0);
        for (Vector2i vane : VANES) {
            Quaternionf rotation = new Quaternionf().rotationTo(xVector, new Vector3f(vane.x(), 0, vane.y()));
            addEntity("turbine_display:bar:" + vane, new ItemDisplayBuilder()
                    .itemStack(barItem)
                    .transformation(new Matrix4f()
                            .scaleLocal(0.49f, 0.1f, 0.1f)
                            .translateLocal(xVector.mul(0.5f, new Vector3f()))
                            .rotateLocal(rotation))
                    .build(center)
            );
            addEntity("turbine_display:vane:" + vane, new ItemDisplayBuilder()
                    .itemStack(vaneItem)
                    .transformation(new Matrix4f()
                            .scaleLocal(1, 4, 1)
                            .translateLocal(xVector)
                            .rotateLocal(rotation))
                    .build(center)
            );
        }

        // runs 1 tick later because the latest fence doesn't update for some reason if I don't do this
        Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
            for (Vector2i vane : VANES) {
                setVane(vane, Material.AIR);
            }
        });

        setPowerProduced(extraPowerPerBlock * (getBlock().getY() - getBlock().getWorld().getMinHeight()) + basePower);
    }

    @Override
    public void onMultiblockUnformed(boolean partUnloaded) {
        SimpleRebarMultiblock.super.onMultiblockUnformed(partUnloaded);
        setPowerProduced(0);
        reset();
    }

    @Override
    public boolean onPreBlockBreak(@NotNull BlockBreakContext context) {
        if (isFormedAndFullyLoaded()) {
            reset();
        }
        return true;
    }

    @Override
    public void onUnload(@NotNull RebarBlockUnloadEvent event, @NotNull EventPriority priority) {
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

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(Component.translatable("pylon.message.producing-power", RebarArgument.of("power", UnitFormat.WATTS.format(getPowerProduced()).decimalPlaces(1))));
    }
}