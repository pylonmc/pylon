package io.github.pylonmc.pylon.content.machines.electric;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarElectricBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.ElectricNode;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Capacitor extends RebarBlock implements
        RebarElectricBlock,
        RebarDirectionalBlock {

    public static final class Item extends RebarItem {

        private final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("capacity", UnitFormat.JOULES.format(capacity)));
        }
    }

    private final double capacity = getSettings().getOrThrow("capacity", ConfigAdapter.DOUBLE);

    private static final NamespacedKey STORED_ENERGY_KEY = pylonKey("stored_energy");
    private double storedEnergy;

    private ElectricNode.Producer output;

    @SuppressWarnings("unused")
    public Capacitor(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setFacing(context.getFacing());

        addElectricPort(getFacing(), new ElectricNode.Acceptor("input", new BlockPosition(block)));
        addElectricPort(getFacing().getOppositeFace(), new ElectricNode.Producer("output", new BlockPosition(block), 0));

        addEntity("text_0", new TextDisplayBuilder()
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(0, Math.PI / 2, 0))
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection().multiply(0.5001).rotateAroundY(Math.PI / 2)))
        );

        addEntity("text_1", new TextDisplayBuilder()
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .rotate(0, -Math.PI / 2, 0))
                .build(getBlock().getLocation().toCenterLocation().add(getFacing().getDirection().multiply(0.5001).rotateAroundY(-Math.PI / 2)))
        );

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
        ElectricNode.Acceptor input = (ElectricNode.Acceptor) getElectricNodeOrThrow("input");
        input.onAccept(energy -> {
            double accepted = Math.min(energy, capacity - storedEnergy);
            setStoredEnergy(storedEnergy + accepted);
            return accepted;
        });

        output = (ElectricNode.Producer) getElectricNodeOrThrow("output");
        output.onPowerTake(energy -> {
            double taken = Math.min(energy, storedEnergy);
            setStoredEnergy(storedEnergy - taken);
        });

        setStoredEnergy(storedEnergy);
    }

    public void setStoredEnergy(double energy) {
        storedEnergy = energy;
        getHeldEntityOrThrow(TextDisplay.class, "text_0").text(UnitFormat.JOULES.format(storedEnergy).decimalPlaces(1).asComponent());
        getHeldEntityOrThrow(TextDisplay.class, "text_1").text(UnitFormat.JOULES.format(storedEnergy).decimalPlaces(1).asComponent());
        output.setPower(storedEnergy);
    }
}
