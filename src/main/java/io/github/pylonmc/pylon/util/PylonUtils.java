package io.github.pylonmc.pylon.util;

import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.base.RebarFluidTank;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@UtilityClass
public class PylonUtils {

    public static @NotNull NamespacedKey pylonKey(@NotNull String key) {
        return new NamespacedKey(Pylon.getInstance(), key);
    }

    public final Color METAL_GRAY = Color.fromRGB(0xaaaaaa);

    public void drawParticleLine(
            Location start,
            @NotNull Location end,
            double spacing,
            Consumer<Location> spawnParticle
    ) {
        double currentPoint = 0;
        Vector startToEnd = end.clone().subtract(start).toVector();
        Vector step = startToEnd.clone().normalize().multiply(spacing);
        double length = startToEnd.length();
        Location current = start.clone();

        while (currentPoint < length) {
            spawnParticle.accept(current);
            currentPoint += spacing;
            current.add(step);
        }
    }

    public @NotNull Color colorFromTemperature(double celsius) {
        double temp = (celsius + 273.15) / 100.0;
        double red, green, blue;

        // ✨ magic ✨
        if (temp <= 66) {
            red = 255;
            green = 99.4708025861 * Math.log(temp) - 161.1195681661;
            blue = temp <= 19 ? 0 : 138.5177312231 * Math.log(temp - 10) - 305.0447927307;
        } else {
            red = 329.698727446 * Math.pow(temp - 60, -0.1332047592);
            green = 288.1221695283 * Math.pow(temp - 60, -0.0755148492);
            blue = 255;
        }

        Color thermalColor = Color.fromRGB(
                clampAndRound(red),
                clampAndRound(green),
                clampAndRound(blue)
        );

        if (celsius < 450) {
            return METAL_GRAY;
        }

        if (celsius > 650) {
            return thermalColor;
        }

        // Interpolate between gray and the thermal color
        double t = (celsius - 450) / 200.0;
        int r = (int) Math.round(Color.GRAY.getRed() + (thermalColor.getRed() - Color.GRAY.getRed()) * t);
        int g = (int) Math.round(Color.GRAY.getGreen() + (thermalColor.getGreen() - Color.GRAY.getGreen()) * t);
        int b = (int) Math.round(Color.GRAY.getBlue() + (thermalColor.getBlue() - Color.GRAY.getBlue()) * t);
        return Color.fromRGB(r, g, b);
    }

    private int clampAndRound(double value) {
        int rounded = (int) Math.round(value);
        return Math.max(0, Math.min(255, rounded));
    }

    public @NotNull TextDisplay spawnUnitSquareTextDisplay(@NotNull Location location, @NotNull Color color) {
        return spawnUnitSquareTextDisplay(location, color, display -> {});
    }

