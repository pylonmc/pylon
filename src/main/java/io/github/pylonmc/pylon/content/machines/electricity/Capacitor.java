package io.github.pylonmc.pylon.content.machines.electricity;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.ElectricRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.nodes.ElectricAcceptorNode;
import io.github.pylonmc.rebar.electricity.nodes.ElectricPortSpec;
import io.github.pylonmc.rebar.electricity.nodes.ElectricProducerNode;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Capacitor extends RebarBlock implements
        ElectricRebarBlock,
        DirectionalRebarBlock,
        InteractRebarBlockHandler {

    public static final class Item extends RebarItem {

        private final double capacity = getSettingOrThrow("capacity", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("capacity", UnitFormat.JOULES.format(capacity)));
        }
    }

    private final double capacity = getSettingOrThrow("capacity", ConfigAdapter.DOUBLE);

    private static final NamespacedKey STORED_ENERGY_KEY = pylonKey("stored_energy");
    private double storedEnergy;

    private ElectricProducerNode output;

    @SuppressWarnings("unused")
    public Capacitor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addElectricPort(new ElectricPortSpec(new ElectricAcceptorNode("input", new BlockPosition(block)), getFacing()));
        addElectricPort(new ElectricPortSpec(new ElectricProducerNode("output", new BlockPosition(block), 0), getFacing().getOppositeFace()));

        storedEnergy = 0;
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public Capacitor(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        storedEnergy = pdc.get(STORED_ENERGY_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(STORED_ENERGY_KEY, RebarSerializers.DOUBLE, storedEnergy);
    }

    @Override
    public void postInitialise() {
        ElectricAcceptorNode input = (ElectricAcceptorNode) getElectricNodeOrThrow("input");
        input.onAccept(energy -> {
            double accepted = Math.min(energy, capacity - storedEnergy);
            setStoredEnergy(storedEnergy + accepted);
            return accepted;
        });

        output = (ElectricProducerNode) getElectricNodeOrThrow("output");
        output.onPowerTake(energy -> {
            double taken = Math.min(energy, storedEnergy);
            setStoredEnergy(storedEnergy - taken);
        });
        output.setPriority(Integer.MAX_VALUE);

        setStoredEnergy(storedEnergy);
    }

    @Override
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND) return;

        BlockFace face = event.getBlockFace();
        Vector direction = face.getDirection();
        String name = displayName(face);
        TextDisplay display = getHeldEntity(TextDisplay.class, name);
        if (display == null) {
            addEntity(name, new TextDisplayBuilder()
                    .transformation(new TransformBuilder()
                            .lookAlong(direction.toVector3f()))
                    .build(getBlock().getLocation().toCenterLocation().add(direction.clone().multiply(0.5001)))
            );
            setStoredEnergy(storedEnergy);
        } else {
            display.remove();
        }
    }

    private static String displayName(BlockFace face) {
        return "text_" + face.name().toLowerCase(Locale.ROOT);
    }

    public void setStoredEnergy(double energy) {
        storedEnergy = energy;
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            TextDisplay display = getHeldEntity(TextDisplay.class, displayName(face));
            if (display == null) continue;
            display.text(formatEnergy(storedEnergy));
        }
        output.setPower(storedEnergy);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player).add(formatEnergy(storedEnergy));
    }

    private static Component formatEnergy(double energy) {
        return UnitFormat.JOULES.format(energy)
                .selectPrefixAndRescale()
                .decimalPlaces(1)
                .asComponent();
    }
}
