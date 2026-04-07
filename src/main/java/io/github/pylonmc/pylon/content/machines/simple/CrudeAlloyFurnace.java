package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.recipes.CrudeAlloyFurnaceRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarLogisticBlock;
import io.github.pylonmc.rebar.block.base.RebarRecipeProcessor;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.ProgressItem;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class CrudeAlloyFurnace extends RebarBlock implements
        RebarGuiBlock,
        RebarVirtualInventoryBlock,
        RebarEntityHolderBlock,
        RebarDirectionalBlock,
        RebarTickingBlock,
        RebarLogisticBlock,
        RebarRecipeProcessor<CrudeAlloyFurnaceRecipe> {

    public static final NamespacedKey FUEL_TICKS_TOTAL_KEY = pylonKey("fuel_ticks_total");
    public static final NamespacedKey FUEL_TICKS_REMAINING_KEY = pylonKey("fuel_ticks_remaining");

    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    private final VirtualInventory fuelInventory = new VirtualInventory(1);
    private final VirtualInventory inputInventory1 = new VirtualInventory(1);
    private final VirtualInventory inputInventory2 = new VirtualInventory(1);
    private final VirtualInventory outputInventory = new VirtualInventory(1);

    public final ItemStackBuilder chamberStack = ItemStackBuilder.of(Material.IRON_BLOCK)
            .addCustomModelDataString(getKey() + ":chamber");
    public final ItemStackBuilder chimneyStack = ItemStackBuilder.of(Material.GRAY_CONCRETE)
            .addCustomModelDataString(getKey() + ":chimney");

    public final ItemStackBuilder burningStack = ItemStackBuilder.gui(Material.FLINT_AND_STEEL, getKey() + "fuel-left")
            .name(Component.translatable("pylon.gui.fuel-left"));
    public final ItemStackBuilder fuelStack = ItemStackBuilder.gui(Material.BLACK_STAINED_GLASS_PANE, getKey() + "fuel")
            .name(Component.translatable("pylon.gui.fuel"));

    private final ProgressItem fuelProgressItem = new ProgressItem(GuiItems.background());

    private int fuelTicksTotal;
    private int fuelTicksRemaining;

    @SuppressWarnings("unused")
    public CrudeAlloyFurnace(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setTickInterval(tickInterval);
        setFacing(context.getFacing());
        addEntity("chimney", new ItemDisplayBuilder()
                .itemStack(chimneyStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.0, -0.5, -0.35)
                        .scale(0.2, 1.6, 0.2))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        addEntity("chamber", new ItemDisplayBuilder()
                .itemStack(chamberStack)
                .transformation(new TransformBuilder()
                        .translate(0, -0.1, 0)
                        .scale(0.6))
                .build(block.getLocation().toCenterLocation().add(0, 0.5, 0))
        );
        setRecipeType(CrudeAlloyFurnaceRecipe.RECIPE_TYPE);
        setRecipeProgressItem(new ProgressItem(GuiItems.background(), false));
    }

    @SuppressWarnings("unused")
    public CrudeAlloyFurnace(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        fuelTicksTotal = pdc.get(FUEL_TICKS_TOTAL_KEY, RebarSerializers.INTEGER);
        fuelTicksRemaining = pdc.get(FUEL_TICKS_REMAINING_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(FUEL_TICKS_TOTAL_KEY, RebarSerializers.INTEGER, fuelTicksTotal);
        pdc.set(FUEL_TICKS_REMAINING_KEY, RebarSerializers.INTEGER, fuelTicksRemaining);
    }

    @Override
    public void postInitialise() {
        createLogisticGroup("fuel", LogisticGroupType.INPUT, fuelInventory);
        createLogisticGroup("input1", LogisticGroupType.INPUT, inputInventory1);
        createLogisticGroup("input2", LogisticGroupType.INPUT, inputInventory2);
        createLogisticGroup("output", LogisticGroupType.OUTPUT, outputInventory);

        outputInventory.addPreUpdateHandler(RebarUtils.DISALLOW_PLAYERS_FROM_ADDING_ITEMS_HANDLER);

        Consumer<ItemPostUpdateEvent> startRecipeHandler = (ItemPostUpdateEvent event) -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                tryStartRecipe();
            }
        };
        fuelInventory.addPostUpdateHandler(startRecipeHandler);
        inputInventory1.addPostUpdateHandler(startRecipeHandler);
        inputInventory2.addPostUpdateHandler(startRecipeHandler);
        outputInventory.addPostUpdateHandler(startRecipeHandler);
    }

    @Override
    public void tick() {
        if (!isProcessingRecipe()) {
            return;
        }

        if (fuelTicksRemaining <= 0) {
            tryConsumeFuel();
            if (fuelTicksRemaining <= 0) {
                return;
            }
        }

        fuelTicksRemaining -= getTickInterval();
        fuelProgressItem.setTotalTimeTicks(fuelTicksTotal);
        fuelProgressItem.setRemainingTimeTicks(fuelTicksRemaining);
        if (fuelTicksRemaining <= 0) {
            fuelProgressItem.setTotalTimeTicks(null);
            fuelProgressItem.setItem(GuiItems.background());
        }

        progressRecipe(getTickInterval());
        Vector smokePosition = Vector.fromJOML(RebarUtils.rotateVectorToFace(
                new Vector3d(0.0, 0.8, -0.35),
                getFacing().getOppositeFace()
        ));
        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .location(getBlock().getLocation().toCenterLocation().add(smokePosition))
                .offset(0, 1, 0)
                .count(0)
                .extra(0.05)
                .spawn();
    }

    public void tryConsumeFuel() {
        if (fuelTicksRemaining > 0) {
            return;
        }

        ItemStack fuel = fuelInventory.getItem(0);
        if (fuel == null) {
            return;
        }

        // dividing by 10 due to suspected bug with getBurnDuration
        fuelTicksTotal = Registry.ITEM.getOrThrow(fuel.getType().key()).getBurnDuration() / 10;
        fuelTicksRemaining = fuelTicksTotal;
        fuelProgressItem.setItem(burningStack);
        fuelProgressItem.setTotalTimeTicks(fuelTicksTotal);
        fuelProgressItem.setRemainingTimeTicks(fuelTicksRemaining);
        fuelInventory.setItem(new MachineUpdateReason(), 0, fuel.subtract());
    }

    public void tryStartRecipe() {
        if (isProcessingRecipe()) {
            return;
        }

        ItemStack input1 = inputInventory1.getItem(0);
        ItemStack input2 = inputInventory2.getItem(0);

        for (CrudeAlloyFurnaceRecipe recipe : CrudeAlloyFurnaceRecipe.RECIPE_TYPE) {
            boolean matches = recipe.input1().matches(input1) && recipe.input2().matches(input2);
            boolean matchesReversed = recipe.input1().matches(input2) && recipe.input2().matches(input1);
            if ((!matches && !matchesReversed) || !outputInventory.canHold(recipe.result())) {
                continue;
            }

            startRecipe(recipe, recipe.timeTicks());
            getRecipeProgressItem().setItem(ItemStackBuilder.of(recipe.result().asOne()).clearLore());

            RecipeInput.Item recipeInputInInventory1 = matches ? recipe.input1() : recipe.input2();
            RecipeInput.Item recipeInputInInventory2 = matches ? recipe.input2() : recipe.input1();

            if (input1 != null) {
                inputInventory1.setItem(new MachineUpdateReason(), 0, input1.subtract(recipeInputInInventory1.getAmount()));
            }
            if (input2 != null) {
                inputInventory2.setItem(new MachineUpdateReason(), 0, input2.subtract(recipeInputInInventory2.getAmount()));
            }
            tryConsumeFuel();
            break;
        }
    }

    @Override
    public void onRecipeFinished(@NotNull CrudeAlloyFurnaceRecipe recipe) {
        getRecipeProgressItem().setItem(GuiItems.background());
        outputInventory.addItem(new MachineUpdateReason(), recipe.result().clone());
        tryStartRecipe();
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # O #",
                        "I i j I # p # o #",
                        "# # b # # # # O #",
                        "# F f F # # # # #",
                        "# # # # # # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory1)
                .addIngredient('j', inputInventory2)
                .addIngredient('p', getRecipeProgressItem())
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .addIngredient('f', fuelInventory)
                .addIngredient('F', fuelStack)
                .addIngredient('b', fuelProgressItem)
                .build();
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        Double progress = getRecipeProgress();
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("info", progress == null
                                ? Component.text("")
                                : Component.translatable("pylon.item.crude_alloy_furnace.info").arguments(
                                RebarArgument.of("progress", PylonUtils.createProgressBar(
                                        1 - progress,
                                        20,
                                        NamedTextColor.WHITE
                                ))
                        )
                )
        ));
    }

    @Override
    public @NotNull Map<String, VirtualInventory> getVirtualInventories() {
        return Map.of(
                "fuel", fuelInventory,
                "input1", inputInventory1,
                "input2", inputInventory2,
                "output", outputInventory
        );
    }
}
