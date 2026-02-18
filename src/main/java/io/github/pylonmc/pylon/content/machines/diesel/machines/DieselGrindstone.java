package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.recipes.GrindstoneRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class DieselGrindstone extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarFluidBufferBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<GrindstoneRecipe> {

    public static final NamespacedKey STONE_ROTATION_KEY = pylonKey("stone_rotation");

    public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(3);
    private double stoneRotation;

    public static class Item extends RebarItem {

        public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselPerSecond)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer))
            );
        }
    }

    public ItemStackBuilder stoneStack = ItemStackBuilder.of(Material.SMOOTH_STONE)
            .addCustomModelDataString(getKey() + ":stone");
    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    @SuppressWarnings("unused")
    public DieselGrindstone(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.55F);
        setFacing(context.getFacing());
        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(chimneyStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.4, 0.0, -0.4)
                        .scale(0.15))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(sideStack1)
                .transformation(new TransformBuilder()
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(sideStack2)
                .transformation(new TransformBuilder()
                        .translate(0, -0.5, 0)
                        .scale(0.9, 0.8, 1.1))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("stone", new ItemDisplayBuilder()
                .itemStack(stoneStack)
                .transformation(new TransformBuilder()
                        .scale(0.6, 0.2, 0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
        setRecipeType(GrindstoneRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
        stoneRotation = 0;
    }

    @SuppressWarnings("unused")
    public DieselGrindstone(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        stoneRotation = pdc.get(STONE_ROTATION_KEY, RebarSerializers.DOUBLE);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(STONE_ROTATION_KEY, RebarSerializers.DOUBLE, stoneRotation);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);
        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);
        outputInventory.addPostUpdateHandler(event -> tryStartRecipe());
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        });
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || fluidAmount(PylonFluids.BIODIESEL) < dieselPerSecond * tickInterval / 20) {
            return;
        }

        removeFluid(PylonFluids.BIODIESEL, dieselPerSecond * tickInterval / 20);
        progressRecipe(tickInterval);
        Vector smokePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0.4, 0.7, -0.4),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .offset(0, 1, 0)
                .count(0)
                .extra(0.05)
                .spawn();
        stoneRotation += Math.PI / 2.2;
        PylonUtils.animate(
                getHeldEntityOrThrow(ItemDisplay.class, "stone"),
                tickInterval,
                new TransformBuilder()
                        .scale(0.6, 0.2, 0.6)
                        .rotate(0, stoneRotation, 0)
                        .buildForItemDisplay()
        );
    }

    public void tryStartRecipe() {
        if (isProcessingRecipe()) {
            return;
        }

        ItemStack stack = inputInventory.getItem(0);
        if (stack == null) {
            return;
        }

        recipeLoop:
        for (GrindstoneRecipe recipe : GrindstoneRecipe.RECIPE_TYPE) {
            if (!recipe.input().matches(stack)) {
                continue;
            }

            for (ItemStack output : recipe.results().getElements()) {
                if (!outputInventory.canHold(output)) {
                    break recipeLoop;
                }
            }

            startRecipe(recipe, recipe.cycles() * Grindstone.CYCLE_DURATION_TICKS);
            getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
            inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
            break;
        }
    }

    @Override
    public void onRecipeFinished(@NotNull GrindstoneRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        outputInventory.addItem(null, recipe.results().getRandom());
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# I # # # O O O #",
                        "# i # p # o o o #",
                        "# I # # # O O O #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .build();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("diesel-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.BIODIESEL),
                        fluidCapacity(PylonFluids.BIODIESEL),
                        20,
                        TextColor.fromHexString("#eaa627")
                ))
        ));
    }

    @Override
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "output", outputInventory
        );
    }
}
