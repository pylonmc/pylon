package io.github.pylonmc.pylon.content.machines.electricity.generation;

import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleElectricBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.PlayerInput;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public final class CreativePowerSource extends RebarBlock implements
        RebarSimpleElectricBlock,
        RebarInteractBlock {

    @SuppressWarnings("unused")
    public CreativePowerSource(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        for (BlockFace face : RebarUtils.IMMEDIATE_FACES) {
            createSimpleElectricPort(NodeType.PRODUCER, face);
        }
    }

    @SuppressWarnings({"unused"})
    public CreativePowerSource(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (player.isSneaking() || !event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND) return;
        player.sendMessage(Component.translatable("pylon.message.creative-power-source.set-power"));
        PlayerInput.requestInput(player).thenAccept(input -> {
            if (input == null) return;
            try {
                double newValue = Double.parseDouble(input);
                setPowerProduced(Math.max(0, newValue));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.translatable("pylon.message.invalid-input.double"));
            }
        });
    }

    @Override
    public @NonNull WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("power", UnitFormat.WATTS.format(getPowerProduced())
                        .ignoreCommonlyUnusedPrefixes()
                        .abbreviate(true)
                        .autoSelectPrefix()
                        .decimalPlaces(2)
                )
        ));
    }
}
