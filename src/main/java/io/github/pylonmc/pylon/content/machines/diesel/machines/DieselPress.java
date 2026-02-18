package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.recipes.PressRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
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
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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


public class DieselPress extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarFluidBufferBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<PressRecipe> {

    public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
    public final double plantOilBuffer = getSettings().getOrThrow("plant-oil-buffer", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double timePerItem = getSettings().getOrThrow("time-per-item", ConfigAdapter.DOUBLE);

    private final VirtualInventory inputInventory = new VirtualInventory(1);

    public static class Item extends RebarItem {

        public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
        public final double timePerItem = getSettings().getOrThrow("time-per-item", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselPerSecond)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer)),
                    RebarArgument.of("time-per-item", UnitFormat.SECONDS.format(timePerItem))
            );
        }
    }

    public ItemStackBuilder pressStack = ItemStackBuilder.of(Material.COMPOSTER)
            .addCustomModelDataString(getKey() + ":press");
    public ItemStackBuilder pressLidStack = ItemStackBuilder.of(Material.COMPOSTER)
            .addCustomModelDataString(getKey() + ":press_lid");
    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    @SuppressWarnings("unused")
    public DieselPress(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false, 0.55F);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false, 0.55F);
        setFacing(context.getFacing());
        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(chimneyStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.37499, -0.15, 0.0)
                        .scale(0.15, 1.7, 0.15))
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
        addEntity("press", new ItemDisplayBuilder()
                .itemStack(pressStack)
                .transformation(new TransformBuilder()
                        .translate(0, 0.3, 0)
                        .scale(0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("press_lid", new ItemDisplayBuilder()
                .itemStack(pressLidStack)
                .transformation(new TransformBuilder()
                        .translate(0, 0.3, 0)
                        .rotate(Math.PI, 0, 0)
                        .scale(0.5999))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
        createFluidBuffer(PylonFluids.PLANT_OIL, plantOilBuffer, false, true);
        setRecipeType(PressRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
    }

    @SuppressWarnings("unused")
    public DieselPress(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("input", LogisticGroupType.INPUT, inputInventory);
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
                new Vector3d(0.375, 1.5, 0),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .offset(0, 1, 0)
                .count(0)
                .extra(0.05)
                .spawn();
    }

    public void tryStartRecipe() {
        if (isProcessingRecipe()) {
            return;
        }

        ItemStack stack = inputInventory.getItem(0);
        if (stack == null) {
            return;
        }

        for (PressRecipe recipe : PressRecipe.RECIPE_TYPE) {
            double plantOilAmount = recipe.oilAmount();
            if (!recipe.input().matches(stack) || fluidSpaceRemaining(PylonFluids.PLANT_OIL) < plantOilAmount) {
                continue;
            }

            startRecipe(recipe, (int) (timePerItem * 20));
            getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
            inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract(recipe.input().getAmount()));
            break;
        }
    }

    @Override
    public void onRecipeFinished(@NotNull PressRecipe recipe) {
        addFluid(PylonFluids.PLANT_OIL, recipe.oilAmount());
        getRecipeProgressItem().setItem(GuiItems.background());
        tryStartRecipe();
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # I # # # #",
                        "# # # # i # # # #",
                        "# # # # p # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
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
                )),
                RebarArgument.of("plant-oil-bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.PLANT_OIL),
                        fluidCapacity(PylonFluids.PLANT_OIL),
                        20,
                        TextColor.fromHexString("#c4b352")
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
                "input", inputInventory
        );
    }
}
