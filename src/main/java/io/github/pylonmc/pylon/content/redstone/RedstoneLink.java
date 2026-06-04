package io.github.pylonmc.pylon.content.redstone;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.BlockBreakRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.GhostBlockHolderRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.NoVanillaInventoryRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.RedstoneRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.UnloadRebarBlockHandler;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.block.TileStateInventoryHolder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.Shelf;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;
import static io.github.pylonmc.rebar.util.RebarUtils.rotateVectorToFace;

/**
 * TODO:
 * - organize this class some more currently pretty messy
 * - proper lang entries for redstone link
 * - 1 redstone link for every wood type?
 * - maaaaaybe abstract this concept of transmitters & receivers
 * - more code polish & content polish
 */
public class RedstoneLink extends RebarBlock implements RedstoneRebarBlockHandler, InteractRebarBlockHandler, UnloadRebarBlockHandler, BlockBreakRebarBlockHandler, DirectionalRebarBlock, GhostBlockHolderRebarBlock, NoVanillaInventoryRebarBlock {

    private static final Channel NO_CHANNEL = new Channel(ItemTypeWrapper.of(Material.AIR), ItemTypeWrapper.of(Material.AIR), ItemTypeWrapper.of(Material.AIR));

    private static final NamespacedKey receiverKey = pylonKey("receiver");
    private static final Vector3i OUTPUT_POSITION = new Vector3i(0, 0, -1);

    private static final ItemStack ANTENNA = ItemStackBuilder.of(Material.EXPOSED_LIGHTNING_ROD)
            .addCustomModelDataString(PylonKeys.REDSTONE_LINK + ":antenna_broadcast")
            .addCustomModelDataString("powered=false")
            .build();
    private static final ItemStack ANTENNA_POWERED = ItemStackBuilder.of(Material.LIGHTNING_ROD)
            .addCustomModelDataString(PylonKeys.REDSTONE_LINK + ":antenna_broadcast")
            .addCustomModelDataString("powered=true")
            .build();
    private static final ItemStack RECEIVER_ANTENNA = ItemStackBuilder.of(Material.WEATHERED_LIGHTNING_ROD)
            .addCustomModelDataString(PylonKeys.REDSTONE_LINK + ":antenna_receiver")
            .addCustomModelDataString("powered=false")
            .build();
    private static final ItemStack RECEIVER_ANTENNA_POWERED = ItemStackBuilder.of(Material.OXIDIZED_LIGHTNING_ROD)
            .addCustomModelDataString(PylonKeys.REDSTONE_LINK + ":antenna_receiver")
            .addCustomModelDataString("powered=true")
            .build();

    private final double signalRadius = getSetting("signal-radius", ConfigAdapter.DOUBLE);

    private static final Map<Channel, Set<RedstoneLink>> CHANNEL_CACHE = new HashMap<>();
    private static final Set<Block> LINK_OUTPUTS = new HashSet<>();

    private SimpleRebarMultiblock.MultiblockComponent outputComponent;

    private Channel channel = NO_CHANNEL;
    @Getter
    private boolean receiver = false;

    @SuppressWarnings("unused")
    public RedstoneLink(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        if (block.getBlockData() instanceof Shelf shelfData) {
            setFacing(shelfData.getFacing().getOppositeFace());
            shelfData.setPowered(false);
            block.setBlockData(shelfData);
        } else {
            setFacing(BlockFace.NORTH);
        }
        addEntity("antenna", new ItemDisplayBuilder()
                .itemStack(ANTENNA)
                .transformation(builder -> builder
                        .rotate(0, RebarUtils.faceToYaw(getFacing()), 0)
                        .translate(0.5615, 0.25, -0.4))
                .build(block.getLocation().toCenterLocation()));
    }

