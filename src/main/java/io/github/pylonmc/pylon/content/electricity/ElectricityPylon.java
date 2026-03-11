package io.github.pylonmc.pylon.content.electricity;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarBreakHandler;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.*;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class ElectricityPylon extends RebarBlock implements
        RebarInteractBlock,
        RebarBreakHandler,
        Listener,
        RebarElectricBlock {

    private static final NamespacedKey CONNECTING_KEY = pylonKey("connecting");
    private static final NamespacedKey CONNECTING_ID_KEY = pylonKey("connecting_id");

    private static final NamespacedKey CONNECTED_KEY = pylonKey("connected");
    private static final PersistentDataType<?, Map<BlockPosition, UUID>> CONNECTED_TYPE = RebarSerializers.MAP.mapTypeFrom(
            RebarSerializers.BLOCK_POSITION,
            RebarSerializers.UUID
    );

    private final Map<BlockPosition, UUID> connectedPylons;

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        connectedPylons = new HashMap<>();

        createElectricNode(getBlock().getLocation().toCenterLocation(), ElectricNode.Type.CONNECTOR);
    }

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        connectedPylons = pdc.get(CONNECTED_KEY, CONNECTED_TYPE);
    }

    {
        Bukkit.getPluginManager().registerEvents(this, Pylon.getInstance());
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        super.write(pdc);
        pdc.set(CONNECTED_KEY, CONNECTED_TYPE, connectedPylons);
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        for (var entry : connectedPylons.entrySet()) {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(entry.getValue());
            assert display != null;
            display.remove();

            BlockPosition otherPos = entry.getKey();
            ElectricityPylon otherPylon = BlockStorage.getAs(ElectricityPylon.class, otherPos);
            assert otherPylon != null;
            otherPylon.connectedPylons.remove(new BlockPosition(getBlock()));
        }
        PlayerMoveEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @Override
    @MultiHandler(priorities = EventPriority.MONITOR)
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (player.isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == Event.Result.DENY
        ) return;

        BlockPosition thisPos = new BlockPosition(getBlock());
        PersistentDataContainer playerPdc = player.getPersistentDataContainer();
        BlockPosition connecting = playerPdc.get(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION);
        if (connecting == null) {
            Location blockLocation = getBlock().getLocation().toCenterLocation();
            Location playerLocation = player.getEyeLocation().subtract(0, 0.5, 0);
            ItemDisplay display = new ItemDisplayBuilder()
                    .transformation(getDisplayTransform(blockLocation, playerLocation))
                    .material(Material.COPPER_BLOCK)
                    .build(blockLocation.add(playerLocation.subtract(blockLocation).multiply(0.5)));
            playerPdc.set(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION, thisPos);
            playerPdc.set(CONNECTING_ID_KEY, RebarSerializers.UUID, display.getUniqueId());
        } else if (connecting.equals(thisPos)) {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(Objects.requireNonNull(playerPdc.get(CONNECTING_ID_KEY, RebarSerializers.UUID)));
            assert display != null;
            display.remove();
            playerPdc.remove(CONNECTING_KEY);
            playerPdc.remove(CONNECTING_ID_KEY);
        } else {
            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(Objects.requireNonNull(playerPdc.get(CONNECTING_ID_KEY, RebarSerializers.UUID)));
            assert display != null;
            Location thisCenter = getBlock().getLocation().toCenterLocation();
            Location connectingLocation = connecting.getLocation().toCenterLocation();
            display.setTransformationMatrix(getDisplayTransform(thisCenter, connectingLocation));
            display.teleportAsync(thisCenter.add(connectingLocation.clone().subtract(thisCenter).multiply(0.5)));

            ElectricityPylon otherPylon = BlockStorage.getAs(ElectricityPylon.class, connecting);
            assert otherPylon != null;
            if (otherPylon.connectedPylons.containsKey(thisPos)) {
                // disconnect if already connected
                display.remove();
                otherPylon.connectedPylons.remove(thisPos);
                Objects.requireNonNull(Bukkit.getEntity(connectedPylons.remove(connecting))).remove();
            } else {
                otherPylon.connectedPylons.put(thisPos, display.getUniqueId());
                connectedPylons.put(connecting, display.getUniqueId());
            }
            playerPdc.remove(CONNECTING_KEY);
            playerPdc.remove(CONNECTING_ID_KEY);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;
        Player player = event.getPlayer();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(CONNECTING_KEY)) return;
        BlockPosition connecting = pdc.get(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION);
        assert connecting != null;
        Location connectingLocation = connecting.getLocation().toCenterLocation();
        Location playerLocation = player.getEyeLocation().subtract(0, 0.5, 0);
        ItemDisplay display = (ItemDisplay) Bukkit.getEntity(Objects.requireNonNull(pdc.get(CONNECTING_ID_KEY, RebarSerializers.UUID)));
        assert display != null;
        display.setTeleportDuration(1);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(1);
        display.setTransformationMatrix(getDisplayTransform(connectingLocation, playerLocation));
        display.teleportAsync(connectingLocation.add(playerLocation.clone().subtract(connectingLocation).multiply(0.5)));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(CONNECTING_KEY)) return;
        BlockPosition connecting = pdc.get(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION);
        assert connecting != null;
        ItemDisplay display = (ItemDisplay) Bukkit.getEntity(Objects.requireNonNull(pdc.get(CONNECTING_ID_KEY, RebarSerializers.UUID)));
        assert display != null;
        display.remove();
        pdc.remove(CONNECTING_KEY);
        pdc.remove(CONNECTING_ID_KEY);
    }

    private static Matrix4f getDisplayTransform(Location from, Location to) {
        return new TransformBuilder()
                .lookAlong(from, to)
                .scale(0.05, 0.05, from.distance(to))
                .buildForItemDisplay();
    }
}
