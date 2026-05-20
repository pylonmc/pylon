package io.github.pylonmc.pylon.content.machines.fluid.multiblock;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankCasing;
import io.github.pylonmc.rebar.block.RebarBlockSchema;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.event.RebarRegisterEvent;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class FluidTankCasingComponent extends RebarSimpleMultiblock.MultiblockComponent {
    private static final List<NamespacedKey> FLUID_TANK_CASING_IDS = new ArrayList<>(RebarRegistry.BLOCKS.stream()
            .filter(schema -> schema.isType(FluidTankCasing.class))
            .map(RebarBlockSchema::getKey)
            .toList());

    public static final FluidTankCasingComponent INSTANCE = new FluidTankCasingComponent();

    private FluidTankCasingComponent() {
        super(List.of(), FLUID_TANK_CASING_IDS);
        Bukkit.getPluginManager().registerEvents(new RegistryListener(), Pylon.getInstance());
    }

    public static class RegistryListener implements Listener {
        @EventHandler
        public void onRegister(RebarRegisterEvent event) {
            if (event.getValue() instanceof RebarBlockSchema schema && schema.isType(FluidTankCasing.class) && !FLUID_TANK_CASING_IDS.contains(schema.getKey())) {
                FLUID_TANK_CASING_IDS.add(schema.getKey());
            }
        }
    }
}