    @SuppressWarnings("unused")
    public RedstoneLink(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        this.receiver = pdc.getOrDefault(receiverKey, RebarSerializers.BOOLEAN, false);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(receiverKey, RebarSerializers.BOOLEAN, this.receiver);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("direction", Component.text(this.receiver ? "Receiver" : "Transmitter").color(this.receiver ? NamedTextColor.GREEN : NamedTextColor.RED))));
    }

    public Vector3i getOutputPosition() {
        return rotateVectorToFace(OUTPUT_POSITION, getFacing());
    }

    @Override
    public void postInitialise() {
        RedstoneWallTorch outputData = (RedstoneWallTorch) Material.REDSTONE_WALL_TORCH.createBlockData();
        outputData.setFacing(getFacing());
        this.outputComponent = SimpleRebarMultiblock.MultiblockComponent.of(outputData);

        if (receiver) {
            LINK_OUTPUTS.add(getRawOutput());
        }
        // talk to idra about funny edge case where I can't do BlockStorage.getByKey because it doesn't add the blocks to the set until all of
        // those blocks have loaded so because this is at load time they dont exist and it fails to update
        Bukkit.getScheduler().runTask(Pylon.getInstance(), this::updateChannel);
    }

    private void updateReceivers(Channel channel) {
        Set<RedstoneLink> otherLinks = CHANNEL_CACHE.get(channel);
        if (otherLinks != null) {
            for (RedstoneLink otherLink : otherLinks) {
                if (otherLink != this && otherLink.receivesSignal(getBlock(), channel)) {
                    otherLink.updatePowered();
                }
            }
        }
    }

    public void updatePowered() {
        Set<RedstoneLink> otherLinks = CHANNEL_CACHE.get(channel);
        for (RedstoneLink otherLink : otherLinks) {
            if (otherLink != this && otherLink.broadcastsSignal(getBlock(), channel) && otherLink.isInputPowered()) {
                setOutputPowered(true, false);
                return;
            }
        }
        setOutputPowered(false, false);
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onRedstoneCurrentChange(@NotNull BlockRedstoneEvent event, @NotNull EventPriority priority) {
        if (receiver) {
            event.setNewCurrent(isOutputPowered() ? 15 : 0);
        } else {
            Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
                updateItemStack();
                updateReceivers(channel);
            });
        }
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.useInteractedBlock() == Event.Result.DENY || !event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            if (event.getHand() == EquipmentSlot.HAND) {
                event.setUseInteractedBlock(Event.Result.DENY);
                if (this.receiver) {
                    this.receiver = false;
                    LINK_OUTPUTS.remove(getRawOutput());
                    removeGhostBlock(getOutputPosition());
                    setOutputPowered(true, true);
                    updateReceivers(channel);
                } else {
                    this.receiver = true;
                    LINK_OUTPUTS.add(getRawOutput());
                    updateReceivers(channel);
                    updatePowered();
                }
                player.swingMainHand();
            }
            return;
        }

        Bukkit.getScheduler().runTask(Pylon.getInstance(), this::updateChannel);
    }

    @Override
    public void onPostBlockBreak(@NotNull BlockBreakContext context) {
        Channel oldChannel = channel;
        channel = NO_CHANNEL;
        updateCache(oldChannel);
        if (!receiver) updateReceivers(oldChannel);
    }

    @Override
    public void onUnload(@NotNull RebarBlockUnloadEvent event, @NotNull EventPriority priority) {
        Channel oldChannel = channel;
        channel = NO_CHANNEL;
        updateCache(oldChannel);
        if (!receiver) updateReceivers(oldChannel);
    }

    private void updateItemStack() {
        ItemDisplay antenna = getHeldEntity(ItemDisplay.class, "antenna");
        if (antenna != null) {
            boolean powered = receiver ? isOutputPowered() : isInputPowered();
            antenna.setItemStack(receiver
                    ? (powered ? RECEIVER_ANTENNA_POWERED : RECEIVER_ANTENNA)
                    : (powered ? ANTENNA_POWERED : ANTENNA)
            );
        }
    }

    private void updateCache(Channel oldChannel) {
        if (oldChannel != null) {
            Set<RedstoneLink> links = CHANNEL_CACHE.get(oldChannel);
            if (links != null) {
                links.remove(this);
                if (links.isEmpty()) {
                    CHANNEL_CACHE.remove(oldChannel);
                }
            }
        }
        if (channel != NO_CHANNEL) {
            Set<RedstoneLink> links = CHANNEL_CACHE.get(channel);
            if (links == null) {
                links = new HashSet<>();
            }
            links.add(this);
            CHANNEL_CACHE.put(channel, links);
        }
    }

    private void updateChannel() {
        Channel oldChannel = channel;
        if (!(getBlock().getState(false) instanceof TileStateInventoryHolder shelf) || !Tag.WOODEN_SHELVES.isTagged(getBlock().getType())) {
            channel = NO_CHANNEL;
            updateCache(oldChannel);
            updateItemStack();
            updateReceivers(oldChannel);
            return;
        }

        ItemTypeWrapper[] frequencies = new ItemTypeWrapper[3];
        for (int i = 0; i < frequencies.length; i++) {
            ItemStack itemStack = shelf.getInventory().getItem(i);
            frequencies[i] = itemStack == null || itemStack.isEmpty() ? ItemTypeWrapper.of(Material.AIR) : ItemTypeWrapper.of(itemStack);
        }
        this.channel = new Channel(frequencies[0], frequencies[1], frequencies[2]);
        updateCache(oldChannel);

        if (!receiver) {
            updateReceivers(oldChannel);
            updateReceivers(channel);
        } else {
            updatePowered();
        }
        updateItemStack();
    }

    public boolean broadcastsSignal(Block otherLink, Channel otherSignal) {
        return !receiver && onChannel(otherLink, otherSignal);
    }
    
    public boolean receivesSignal(Block otherLink, Channel otherSignal) {
        return receiver && onChannel(otherLink, otherSignal);
    }

    public boolean onChannel(Block otherLink, Channel otherChannel) {
        if (!this.channel.equals(otherChannel)) return false;
        double distSqr = distanceSquared(getBlock(), otherLink);
        return distSqr != -1 && distSqr < signalRadius * signalRadius;
    }
    
    private boolean isInputPowered() {
        return getBlock().getBlockData() instanceof Shelf shelfData && shelfData.isPowered();
    }

    private boolean isOutputPowered() {
        Block output = getOutput(false);
        return output != null && output.getBlockData() instanceof RedstoneWallTorch outputData && outputData.isLit();
    }

    private void setOutputPowered(boolean powered, boolean force) {
        Block output = getOutput(force);
        if (output != null && output.getBlockData() instanceof RedstoneWallTorch outputData) {
            outputData.setLit(powered);
            output.setBlockData(outputData);
        } else if (receiver && output == null && getVanillaGhostBlockDisplay(getOutputPosition()) == null) {
            this.outputComponent.spawnGhostBlock(this, getOutputPosition());
        }
        if (getBlock().getBlockData() instanceof Shelf shelfData) {
            shelfData.setPowered(powered);
            getBlock().setBlockData(shelfData);
        }
        updateItemStack();
    }

    private Block getRawOutput() {
        Vector3i outputPosition = getOutputPosition();
        return getBlock().getRelative(outputPosition.x, outputPosition.y, outputPosition.z);
    }

    private Block getOutput(boolean force) {
        if (!receiver && !force) return null;
        Block block = getRawOutput();
        if (block.getBlockData() instanceof RedstoneWallTorch outputData && outputData.getFacing() == getFacing() && !BlockStorage.isRebarBlock(block)) {
            return block;
        }
        return null;
    }

    private double distanceSquared(Block block, Block otherBlock) {
        if (block.getWorld() != otherBlock.getWorld()) {
            return -1;
        }

        int dx = block.getX() - otherBlock.getX();
        int dy = block.getY() - otherBlock.getY();
        int dz = block.getZ() - otherBlock.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public record Channel(ItemTypeWrapper frequency1, ItemTypeWrapper frequency2, ItemTypeWrapper frequency3) {}

    public static final class OutputListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPlaceOutput(BlockPlaceEvent event) {
            Block block = event.getBlock();
            if (!LINK_OUTPUTS.contains(block) || !(block.getBlockData() instanceof RedstoneWallTorch outputData)) {
                return;
            }

            RedstoneLink link = BlockStorage.getAs(RedstoneLink.class, block.getRelative(outputData.getFacing().getOppositeFace()));
            if (link != null && link.receiver) {
                Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
                    link.removeGhostBlock(link.getOutputPosition());
                    link.updatePowered();
                });
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onOutputRedstone(BlockRedstoneEvent event) {
            Block block = event.getBlock();
            if (!LINK_OUTPUTS.contains(block) || !(block.getBlockData() instanceof RedstoneWallTorch outputData)) {
                return;
            }

            RedstoneLink link = BlockStorage.getAs(RedstoneLink.class, block.getRelative(outputData.getFacing().getOppositeFace()));
            if (link != null && link.receiver) {
                event.setNewCurrent(event.getOldCurrent());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBreakOutput(BlockBreakEvent event) {
            Block block = event.getBlock();
            if (!LINK_OUTPUTS.contains(block) || !(block.getBlockData() instanceof RedstoneWallTorch outputData)) {
                return;
            }

            RedstoneLink link = BlockStorage.getAs(RedstoneLink.class, block.getRelative(outputData.getFacing().getOppositeFace()));
            if (link != null && link.receiver) {
                Bukkit.getScheduler().runTask(Pylon.getInstance(), link::updatePowered);
            }
        }

    }
}
