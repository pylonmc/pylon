package io.github.pylonmc.pylon.content.machines.smelting;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.util.BurnerProgressItem;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import java.util.Map;
import kotlin.Pair;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class SmelteryBurner extends SmelteryComponent implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarProcessor {

    public static final NamespacedKey FUELS_KEY = pylonKey("smeltery_burner_fuels");
    public static final RebarRegistry<Fuel> FUELS = new RebarRegistry<>(FUELS_KEY);

    static {
        RebarRegistry.addRegistry(FUELS);
    }

    private static final NamespacedKey FUEL_KEY = pylonKey("fuel");
    private static final PersistentDataType<?, Fuel> FUEL_TYPE = RebarSerializers.KEYED.keyedTypeFrom(Fuel.class, FUELS::getOrThrow);

    private @Nullable Fuel fuel;

    private final VirtualInventory fuelInventory = new VirtualInventory(3);
    private final BurnerProgressItem progressItem = new BurnerProgressItem();

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);

        setTickInterval(SmelteryController.TICK_INTERVAL);

        fuel = null;
    }

    @SuppressWarnings("unused")
    public SmelteryBurner(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);

        fuel = pdc.get(FUEL_KEY, FUEL_TYPE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, FUEL_KEY, FUEL_TYPE, fuel);
    }

    @Override
    public void postInitialise() {
        setProcessProgressItem(progressItem);
        createLogisticGroup("fuel", LogisticGroupType.INPUT, fuelInventory);
    }

    @Override
    public @NotNull Map<String, Pair<String, Integer>> getBlockTextureProperties() {
        var properties = super.getBlockTextureProperties();
        properties.put("lit", new Pair<>(String.valueOf(fuel != null), 2));
        return properties;
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # i p i # # #",
                        "# # # i x i # # #",
                        "# # # i i i # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('i', GuiItems.input())
                .addIngredient('p', progressItem)
                .addIngredient('x', fuelInventory)
                .build();
    }

    private void tryStartProcessing() {
        for (Fuel fuel : FUELS) {
            if (fuelInventory.removeFirstSimilar(new MachineUpdateReason(), 1, fuel.material()) > 0) {
                this.fuel = fuel;
                startProcess(fuel.burnTimeSeconds() * 20);
                refreshBlockTextureItem();
                return;
            }
        }
    }

    @Override
    public void tick() {
        SmelteryController controller = getController();
        if (controller == null || !controller.isRunning()) {
            return;
        }

        if (!isProcessing()) {
            tryStartProcessing();
        }

        if (!isProcessing()) return;

        progressProcess(getTickInterval());

        if (fuel != null) {
            controller.heatAsymptotically(fuel.temperature);
            return;
        }
    }

    @Override
    public void onProcessFinished() {
        refreshBlockTextureItem();
        fuel = null;
        tryStartProcessing();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("fuels", fuelInventory);
    }

    // TODO display fuels
    public record Fuel(
            @NotNull NamespacedKey key,
            @NotNull ItemStack material,
            double temperature,
            int burnTimeSeconds
    ) implements Keyed {
        @Override
        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }

    static {
        FUELS.register(new Fuel(
                pylonKey("coal"),
                new ItemStack(Material.COAL),
                1100,
                30
        ));
        FUELS.register(new Fuel(
                pylonKey("coal_dust"),
                PylonItems.COAL_DUST,
                1100,
                30
        ));
        FUELS.register(new Fuel(
                pylonKey("charcoal"),
                new ItemStack(Material.CHARCOAL),
                1100,
                30
        ));
    }
}
