package io.github.pylonmc.pylon.content.machines.chemistry;

import com.google.common.collect.Iterables;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.recipes.ChemicalPlantRecipe;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.BlockTypeWrapper;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.electricity.nodes.ElectricNodeType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.ingredient.FluidChoice;
import io.github.pylonmc.rebar.recipe.ingredient.FluidWithAmount;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.persistence.PersistentDataKey;
import java.util.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class ChemicalPlant extends RebarBlock implements
        DirectionalRebarBlock,
        SimpleRebarMultiblock,
        SimpleElectricRebarBlock,
        TickingRebarBlock,
        GuiRebarBlock {

    public static class Item extends RebarItem {

        private final double powerUsage = getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("power", UnitFormat.WATTS.format(powerUsage).decimalPlaces(2).selectPrefixAndRescale()));
        }
    }

    private static final PersistentDataKey<ChemicalPlantRecipe> RECIPE_KEY = PersistentDataKey.of(
            pylonKey("recipe"),
            RebarSerializers.REBAR_RECIPE.recipeTypeFrom(ChemicalPlantRecipe.class, ChemicalPlantRecipe.RECIPE_TYPE)
    );

    @Getter
    private @Nullable ChemicalPlantRecipe recipe = null;

    @SuppressWarnings("unused")
    public ChemicalPlant(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setTickInterval(getSettingOrThrow("tick-interval", ConfigAdapter.INTEGER));
        createSimpleElectricPort(ElectricNodeType.CONSUMER, getFacing());
        setRequiredPower(getSettingOrThrow("power-usage", ConfigAdapter.DOUBLE));
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public ChemicalPlant(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        recipe = pdc.get(RECIPE_KEY);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, RECIPE_KEY, recipe);
    }

    public void setRecipe(@Nullable ChemicalPlantRecipe recipe) {
        this.recipe = recipe;
        List<FluidChoice> inputFluids = recipe == null ? Collections.emptyList() : recipe.inputFluids();
        for (int i = 0; i < 3; i++) {
            FluidChoice choice = Iterables.get(inputFluids, i, null);
            List<RebarFluid> fluids = choice == null ? Collections.emptyList() : new ArrayList<>(choice.getFluids());
            getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUTS[i]).setAllowedFluids(fluids);
        }

        List<ItemChoice> inputItems = recipe == null ? Collections.emptyList() : recipe.inputItems();
        for (int i = 0; i < 3; i++) {
            ItemChoice choice = Iterables.get(inputItems, i, null);
            Set<ItemTypeWrapper> items = new HashSet<>();
            if (choice != null) {
                for (ItemStack representative : choice.getRepresentativeItems()) {
                    items.add(ItemTypeWrapper.of(representative));
                }
            }
            getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUTS[i]).setAllowedItems(items);
        }
    }

    private class RecipeButton extends AbstractItem {

        @Override
        public @NotNull ItemProvider getItemProvider(@NonNull Player viewer) {
            if (recipe == null) {
                return ItemStackBuilder.gui(Material.RED_STAINED_GLASS_PANE, "no_recipe")
                        .name(Component.translatable("pylon.gui.no-recipe"))
                        .lore(Component.translatable("pylon.gui.chemical-plant.click-to-select"));
            }
            if (!isFormedAndFullyLoaded()) {
                return ItemStackBuilder.gui(Material.RED_STAINED_GLASS_PANE, "incomplete")
                        .name(Component.translatable("pylon.gui.status.incomplete"))
                        .lore(Component.translatable(
                                "pylon.gui.chemical-plant.recipe",
                                RebarArgument.of("name", recipe.displayName())
                        ));
            }
            if (!isPowered()) {
                return ItemStackBuilder.gui(Material.RED_STAINED_GLASS_PANE, "no_power")
                        .name(Component.translatable(
                                "pylon.gui.status.no_power",
                                RebarArgument.of("power", UnitFormat.WATTS.format(getRequiredPower()).decimalPlaces(2).selectPrefixAndRescale())
                        ))
                        .lore(Component.translatable(
                                "pylon.gui.chemical-plant.recipe",
                                RebarArgument.of("name", recipe.displayName())
                        ));
            }
            if (!recipeHasRequiredModules()) {
                return ItemStackBuilder.gui(Material.RED_STAINED_GLASS_PANE, "missing_modules")
                        .name(Component.translatable("pylon.gui.chemical-plant.missing-modules"))
                        .lore(Component.translatable(
                                "pylon.gui.chemical-plant.recipe",
                                RebarArgument.of("name", recipe.displayName())
                        ));
            }
            return ItemStackBuilder.gui(Material.GREEN_STAINED_GLASS_PANE, "running")
                    .name(Component.translatable("pylon.gui.status.running")).lore(Component.translatable(
                            "pylon.gui.chemical-plant.recipe",
                            RebarArgument.of("name", recipe.displayName())
                    ));
        }

        @Override
        public void handleClick(@NonNull ClickType clickType, @NonNull Player player, @NonNull Click click) {
            selectRecipe(player);
        }
    }

    private final RecipeButton recipeButton = new RecipeButton();

    private void selectRecipe(Player player) {
        List<xyz.xenondevs.invui.item.Item> items = new ArrayList<>();
        items.add(xyz.xenondevs.invui.item.Item.builder()
                .setItemProvider(ItemStackBuilder.gui(Material.BARRIER, "none").name(Component.translatable("pylon.gui.none")))
                .addClickHandler(_ -> {
                    setRecipe(null);
                    open(player);
                })
                .build());
        for (ChemicalPlantRecipe recipe : ChemicalPlantRecipe.RECIPE_TYPE) {
            items.add(xyz.xenondevs.invui.item.Item.builder()
                    .setItemProvider(new ItemWrapper(recipe.createDisplayItem()))
                    .addClickHandler(_ -> {
                        setRecipe(recipe);
                        open(player);
                    })
                    .build());
        }
        Gui gui = PagedGui.itemsBuilder()
                .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', GuiItems.pagePrevious())
                .addIngredient('>', GuiItems.pageNext())
                .setContent(items)
                .build();
        Window.builder()
                .setUpperGui(gui)
                .setTitle(Component.translatable("pylon.gui.select-recipe"))
                .open(player);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # r # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('r', recipeButton)
                .build();
    }

    @Override
    public void tick() {
        recipeButton.notifyWindows();
        if (!isPowered() || !isFormedAndFullyLoaded() || recipe == null || !recipeHasRequiredModules()) return;

        List<FluidChoice> inputFluids = recipe.inputFluids();
        List<ItemChoice> inputItems = recipe.inputItems();
        List<FluidWithAmount> resultFluids = recipe.resultFluids();
        List<ItemStack> resultItems = recipe.resultItems();

        // Check if enough inputs
        for (int i = 0; i < inputFluids.size(); i++) {
            FluidChoice choice1 = inputFluids.get(i);
            FluidInputHatch hatch2 = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUTS[i]);
            if (!choice1.matches(hatch2.getFluidType(), hatch2.getFluidAmount())) return;
        }

        for (int i = 0; i < inputItems.size(); i++) {
            ItemChoice choice1 = inputItems.get(i);
            ItemInputHatch hatch2 = getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUTS[i]);
            if (!choice1.matches(hatch2.getItem())) return;
        }

        // Check if outputs can fit
        for (int i = 0; i < resultFluids.size(); i++) {
            FluidWithAmount result1 = resultFluids.get(i);
            FluidOutputHatch hatch2 = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUTS[i]);
            if (!hatch2.canAddFluid(result1)) return;
        }

        for (int i = 0; i < resultItems.size(); i++) {
            ItemStack result1 = resultItems.get(i);
            ItemOutputHatch hatch2 = getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUTS[i]);
            if (!hatch2.inventory.canHold(result1)) return;
        }

        // Consume inputs
        for (int i = 0; i < inputFluids.size(); i++) {
            FluidChoice choice = inputFluids.get(i);
            FluidInputHatch hatch1 = getMultiblockComponentOrThrow(FluidInputHatch.class, FLUID_INPUTS[i]);
            hatch1.removeFluid(choice.getAmount());
        }

        for (int i = 0; i < inputItems.size(); i++) {
            ItemChoice choice = inputItems.get(i);
            ItemInputHatch hatch1 = getMultiblockComponentOrThrow(ItemInputHatch.class, ITEM_INPUTS[i]);
            hatch1.inventory.setItem(new MachineUpdateReason(), 0, hatch1.getItem().subtract(choice.getAmount()));
        }

        // Add outputs
        for (int i = 0; i < resultFluids.size(); i++) {
            FluidWithAmount result = resultFluids.get(i);
            FluidOutputHatch hatch = getMultiblockComponentOrThrow(FluidOutputHatch.class, FLUID_OUTPUTS[i]);
            hatch.addFluid(result);
        }

        for (int i = 0; i < resultItems.size(); i++) {
            ItemStack result = resultItems.get(i);
            ItemOutputHatch hatch = getMultiblockComponentOrThrow(ItemOutputHatch.class, ITEM_OUTPUTS[i]);
            hatch.inventory.addItem(new MachineUpdateReason(), result);
        }
    }

    public boolean recipeHasRequiredModules() {
        if (recipe == null) return false;

        moduleLoop:
        for (NamespacedKey module : recipe.modules()) {
            for (int i = 1; i <= 2; i++) {
                Block relative = getBlock().getRelative(0, i, 0);
                BlockTypeWrapper type = BlockTypeWrapper.of(relative);
                boolean valid = type.getKey().equals(module);
                if (valid && type instanceof BlockTypeWrapper.Rebar) {
                    ConditionalModuleBlock moduleBlock = BlockStorage.getAs(ConditionalModuleBlock.class, relative);
                    if (moduleBlock != null && !moduleBlock.isActive()) {
                        valid = false;
                    }
                }
                if (valid) continue moduleLoop;
            }
            return false;
        }
        return true;
    }

    private static final Vector3i[] FLUID_INPUTS = {
            new Vector3i(-1, 1, 0),
            new Vector3i(-2, 1, 0),
            new Vector3i(-3, 1, 0),
    };
    private static final Vector3i[] FLUID_OUTPUTS = {
            new Vector3i(1, 1, 0),
            new Vector3i(2, 1, 0),
            new Vector3i(3, 1, 0),
    };
    private static final Vector3i[] ITEM_INPUTS = {
            new Vector3i(-1, 0, 0),
            new Vector3i(-2, 0, 0),
            new Vector3i(-3, 0, 0),
    };
    private static final Vector3i[] ITEM_OUTPUTS = {
            new Vector3i(1, 0, 0),
            new Vector3i(2, 0, 0),
            new Vector3i(3, 0, 0),
    };

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> components = new HashMap<>();
        for (Vector3i input : FLUID_INPUTS) {
            components.put(input, MultiblockComponent.of(PylonKeys.FLUID_INPUT_HATCH));
        }
        for (Vector3i output : FLUID_OUTPUTS) {
            components.put(output, MultiblockComponent.of(PylonKeys.FLUID_OUTPUT_HATCH));
        }
        for (Vector3i input : ITEM_INPUTS) {
            components.put(input, MultiblockComponent.of(PylonKeys.ITEM_INPUT_HATCH));
        }
        for (Vector3i output : ITEM_OUTPUTS) {
            components.put(output, MultiblockComponent.of(PylonKeys.ITEM_OUTPUT_HATCH));
        }
        return components;
    }

    @Override
    public void onMultiblockFormed() {
        SimpleRebarMultiblock.super.onMultiblockFormed();
        setRecipe(this.recipe);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (!isPowered()) {
            display.add(Component.translatable(
                    "pylon.message.no_power",
                    RebarArgument.of("power", UnitFormat.WATTS.format(getRequiredPower()).decimalPlaces(2).selectPrefixAndRescale())
            ));
        }
        return display;
    }

    /**
     * Implemented by modules which have other conditions to work
     */
    public interface ConditionalModuleBlock {
        boolean isActive();
    }
}