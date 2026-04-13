package io.github.pylonmc.pylon.content.machines.electric;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import net.kyori.adventure.text.Component;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Objects;

import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.ElectricNetwork;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.util.position.BlockPosition;

public final class ElectricityPylon extends RebarBlock implements
        RebarElectricBlock.Connector,
        RebarTickingBlock,
        RebarEntityHolderBlock {

    private static final NamespacedKey CONNECTING_KEY = pylonKey("connecting");
    private static final NamespacedKey CONNECTING_ID_KEY = pylonKey("connecting_id");

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(10);
        createElectricNode(getBlock().getLocation().toCenterLocation(), ElectricNode.Type.CONNECTOR);
    }

    @SuppressWarnings("unused")
    public ElectricityPylon(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        getElectricNode().onDisconnect((thisNode, otherNode) -> tryRemoveEntity(otherNode.getId().toString()));
    }

    @Override
    public double getCurrentLimit(@NotNull ElectricNode otherNode) {
        return 100;
    }

    @Override
    public void tick() {
        ElectricNetwork network = getElectricNode().getNetwork();
        for (ElectricNode node : network.getNodes()) {
            Particle.DUST.builder()
                    .color(Color.fromARGB(network.hashCode()))
                    .location(node.getBlock().getLocation().toCenterLocation().add(0, 0.6, 0))
                    .receivers(32, true)
                    .spawn();
        }
    }

    private ElectricNode getElectricNode() {
        return getElectricNodes().getFirst();
    }

    private static Matrix4f getDisplayTransform(Location from, Location to) {
        return new TransformBuilder()
                .lookAlong(from, to)
                .scale(0.05, 0.05, from.distance(to))
                .buildForItemDisplay();
    }

    private static Location getMidpoint(Location a, Location b) {
        return a.clone().add(b).multiply(0.5);
    }

    public static class InteractionListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        private void onRightClickElectricBlock(PlayerInteractEvent event) {
            if (event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Event.Result.DENY) return;

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null) return;
            RebarElectricBlock electricBlock = BlockStorage.getAs(RebarElectricBlock.class, clickedBlock);
            if (electricBlock == null) return;

            BlockPosition clickedPos = new BlockPosition(clickedBlock);
            Player player = event.getPlayer();
            PersistentDataContainer playerPdc = player.getPersistentDataContainer();
            BlockPosition connecting = playerPdc.get(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION);
            if (connecting == null) {
                if (electricBlock instanceof ElectricityPylon && !player.isSneaking() && event.getAction().isRightClick()) {
                    Location connectionLocation = electricBlock.getElectricNodes().getFirst().getConnectionPoint();
                    Location playerLocation = player.getEyeLocation().subtract(0, 0.5, 0);
                    ItemDisplay display = new ItemDisplayBuilder()
                            .transformation(getDisplayTransform(connectionLocation, playerLocation))
                            .material(Material.COPPER_BLOCK)
                            .build(getMidpoint(connectionLocation, playerLocation));
                    playerPdc.set(CONNECTING_KEY, RebarSerializers.BLOCK_POSITION, clickedPos);
                    playerPdc.set(CONNECTING_ID_KEY, RebarSerializers.UUID, display.getUniqueId());
                }
                return;
            }

            event.setUseInteractedBlock(Event.Result.DENY);

            ItemDisplay display = (ItemDisplay) Bukkit.getEntity(Objects.requireNonNull(playerPdc.get(CONNECTING_ID_KEY, RebarSerializers.UUID)));
            assert display != null;
            if (connecting.equals(clickedPos)) {
                display.remove();
                playerPdc.remove(CONNECTING_KEY);
                playerPdc.remove(CONNECTING_ID_KEY);
                return;
            }

            ElectricityPylon connectingBlock = BlockStorage.getAsOrThrow(ElectricityPylon.class, connecting);
            ElectricNode connectingNode = connectingBlock.getElectricNodes().getFirst();
            ElectricNode clickedNode = electricBlock.getElectricNodes().getFirst();
            if (!clickedNode.getConnections().isEmpty() && !clickedNode.isConnectedTo(connectingNode) && clickedNode.getType() != ElectricNode.Type.CONNECTOR) {
                player.sendActionBar(Component.translatable("pylon.message.electricity_pylon.already_connected"));
                return;
            }

            if (connectingNode.isConnectedTo(clickedNode)) {
                display.remove();
                connectingNode.disconnect(clickedNode);
            } else {
                Location connectingLocation = connectingNode.getConnectionPoint();
                Location clickedLocation = clickedNode.getConnectionPoint();
                display.setTransformationMatrix(getDisplayTransform(connectingLocation, clickedLocation));
                display.teleportAsync(getMidpoint(connectingLocation, clickedLocation));
                connectingNode.connect(clickedNode);
                connectingBlock.addEntity(clickedNode.getId().toString(), display);
            }
            playerPdc.remove(CONNECTING_KEY);
            playerPdc.remove(CONNECTING_ID_KEY);
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
    }
}
