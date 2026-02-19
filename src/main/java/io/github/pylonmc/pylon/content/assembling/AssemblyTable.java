package io.github.pylonmc.pylon.content.assembling;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.recipes.AssemblingRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarEntityHolderBlock;
import io.github.pylonmc.rebar.block.base.RebarGuiBlock;
import io.github.pylonmc.rebar.block.base.RebarVirtualInventoryBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.TextDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.util.MachineUpdateReason;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3i;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.inventory.OperationCategory;
import xyz.xenondevs.invui.inventory.VirtualInventory;

import java.util.HashSet;
import java.util.Map;


public class AssemblyTable extends RebarBlock implements
        RebarVirtualInventoryBlock,
        RebarDirectionalBlock,
        RebarEntityHolderBlock,
        RebarGuiBlock {

    private static final NamespacedKey RECIPE_KEY = PylonUtils.pylonKey("recipe");
    private static final NamespacedKey STEP_INDEX_KEY = PylonUtils.pylonKey("step_index");
    private static final NamespacedKey REMAINING_CLICKS_KEY = PylonUtils.pylonKey("remaining_clicks");
    private static final PersistentDataType<String, AssemblingRecipe> ASSEMBLING_RECIPE_TYPE = RebarSerializers.KEYED.keyedTypeFrom(
            AssemblingRecipe.class,
            AssemblingRecipe.RECIPE_TYPE::getRecipeOrThrow
    );

    public final ItemStackBuilder topStack = ItemStackBuilder.of(Material.STRIPPED_OAK_WOOD)
            .addCustomModelDataString(getKey() + ":top");
    public final ItemStackBuilder craftingTableStack = ItemStackBuilder.of(Material.CRAFTING_TABLE)
            .addCustomModelDataString(getKey() + ":crafting_Table");
    public final ItemStackBuilder worktopStack = ItemStackBuilder.of(Material.CYAN_CONCRETE)
            .addCustomModelDataString(getKey() + ":worktop");
    public final ItemStackBuilder legStack = ItemStackBuilder.of(Material.OAK_PLANKS)
            .addCustomModelDataString(getKey() + ":leg");
    public final ItemStackBuilder sideStack = ItemStackBuilder.of(Material.ORANGE_TERRACOTTA)
            .addCustomModelDataString(getKey() + ":worktop");

    private @Nullable AssemblingRecipe recipe;
    private int stepIndex;
    private int remainingClicks;
    private final VirtualInventory inputInventory = new VirtualInventory(6);
    private final VirtualInventory outputInventory = new VirtualInventory(6);

    public final double scale = getSettings().getOrThrow("scale",  ConfigAdapter.DOUBLE);
    public final double xOffset = getSettings().getOrThrow("x-offset",  ConfigAdapter.DOUBLE);
    public final double zOffset = getSettings().getOrThrow("z-offset",  ConfigAdapter.DOUBLE);
    public final int particleCount = getSettings().getOrThrow("particle-count",  ConfigAdapter.INT);

    @SuppressWarnings("unused")
    public AssemblyTable(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        recipe = null;
        stepIndex = -1;
        remainingClicks = -1;

        setFacing(context.getFacing());

        addEntity("top", new ItemDisplayBuilder()
                .itemStack(topStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.31, 0)
                        .scale(1.1, 0.4, 0.7)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("crafting-table-1", new ItemDisplayBuilder()
                .itemStack(craftingTableStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.3, 0.45, -0.125)
                        .scale(0.2)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("crafting-table-2", new ItemDisplayBuilder()
                .itemStack(craftingTableStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.3, 0.45, 0.125)
                        .scale(0.2)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("worktop", new ItemDisplayBuilder()
                .itemStack(worktopStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(scale + 0.1, 0.02005, scale + 0.1)
                )
                .build(getWorkspaceCenter())
        );
        addEntity("leg", new ItemDisplayBuilder()
                .itemStack(legStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .scale(0.65)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("side1", new ItemDisplayBuilder()
                .itemStack(sideStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.31, 0.35)
                        .scale(1.169, 0.5, 0.07)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("side2", new ItemDisplayBuilder()
                .itemStack(sideStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0, 0.31, -0.35)
                        .scale(1.169, 0.5, 0.07)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("side3", new ItemDisplayBuilder()
                .itemStack(sideStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(0.55, 0.31, 0)
                        .scale(0.07, 0.499, 0.699)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("side4", new ItemDisplayBuilder()
                .itemStack(sideStack)
                .transformation(new TransformBuilder()
                        .lookAlong(getFacing())
                        .translate(-0.55, 0.31, 0)
                        .scale(0.07, 0.499, 0.699)
                )
                .build(getBlock().getLocation().toCenterLocation())
        );

        addEntity("progress", new TextDisplayBuilder()
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .transformation(new TransformBuilder().scale(0.3))
                .build(getWorkspaceCenter().add(0, 0.8, 0))
        );
        addEntity("tool_name", new TextDisplayBuilder()
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .transformation(new TransformBuilder().scale(0.4))
                .build(getWorkspaceCenter().add(0, 0.7, 0))
        );
        addEntity("tool_item", new ItemDisplayBuilder()
                .billboard(Display.Billboard.VERTICAL)
                .transformation(new TransformBuilder().scale(0.3))
                .build(getWorkspaceCenter().add(0, 0.55, 0))
        );
        addEntity("tool_clicks_remaining", new TextDisplayBuilder()
                .billboard(Display.Billboard.VERTICAL)
                .backgroundColor(Color.fromARGB(0, 0, 0, 0))
                .transformation(new TransformBuilder().scale(0.4))
                .build(getWorkspaceCenter().add(0, 0.3, 0))
        );
        checkForNewRecipe();
    }

    @SuppressWarnings({"unused", "DataFlowIssue"})
    public AssemblyTable(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        recipe = pdc.get(RECIPE_KEY, ASSEMBLING_RECIPE_TYPE);
        stepIndex = pdc.get(STEP_INDEX_KEY, PersistentDataType.INTEGER);
        remainingClicks = pdc.get(REMAINING_CLICKS_KEY, PersistentDataType.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, RECIPE_KEY, ASSEMBLING_RECIPE_TYPE, recipe);
        pdc.set(STEP_INDEX_KEY, PersistentDataType.INTEGER, stepIndex);
        pdc.set(REMAINING_CLICKS_KEY, PersistentDataType.INTEGER, remainingClicks);
    }

    @Override
    public void postInitialise() {
        inputInventory.addPostUpdateHandler(event -> {
            if (!(event.getUpdateReason() instanceof MachineUpdateReason)) {
                checkForNewRecipe();
            }
        });
        inputInventory.setGuiPriority(OperationCategory.ADD, 1);
        outputInventory.setGuiPriority(OperationCategory.COLLECT, 1);
    }

    @Override
    public @NotNull Gui createGui() {
        return Gui.builder()
                .setStructure(
                        "# # # # # # # # #",
                        "I i i I # O o o O",
                        "I i i I # O o o O",
                        "I i i I # O o o O",
                        "# # # # # # # # #"
                )
                .addIngredient('#', GuiItems.background())
                .addIngredient('I', GuiItems.input())
                .addIngredient('i', inputInventory)
                .addIngredient('O', GuiItems.output())
                .addIngredient('o', outputInventory)
                .build();
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull VirtualInventory> getVirtualInventories() {
        return Map.of(
                "input", inputInventory,
                "output", outputInventory
        );
    }

    public void setStep(int i) {
        if (stepIndex != -1) {
            AssemblingRecipe.Step step = recipe.steps().get(stepIndex);

            for (String removeDisplay : step.removeDisplays()) {
                getHeldEntityOrThrow("recipe_display:" + removeDisplay).remove();
            }

            for (AssemblingRecipe.AddDisplay addDisplay : step.addDisplays()) {
                addEntity("recipe_display:" + addDisplay.name(), new ItemDisplayBuilder()
                        .itemStack(addDisplay.stack())
                        .transformation(new TransformBuilder()
                                .lookAlong(getFacing())
                                .translate(new Vector3d(
                                        (xOffset - addDisplay.position().x) * scale,
                                        (addDisplay.scale().y * scale) / 2,
                                        (zOffset - addDisplay.position().y) * scale
                                ))
                                .scale(new Vector3d(addDisplay.scale()).mul(scale))
                        )
                        .build(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
                );
            }
        }

        stepIndex = i;
        updateInfoDisplays();
    }

    public void setRemainingClicks(int i) {
        remainingClicks = i;
        updateInfoDisplays();
    }

    private void updateInfoDisplays() {
        AssemblingRecipe.Step step = recipe.steps().get(stepIndex);
        getHeldEntityOrThrow(ItemDisplay.class, "tool_item")
                .setItemStack(step.icon());
        getHeldEntityOrThrow(TextDisplay.class, "tool_name")
                .text(Component.translatable("pylon.gui.assembly_table.tools." + step.tool()));
        getHeldEntityOrThrow(TextDisplay.class, "progress").text(
                PylonUtils.createDiscreteProgressBar(
                        stepIndex,
                        recipe.steps().size(),
                        TextColor.color(120, 150, 255)
                )
        );
        getHeldEntityOrThrow(TextDisplay.class, "tool_clicks_remaining").text(
                Component.translatable("pylon.gui.assembly_table.clicks_remaining")
                        .arguments(RebarArgument.of("clicks", remainingClicks))
        );
    }

    public boolean isRecipeStarted() {
        return recipe != null && !(stepIndex == 0 && remainingClicks == recipe.steps().getFirst().clicks());
    }

    public void checkForNewRecipe() {
        if (isRecipeStarted()) {
            return;
        }

        for (String name : new HashSet<>(getHeldEntities().keySet())) {
            if (name.startsWith("recipe_display:")) {
                getHeldEntityOrThrow(name).remove();
            }
        }

        recipe = AssemblingRecipe.findRecipe(inputInventory.getItems());
        if (recipe != null) {
            setStep(0);
            setRemainingClicks(recipe.steps().getFirst().clicks());
        } else {
            stepIndex = -1;
            remainingClicks = -1;
            getHeldEntityOrThrow(TextDisplay.class, "progress").text(null);
            getHeldEntityOrThrow(TextDisplay.class, "tool_name").text(
                    Component.translatable("pylon.gui.assembly_table.no_recipe")
            );
            getHeldEntityOrThrow(ItemDisplay.class, "tool_item")
                    .setItemStack(new ItemStack(Material.BARRIER));
            getHeldEntityOrThrow(TextDisplay.class, "tool_clicks_remaining").text(null);
        }
    }

    /**
     * Returns true if tool used
     */
    public boolean useTool(@NotNull String toolName, @Nullable Player player) {
        if (recipe == null) {
            return false;
        }

        AssemblingRecipe.Step step = recipe.steps().get(stepIndex);
        if (!step.tool().equals(toolName)) {
            return false;
        }

        // Remove input items if recipe has not been started yet
        if (!isRecipeStarted()) {
            for (RecipeInput.Item item : recipe.inputs()) {
                for (int i = 0; i < inputInventory.getItems().length; i++) {
                    ItemStack stack = inputInventory.getItem(i);
                    if (stack != null && item.matches(stack)) {
                        inputInventory.addItemAmount(new MachineUpdateReason(), i, -item.getAmount());
                        break;
                    }
                }
            }
        }

        setRemainingClicks(remainingClicks - 1);
        if (remainingClicks != 0) {
            // Step not finished
            return true;
        }

        if (stepIndex + 1 != recipe.steps().size()) {
            // Recipe not finished
            setStep(stepIndex + 1);
            setRemainingClicks(recipe.steps().get(stepIndex).clicks());
            return true;
        }

        if (!outputInventory.canHold(recipe.results())) {
            // Can't hold output
            if (player != null) {
                player.sendMessage(Component.translatable("pylon.message.assembly_table.full"));
            }
            return false;
        }

        for (ItemStack stack : recipe.results()) {
            outputInventory.addItem(new MachineUpdateReason(), stack);
        }
        recipe = null;
        checkForNewRecipe();
        new ParticleBuilder(Particle.HAPPY_VILLAGER)
                .count(particleCount)
                .offset(scale / 2, 0, scale / 2)
                .location(getWorkspaceCenter())
                .spawn();
        return true;
    }

    public Location getWorkspaceCenter() {
        Vector3d offset = new Vector3d((0.5 - xOffset) * scale, 0.5, (0.5 - zOffset) * scale);
        return getBlock()
                .getLocation()
                .toCenterLocation()
                .add(Vector.fromJOML(RebarUtils.rotateVectorToFace(offset, getFacing())));
    }
}
