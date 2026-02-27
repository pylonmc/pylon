package io.github.pylonmc.pylon.content.machines.fluid;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.*;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeType;
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeWrapper;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FluidFurnace extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarFluidBufferBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarFurnace,
        RebarRecipeProcessor<FurnaceRecipeWrapper> {

    public final double fluidBuffer = getSettings().getOrThrow("fluid-buffer", ConfigAdapter.DOUBLE);
    public final double fluidConsumptionRate = getSettings().getOrThrow("input-fluid-per-second", ConfigAdapter.DOUBLE);
    public final @Nullable Double fluidOutputRate = getSettings().get("output-fluid-per-second", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
    public final int recipeTime = (int) Math.round(20 * 8 / speed);
    public final @NotNull RebarFluid inputFluid = getSettings().getOrThrow("input-fluid", ConfigAdapter.REBAR_FLUID);
    public final @Nullable RebarFluid outputFluid = getSettings().get("output-fluid", ConfigAdapter.REBAR_FLUID);

    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    public static class Item extends RebarItem {

        public final double fluidPerSecond = getSettings().getOrThrow("input-fluid-per-second", ConfigAdapter.DOUBLE);
        public final double fluidBuffer = getSettings().getOrThrow("fluid-buffer", ConfigAdapter.DOUBLE);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
        public final @NotNull RebarFluid inputFluid = getSettings().getOrThrow("input-fluid", ConfigAdapter.REBAR_FLUID);
        public final @Nullable RebarFluid outputFluid = getSettings().get("output-fluid", ConfigAdapter.REBAR_FLUID);
        public final @Nullable Double fluidOutputRate = getSettings().get("output-fluid-per-second", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            ArrayList<RebarArgument> args = new ArrayList<>(List.of(RebarArgument.of("fluid-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(fluidPerSecond)),
                    RebarArgument.of("fluid-buffer", UnitFormat.MILLIBUCKETS.format(fluidBuffer)),
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100)),
                    RebarArgument.of("input-fluid", Component.translatable(inputFluid.getKey() + ".name"))));
            if(outputFluid != null && fluidOutputRate != null){
                args.add(RebarArgument.of("output-fluid", Component.translatable(outputFluid.getKey() + ".name")));
                args.add(RebarArgument.of("fluid-output-rate", UnitFormat.MILLIBUCKETS_PER_SECOND.format(fluidOutputRate)));
            }
            return args;
        }
    }

    @SuppressWarnings("unused")
    public FluidFurnace(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, BlockFace.WEST);
        if(outputFluid != null){
            createFluidPoint(FluidPointType.OUTPUT, BlockFace.EAST);
        }
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
                        .lookAlong(getFacing())
                        .translate(0, -0.5, -0.1)
                        .scale(0.8, 0.8, 0.9))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(sideStack2)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, -0.5, 0)
                        .scale(1.1, 0.8, 0.8))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        createFluidBuffer(inputFluid, fluidBuffer, true, false);
        if(outputFluid != null) {
            createFluidBuffer(outputFluid, fluidBuffer, false, true);
        }
        setRecipeType(FurnaceRecipeType.INSTANCE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
    }

    @SuppressWarnings("unused")
    public FluidFurnace(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
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
    public void onBreak(@NotNull List<@NotNull ItemStack> drops, @NotNull BlockBreakContext context) {
        RebarVirtualInventoryBlock.super.onBreak(drops, context);
        RebarFluidBufferBlock.super.onBreak(drops, context);
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe() || fluidAmount(inputFluid) < fluidConsumptionRate * tickInterval / 20) {
            return;
        }

        removeFluid(inputFluid,fluidConsumptionRate * tickInterval / 20);
        if(outputFluid != null && fluidOutputRate != null){
            addFluid(outputFluid, fluidOutputRate * tickInterval / 20);
        }
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
        progressRecipe(tickInterval);
    }

    public void tryStartRecipe() {
        if (isProcessingRecipe()) {
            return;
        }

        ItemStack stack = inputInventory.getItem(0);
        if (stack == null) {
            return;
        }

        for (FurnaceRecipeWrapper recipe : FurnaceRecipeType.INSTANCE) {
            if (!recipe.isInput(stack) || !outputInventory.canHold(recipe.getRecipe().getResult())) {
                continue;
            }

            startRecipe(recipe, recipeTime);
            getRecipeProgressItem().setItem(ItemStackBuilder.of(stack.asOne()).clearLore());
            inputInventory.setItem(new MachineUpdateReason(), 0, stack.subtract());
            break;
        }
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # I # # # O # #",
                        "# # i # p # o # #",
                        "# # I # # # O # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('p', getRecipeProgressItem())
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .build();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        RebarArgument inputBar = RebarArgument.of("inputBar", PylonUtils.createFluidAmountBar(
                fluidAmount(inputFluid),
                fluidCapacity(inputFluid),
                20,
                TextColor.fromHexString("#eaa627")
        ));
        if(outputFluid != null) {
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                    inputBar,
                    RebarArgument.of("outputBar", PylonUtils.createFluidAmountBar(
                            fluidAmount(outputFluid),
                            fluidCapacity(outputFluid),
                            20,
                            TextColor.fromHexString("#eaa627")
                    ))
            ));
        } else {
            return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                    inputBar
            ));
        }
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onEndSmelting(@NotNull BlockCookEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onFuelBurn(@NotNull FurnaceBurnEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override
    public void onRecipeFinished(@NotNull FurnaceRecipeWrapper recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        outputInventory.addItem(null, recipe.getRecipe().getResult().clone());
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of("input", inputInventory, "output", outputInventory);
    }
}
