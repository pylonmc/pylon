package io.github.pylonmc.pylon.content.machines.smelting;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import net.kyori.adventure.text.Component;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import kotlin.Pair;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

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

    private final ItemStackBuilder notBurningProgressItem = ItemStackBuilder.of(Material.CHARCOAL)
            .name(Component.translatable("pylon.gui.smeltery_burner.not_burning"));
    private final ItemStackBuilder burningProgressItem = ItemStackBuilder.of(Material.BLAZE_POWDER)
            .name(Component.translatable("pylon.gui.smeltery_burner.burning"));

    private final VirtualInventory inventory = new VirtualInventory(3);
    private final ProgressItem progressItem = new ProgressItem(notBurningProgressItem);

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
        createLogisticGroup("fuel", LogisticGroupType.INPUT, inventory);
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
                        "# # # # f # # # #",
                        "# # # x x x # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('f', progressItem)
                .addIngredient('x', inventory)
                .addIngredient('#', GuiItems.background())
                .build();
    }

    @Override
    public void tick() {
        progressProcess(getTickInterval());

        SmelteryController controller = getController();
        if (controller == null || !controller.isRunning()) {
            return;
        }

        if (fuel != null) {
            controller.heatAsymptotically(fuel.temperature);
            return;
        }

        itemLoop:
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) {
                continue;
            }

            for (Fuel fuel : FUELS) {
                if (!item.isSimilar(fuel.material)) {
                    continue;
                }

                this.fuel = fuel;
                progressItem.setItem(burningProgressItem);
                inventory.setItem(null, i, item.subtract());
                startProcess(Math.round(fuel.burnTimeSeconds * 20));
                refreshBlockTextureItem();
                break itemLoop;
            }
        }
    }

    @Override
    public void onProcessFinished() {
        progressItem.setItem(notBurningProgressItem);
        fuel = null;
        refreshBlockTextureItem();
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("fuels", inventory);
    }

    // TODO display fuels
    public record Fuel(
            @NotNull NamespacedKey key,
            @NotNull ItemStack material,
            double temperature,
            long burnTimeSeconds
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
