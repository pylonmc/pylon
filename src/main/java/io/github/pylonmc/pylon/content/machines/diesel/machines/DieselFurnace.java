package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonFluids;
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
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeType;
import io.github.pylonmc.rebar.recipe.vanilla.FurnaceRecipeWrapper;
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

import java.util.List;
import java.util.Map;


public class DieselFurnace extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarFluidBufferBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarFurnace,
        RebarRecipeProcessor<FurnaceRecipeWrapper> {

    public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
    public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);
    public final int recipeTime = (int) Math.round(20 * 8 / speed);

    public ItemStackBuilder sideStack1 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side1");
    public ItemStackBuilder sideStack2 = ItemStackBuilder.of(Material.BRICKS)
            .addCustomModelDataString(getKey() + ":side2");
    public ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.CYAN_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":chimney");

    private final VirtualInventory inputInventory = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    public static class Item extends RebarItem {

        public final double dieselPerSecond = getSettings().getOrThrow("diesel-per-second", ConfigAdapter.DOUBLE);
        public final double dieselBuffer = getSettings().getOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);
        public final double speed = getSettings().getOrThrow("speed", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("diesel-usage", UnitFormat.MILLIBUCKETS_PER_SECOND.format(dieselPerSecond)),
                    RebarArgument.of("diesel-buffer", UnitFormat.MILLIBUCKETS.format(dieselBuffer)),
                    RebarArgument.of("speed", UnitFormat.PERCENT.format(speed * 100))
            );
        }
    }

    @SuppressWarnings("unused")
    public DieselFurnace(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        createFluidPoint(FluidPointType.INPUT, BlockFace.UP);
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
        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);
        setRecipeType(FurnaceRecipeType.INSTANCE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background()));
    }

    @SuppressWarnings("unused")
    public DieselFurnace(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
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
        if (!isProcessingRecipe() || fluidAmount(PylonFluids.BIODIESEL) < dieselPerSecond * tickInterval / 20) {
            return;
        }

        removeFluid(PylonFluids.BIODIESEL, dieselPerSecond * tickInterval / 20);
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
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.BIODIESEL),
                        fluidCapacity(PylonFluids.BIODIESEL),
                        20,
                        TextColor.fromHexString("#eaa627")
                ))
        ));
    }

    @Override
    public void onEndSmelting(@NotNull BlockCookEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void onFuelBurn(@NotNull FurnaceBurnEvent event) {
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