    public @NotNull TextDisplay spawnUnitSquareTextDisplay(@NotNull Location location, @NotNull Color color, Consumer<TextDisplay> initializer) {
        return location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setTransformationMatrix( // https://github.com/TheCymaera/minecraft-hologram/blob/d67eb43308df61bdfe7283c6821312cca5f9dea9/src/main/java/com/heledron/hologram/utilities/rendering/textDisplays.kt#L15
                    new Matrix4f()
                            .translate(-0.1f + .5f, -0.5f + .5f, 0f)
                            .scale(8.0f, 4.0f, 1f)
            );
            display.text(Component.text(" "));
            display.setBackgroundColor(color);
            initializer.accept(display);
        });
    }

    public @NotNull Vector3d getDisplacement(@NotNull Location source, @NotNull Location target) {
        return new Vector3d(target.toVector().toVector3f()).sub(source.toVector().toVector3f());
    }

    public @NotNull Vector3d getDirection(@NotNull Location source, @NotNull Location target) {
        return getDisplacement(source, target).normalize();
    }

    public @NotNull Component createBar(double proportion, int bars, TextColor color) {
        int filledBars = (int) Math.round(bars * proportion);
        return Component.text("|".repeat(filledBars)).color(color)
                .append(Component.text("|".repeat(bars - filledBars)).color(NamedTextColor.GRAY));
    }

    public @NotNull Component createProgressBar(double progress, int bars, TextColor color) {
        int filledBars = (int) Math.round(bars * progress);
        return Component.translatable("pylon.gui.progress_bar.text").arguments(
                RebarArgument.of("filled_bars", Component.text("|".repeat(filledBars)).color(color)),
                RebarArgument.of("empty_bars", "|".repeat(bars - filledBars)),
                RebarArgument.of("progress", UnitFormat.PERCENT.format(progress * 100))
        );
    }

    public @NotNull Component createDiscreteProgressBar(int stage, int maxStage, TextColor color) {
        return Component.translatable("pylon.gui.discrete_progress_bar.text").arguments(
                RebarArgument.of("filled_bars", Component.text("|".repeat(stage)).color(color)),
                RebarArgument.of("empty_bars", "|".repeat(maxStage - stage)),
                RebarArgument.of("stage", stage),
                RebarArgument.of("max_stage", maxStage)
        );
    }

    public @NotNull Component createProgressBar(double amount, double max, int bars, TextColor color) {
        return createProgressBar(amount / max, bars, color);
    }

    public @NotNull Component createFluidAmountBar(double amount, double capacity, int bars, TextColor fluidColor) {
        int filledBars = Math.max(0, (int) Math.round(bars * amount / capacity));
        return Component.translatable("pylon.gui.fluid_amount_bar.text").arguments(
                RebarArgument.of("filled_bars", Component.text("|".repeat(filledBars)).color(fluidColor)),
                RebarArgument.of("empty_bars", Component.text("|".repeat(bars - filledBars)).color(NamedTextColor.GRAY)),
                RebarArgument.of("amount", Math.round(amount)),
                RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(Math.round(capacity)))
        );
    }

    /**
     * @param display if null nothing gets done
     */
    public void animate(@Nullable ItemDisplay display, int delay, int duration, Matrix4f matrix) {
        if (display == null) return;

        display.setInterpolationDelay(delay);
        display.setInterpolationDuration(duration);
        display.setTransformationMatrix(matrix);
    }

    /**
     * @param display if null nothing gets done
     */
    public void animate(@Nullable ItemDisplay display, int duration, Matrix4f matrix) {
        if (display == null) return;

        animate(display, 0, duration, matrix);
    }

    public boolean shouldBreakBlockUsingTool(@NotNull Block block, @NotNull ItemStack tool) {
        return !block.getType().isAir()
                && !(block.getState() instanceof BlockInventoryHolder)
                && !BlockStorage.isRebarBlock(block)
                && block.getType().getHardness() >= 0
                && block.isPreferredTool(tool)
                && tool.hasData(DataComponentTypes.TOOL)
                && tool.hasData(DataComponentTypes.DAMAGE);
    }

    private static final Map<Material, Material> BLOCK_ITEM_FALLBACK = Map.of(
        Material.FIRE, Material.FLINT_AND_STEEL,
        Material.SOUL_FIRE, Material.FLINT_AND_STEEL
    );

    /**
     * Returns an item representing the key.
     *
     * If no representative item exists (e.g. fire) a fallback for that item will be used (if one exists)
     */
    public static ItemStack itemFromKey(NamespacedKey key) {
        ItemTypeWrapper wrapper = ItemTypeWrapper.of(key);

        if (!(wrapper instanceof ItemTypeWrapper.Vanilla vanilla)) {
            return wrapper.createItemStack();
        }

        Material mat = vanilla.material();
        if (mat.isItem()) {
            return vanilla.createItemStack();
        }

        Material fallback = BLOCK_ITEM_FALLBACK.getOrDefault(mat, Material.BARRIER);
        ItemStack stack = new ItemStack(fallback);

        if (fallback == Material.BARRIER) {
            stack.setData(
                DataComponentTypes.ITEM_NAME,
                Component.text("ERROR: " + mat)
            );
        }

        return stack;
    }

    /**
     * Handles players right clicking with bottles, water buckets, etc
     * Returns true if the function attempted to process the item used (i.e. if it's a water bucket, bottle, etc)
     */
    public boolean handleFluidTankRightClick(@NotNull RebarFluidTank tank, @NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.useInteractedBlock() == Event.Result.DENY) {
            return false;
        }

        ItemStack item = event.getItem();
        EquipmentSlot hand = event.getHand();
        if (item == null || hand == null || RebarItem.isRebarItem(item)) {
            return false;
        }

        Material type = item.getType();
        ItemStack newItemStack = null;
        boolean triggered = false;

        // Inserting water
        if (type == Material.WATER_BUCKET && tank.canAddFluid(PylonFluids.WATER, 1000.0)) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY);
            } else {
                tank.setFluidType(PylonFluids.WATER);
                tank.addFluid(1000.0);
                newItemStack = new ItemStack(Material.BUCKET);
            }
            triggered = true;
        }

        if (type == Material.POTION
                && item.hasData(DataComponentTypes.POTION_CONTENTS)
                && item.getData(DataComponentTypes.POTION_CONTENTS).potion() == PotionType.WATER
                && tank.canAddFluid(PylonFluids.WATER, 333.333)

        ) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY);
            } else {
                tank.setFluidType(PylonFluids.WATER);
                tank.setFluid(333.333);
                newItemStack = new ItemStack(Material.GLASS_BOTTLE);
            }
            triggered = true;
        }

        // Inserting lava
        if (type == Material.LAVA_BUCKET && tank.canAddFluid(PylonFluids.LAVA, 1000.0)) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY);
            } else if (tank.canAddFluid(PylonFluids.LAVA, 1000.0)) {
                tank.setFluidType(PylonFluids.LAVA);
                tank.addFluid(1000.0);
                newItemStack = new ItemStack(Material.BUCKET);
            }
        }

        if (type == Material.BUCKET && tank.getFluidAmount() >= 1000.0 && (tank.getFluidType() == PylonFluids.WATER || tank.getFluidType() == PylonFluids.LAVA)) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY);
            } else {
                tank.removeFluid(1000.0);
                newItemStack = new ItemStack(tank.getFluidType() == PylonFluids.WATER ? Material.WATER_BUCKET : Material.LAVA_BUCKET);
            }
            triggered = true;
        }

        if (item.getType() == Material.GLASS_BOTTLE && tank.getFluidAmount() >= 333.333 && PylonFluids.WATER.equals(tank.getFluidType())) {
            if (priority == EventPriority.NORMAL) {
                event.setUseItemInHand(Event.Result.DENY);
            } else {
                tank.setFluid(Math.max(0.0, tank.getFluidAmount() - 333.333));
                newItemStack = new ItemStack(Material.POTION);
                newItemStack.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().potion(PotionType.WATER));
            }
            triggered = true;
        }

        if (newItemStack != null) {
            event.getPlayer().swingHand(hand);
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                // This is a hack. When I change the item from within a PlayerInteractEvent, a new event
                // is fired for the new item stack. No idea why. Nor did the guy from the paper team.
                ItemStack finalNewItemStack = newItemStack;
                Bukkit.getScheduler().runTask(Pylon.getInstance(), () -> {
                    item.subtract();
                    event.getPlayer().give(finalNewItemStack);
                });
            }
            return true;
        }
        return triggered;
    }

    @Nullable
    public ItemStack damageTool(@NotNull ItemStack tool, int damage) {
        if (tool.hasData(DataComponentTypes.UNBREAKABLE)) {
            return tool;
        }

        tool.setData(DataComponentTypes.DAMAGE, tool.getData(DataComponentTypes.DAMAGE) + damage);
        if (tool.getData(DataComponentTypes.DAMAGE) >= tool.getData(DataComponentTypes.MAX_DAMAGE)) {
            return null;
        }
        return tool;
    }
}
