package io.github.pylonmc.pylon.content.tools.electric;

import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.electricity.BatteryBank;
import io.github.pylonmc.pylon.content.tools.EntitySensor;
import io.github.pylonmc.pylon.util.ElectricBolt;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.ConfirmationDialogGuiRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.SimpleRebarMultiblock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class TezlaTower extends RebarBlock implements
        SimpleRebarMultiblock,
        TickingRebarBlock,
        ConfirmationDialogGuiRebarBlock {

    private static final NamespacedKey POWER_KEY = pylonKey("power");
    @Getter
    @Setter
    private float power = 0;

    private static final NamespacedKey ON_KEY = pylonKey("on");
    @Getter
    @Setter
    private boolean on = false;

    @SuppressWarnings("unused")
    public TezlaTower(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public TezlaTower(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        power = pdc.get(POWER_KEY, RebarSerializers.FLOAT);
        on = pdc.get(ON_KEY, RebarSerializers.BOOLEAN);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(POWER_KEY, RebarSerializers.FLOAT, power);
        pdc.set(ON_KEY, RebarSerializers.BOOLEAN, on);
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded() || power == 0 || !on) return;
        BatteryBank bank = getMultiblockComponentOrThrow(BatteryBank.class, CAPACITOR_POSITION);
        if (bank.getStoredEnergy() < power) return;
        EntitySensor sensor = getMultiblockComponentOrThrow(EntitySensor.class, SENSOR_POSITION);

        Set<Entity> sensed = sensor.getSensedEntities();
        Set<LivingEntity> sensedLiving = new HashSet<>();
        for (Entity entity : sensed) {
            if (entity instanceof LivingEntity livingEntity) {
                sensedLiving.add(livingEntity);
            }
        }
        if (sensedLiving.isEmpty()) return;

        bank.discharge(power);
        ElectricBolt.spawn(
                getBlock().getLocation().toCenterLocation().add(0, 1, 0),
                1,
                Math.cbrt(power),
                null,
                sensedLiving::contains
        );
    }

    private static final Vector3i CAPACITOR_POSITION = new Vector3i(0, -2, 0);
    private static final Vector3i SENSOR_POSITION = new Vector3i(0, -1, 0);

    @Override
    public boolean overrideComponentWailas() {
        return false;
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(
                CAPACITOR_POSITION, MultiblockComponent.of(PylonKeys.BATTERY_BANK),
                SENSOR_POSITION, MultiblockComponent.of(PylonKeys.ENTITY_SENSOR)
        );
    }

    @Override
    public void open(@NotNull Player player) {
        if (isFormedAndFullyLoaded()) {
            ConfirmationDialogGuiRebarBlock.super.open(player);
        } else {
            player.sendMessage(Component.translatable("pylon.gui.status.incomplete"));
        }
    }

    @Override
    public @NotNull DialogBase createDialogBase() {
        return DialogBase.builder(getNameTranslationKey())
                .inputs(List.of(
                        DialogInput.singleOption(
                                "on",
                                Component.empty(),
                                List.of(
                                        SingleOptionDialogInput.OptionEntry.create("true", Component.translatable("pylon.gui.status.on"), on),
                                        SingleOptionDialogInput.OptionEntry.create("false", Component.translatable("pylon.gui.status.off"), !on)
                                )
                        ).labelVisible(false).build(),
                        DialogInput.numberRange(
                                        "power",
                                        Component.translatable("pylon.gui.tezla-tower.power-per-discharge"),
                                        0,
                                        (float) getMultiblockComponentOrThrow(BatteryBank.class, CAPACITOR_POSITION).getCapacity()
                                )
                                .initial(power)
                                .step(1f)
                                .build()
                ))
                .build();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onYesAction(@NotNull DialogResponseView response, @NotNull Audience audience) {
        power = response.getFloat("power");
        on = Boolean.parseBoolean(response.getText("on"));
    }
}