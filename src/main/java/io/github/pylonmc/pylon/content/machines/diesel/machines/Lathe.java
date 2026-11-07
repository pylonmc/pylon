package io.github.pylonmc.pylon.content.machines.diesel.machines;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.recipes.LatheRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.*;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.EntityStorage;
import io.github.pylonmc.rebar.entity.RebarEntity;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformUtil;
import io.github.pylonmc.rebar.entity.interfaces.InteractableRebarItemDisplay;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.logistics.LogisticGroupType;
import io.github.pylonmc.rebar.logistics.slot.ItemDisplayLogisticSlot;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.util.gui.GuiItems;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Lathe extends RebarBlock implements
        EntityHolderRebarBlock,
        DirectionalRebarBlock,
        FluidBufferRebarBlock,
        LogisticRebarBlock,
        InteractRebarBlockHandler,
        UnloadRebarBlockHandler {

    public static class Item extends RebarItem {

        @Getter
        private final double dieselPerItem = getSettingOrThrow("diesel-per-item", ConfigAdapter.DOUBLE);

        @SuppressWarnings("unused")
        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("usage", UnitFormat.MILLIBUCKETS_PER_ITEM.format(dieselPerItem)));
        }
    }

    @Getter
    private final double dieselBuffer = getSettingOrThrow("diesel-buffer", ConfigAdapter.DOUBLE);

    @Getter
    private final double dieselPerItem = getSettingOrThrow("diesel-per-item", ConfigAdapter.DOUBLE);

    @Getter
    private final int fullCycleTime = getSettingOrThrow("full-cycle-time", ConfigAdapter.INTEGER);

    private static final NamespacedKey RECIPE_KEY = pylonKey("recipe");
    private static final PersistentDataType<?, LatheRecipe> RECIPE_TYPE = RebarSerializers.KEYED.keyedTypeFrom(
            LatheRecipe.class,
            LatheRecipe.RECIPE_TYPE::getRecipeOrThrow
    );
    private @Nullable LatheRecipe recipe = null;

    @SuppressWarnings("unused")
    public Lathe(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());

        createFluidPoint(FluidPointType.INPUT, BlockFace.EAST);
        createFluidBuffer(PylonFluids.BIODIESEL, dieselBuffer, true, false);

        Quaternionf facingQuat = new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0));
        addEntity("base", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":base"))
                .transformation(new Matrix4f()
                        .scaleLocal(1.0004234388577438F, 0.29988064520392194F, 1.0003602451037632F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0F, -0.35F, 0F).rotate(facingQuat))))
        );

        addEntity("item", new ItemDisplayBuilder()
                .itemStack((ItemStack) null)
                .transformation(new Matrix4f()
                        .scaleLocal(0.35039115366152634F, 0.34960156474252563F, 0.3499448451292878F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(-0.25F, 0.15000000000000002F, 0F).rotate(facingQuat))))
        );

        addEntity("rod", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":rod"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.3501565987581572F, 0.20011426106329458F, 0.20011219704691643F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0.09999999999999998F, 0.15000000000000002F, 0F).rotate(facingQuat))))
        );

        addEntity("rod_base", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":rod_base"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.22470703568262898F, 0.700018252025617F, 0.6995026344071118F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0.38749999999999996F, 0.15000000000000002F, 0F).rotate(facingQuat))))
        );

        addEntity("moving_thingy", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("netherite_block")))
                        .addCustomModelDataString(getKey() + ":moving_thingy"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.09989957606017784F, 0.4248449950455608F, 0.09951330499663973F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(-0.25F, 0.012499999999999956F, 0.44999999999999996F).rotate(facingQuat))))
        );

        addEntity("bit", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("diamond_block")))
                        .addCustomModelDataString(getKey() + ":bit"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.049883224675357746F, 0.05020282626038203F, 0.07533509454877732F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(-0.25F, 0.17500000000000004F, 0.36250000000000004F).rotate(facingQuat))))
        );

        addEntity("display", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("gray_terracotta")))
                        .addCustomModelDataString(getKey() + ":display"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.3246554560647674F, 0.6996341968668202F, 0.050475593684390495F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0.3375F, 0.15000000000000002F, 0.375F).rotate(facingQuat))))
        );

        addEntity("pointer", new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("black_concrete")))
                        .addCustomModelDataString(getKey() + ":pointer"))
                .transformation(new Matrix4f()
                        .scaleLocal(0.07481127918105188F, 0.04998907656960586F, 0.024698269865597252F)
                        .rotateLocal(facingQuat)
                )
                .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0.21250000000000002F, 0.5F, 0.4125F).rotate(facingQuat))))
        );

        addEntity("button", new Lathe.ButtonEntity(
                new ItemDisplayBuilder()
                        .itemStack(ItemStackBuilder.of(Registry.MATERIAL.getOrThrow(NamespacedKey.minecraft("lodestone")))
                                .addCustomModelDataString(getKey() + ":button"))
                        .transformation(new Matrix4f()
                                .scaleLocal(0.19969952915351913F, 0.20042008245700776F, 0.049575707574394036F)
                                .translateLocal(0F, 0F, 0.4750000000000001F)
                                .rotateLocal(facingQuat)
                        )
                        .build(block.getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0F, -0.35F, 0.012499999999999956F).rotate(facingQuat)))),
                this
        ));
    }

    @SuppressWarnings("unused")
    public Lathe(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        recipe = pdc.get(RECIPE_KEY, RECIPE_TYPE);
    }


    private Matrix4f defaultMovingThingyTransform;
    private Matrix4f defaultBitTransform;
    private Matrix4f defaultPointerTransform;

    @Override
    public void postInitialise() {
        createLogisticGroup("item", LogisticGroupType.BOTH, new ItemDisplayLogisticSlot(getItemDisplay()));
        defaultMovingThingyTransform = TransformUtil.transformationToMatrix(getMovingThingy().getTransformation());
        defaultBitTransform = TransformUtil.transformationToMatrix(getBit().getTransformation());
        defaultPointerTransform = TransformUtil.transformationToMatrix(getPointer().getTransformation());
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, RECIPE_KEY, RECIPE_TYPE, recipe);
    }

    private final List<BukkitTask> tasks = new ArrayList<>();
    private int startTick = -1;

    @Override
    @MultiHandler(priorities = {EventPriority.NORMAL, EventPriority.MONITOR})
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND || event.useInteractedBlock() == Event.Result.DENY)
            return;

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        ItemDisplay display = getItemDisplay();
        ItemStack existingItem = display.getItemStack();
        if (!existingItem.isEmpty()) {
            reset();
            RebarUtils.addToInventoryOrDrop(player, existingItem);
            display.setItemStack(null);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) return;
        chooseRecipe(player, item);
    }

    private void chooseRecipe(Player player, ItemStack input) {
        if (input.isEmpty()) return;
        var items = LatheRecipe.RECIPE_TYPE.stream()
                .filter(recipe -> recipe.input().matchesIgnoringAmount(input))
                .map(recipe -> xyz.xenondevs.invui.item.Item.builder()
                        .setItemProvider(new ItemWrapper(recipe.result()))
                        .addClickHandler(_ -> {
                            getItemDisplay().setItemStack(input.asOne());
                            input.subtract();
                            player.closeInventory();
                            this.recipe = recipe;
                            setUpNewRecipe();
                        })
                        .build()
                )
                .toList();

        if (items.isEmpty()) {
            player.sendMessage(Component.translatable("pylon.message.invalid_recipe"));
            return;
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
                .setTitle(Component.translatable("pylon.gui.lathe.select-recipe"))
                .open(player);
    }

    private void setUpNewRecipe() {
        if (recipe == null) return;

        float completionValue = recipe.completionValue();
        float marginOfError = recipe.marginOfError();

        float greenStart = Math.clamp(completionValue - marginOfError, 0, 1);
        float greenEnd = Math.clamp(completionValue + marginOfError, 0, 1);
        @SuppressWarnings("UnnecessaryLocalVariable")
        float yellowEnd = greenStart;
        float yellowStart = Math.clamp(yellowEnd - marginOfError * 2, 0, 1);

        if (yellowStart > 0) {
            createSubdisplay(Material.RED_CONCRETE, 0, yellowStart);
        }
        createSubdisplay(Material.YELLOW_CONCRETE, yellowStart, yellowEnd);
        createSubdisplay(Material.GREEN_CONCRETE, greenStart, greenEnd);
        if (greenEnd < 1) {
            createSubdisplay(Material.RED_CONCRETE, greenEnd, 1);
        }
    }

    private void onButtonPressed(Player player) {
        if (recipe == null) {
            chooseRecipe(player, getItemDisplay().getItemStack());
        } else if (fluidAmount(PylonFluids.BIODIESEL) < dieselPerItem) {
            player.sendMessage(Component.translatable("pylon.gui.lathe.no-fuel"));
        } else if (startTick == -1) {
            start();
        } else {
            playerEnd();
        }
    }

    private void start() {
        removeFluid(PylonFluids.BIODIESEL, dieselPerItem);
        startTick = Bukkit.getCurrentTick();

        applyTransform(getPointer(), fullCycleTime, matrix -> matrix.translateLocal(0, -0.7F, 0));
        Vector3f armTranslation = getFacing().getDirection().toVector3f().mul(-0.3F);
        applyTransform(getMovingThingy(), fullCycleTime, matrix -> matrix.translateLocal(armTranslation));
        applyTransform(getBit(), fullCycleTime, matrix -> matrix.translateLocal(armTranslation));

        Vector3f rotationAxis = getFacing().getDirection().toVector3f().mul(-1).cross(new Vector3f(0, 1, 0));
        Quaternionf rodRotation = new Quaternionf().rotationAxis(new AxisAngle4f((float) (2 * Math.PI / 3), rotationAxis));
        ItemDisplay itemDisplay = getItemDisplay();
        ItemDisplay rod = getHeldEntityOrThrow(ItemDisplay.class, "rod");
        for (int tick = 0; tick < fullCycleTime; tick += 2) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                applyTransform(itemDisplay, 2, matrix -> matrix.rotateLocal(rodRotation));
                applyTransform(rod, 2, matrix -> matrix.rotateLocal(rodRotation));
                new ParticleBuilder(Particle.ITEM)
                        .location(itemDisplay.getLocation())
                        .count(8)
                        .data(itemDisplay.getItemStack())
                        .speed(0.1)
                        .receivers(32, true)
                        .spawn();
            }, tick);
            tasks.add(task);
        }

        BukkitTask endTask = Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
            fail();
            dropByproduct();
            reset();
        }, fullCycleTime);
        tasks.add(endTask);
    }

    private static void applyTransform(ItemDisplay display, int duration, Function<Matrix4f, Matrix4f> transform) {
        Matrix4f oldTransform = TransformUtil.transformationToMatrix(display.getTransformation());
        Matrix4f newTransform = transform.apply(oldTransform);
        display.setTransformationMatrix(newTransform);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(duration);
    }

    private void playerEnd() {
        float pointInCycle = (float) (Bukkit.getCurrentTick() - startTick) / fullCycleTime;
        float completionValue = recipe.completionValue();
        float marginOfError = recipe.marginOfError();

        float difference = Math.abs(pointInCycle - completionValue);
        if (difference < marginOfError) {
            succeed();
        } else if (pointInCycle < completionValue && difference < marginOfError * 3 && ThreadLocalRandom.current().nextBoolean()) {
            // yellow zone
            succeed();
        } else {
            fail();
        }
        dropByproduct();
        reset();
    }

    private void succeed() {
        if (recipe == null) return;
        ItemDisplay display = getItemDisplay();
        display.setItemStack(null);
        Location location = display.getLocation();
        location.getWorld().dropItemNaturally(location, recipe.result());
    }

    private void fail() {
        ItemDisplay display = getItemDisplay();
        display.setItemStack(null);
        Location location = display.getLocation();
        location.getWorld().createExplosion(location, 1, false, false);
    }

    private void dropByproduct() {
        if (recipe == null) return;
        ItemStack byproduct = recipe.byproduct();
        if (byproduct == null) return;
        Location location = getItemDisplay().getLocation();
        location.getWorld().dropItemNaturally(location, byproduct.asQuantity(ThreadLocalRandom.current().nextInt(1, byproduct.getAmount())));
    }

    private void reset() {
        startTick = -1;
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        removeSubdisplays();
        PylonUtils.animate(getMovingThingy(), 5, defaultMovingThingyTransform);
        PylonUtils.animate(getBit(), 5, defaultBitTransform);
        PylonUtils.animate(getPointer(), 5, defaultPointerTransform);
    }

    private ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    private ItemDisplay getPointer() {
        return getHeldEntityOrThrow(ItemDisplay.class, "pointer");
    }

    private ItemDisplay getMovingThingy() {
        return getHeldEntityOrThrow(ItemDisplay.class, "moving_thingy");
    }

    private ItemDisplay getBit() {
        return getHeldEntityOrThrow(ItemDisplay.class, "bit");
    }

    @Override
    public void onUnload(@NotNull RebarBlockUnloadEvent event, @NotNull EventPriority priority) {
        reset();
    }

    private void createSubdisplay(Material material, float start, float end) {
        float invertedCenterpoint = ((1 - start) + (1 - end)) / 2;
        Quaternionf facingQuat = new Quaternionf().lookAlong(getFacing().getDirection().toVector3f().mul(-1F, -1F, 1F), new Vector3f(0, 1, 0));
        addEntity("subdisplay_" + ThreadLocalRandom.current().nextLong(), new ItemDisplayBuilder()
                .itemStack(ItemStackBuilder.of(material)
                        .addCustomModelDataString(getKey() + ":subdisplay_" + material.name().toLowerCase(Locale.ROOT)))
                .transformation(new Matrix4f()
                        .scaleLocal(0.32481545219902447F, 0.7F * (end - start), 0.012414763219462898F)
                        .rotateLocal(facingQuat)
                )
                .build(getBlock().getLocation().toCenterLocation().add(Vector.fromJOML(new Vector3f(0.3375F, (invertedCenterpoint * 0.7F) + (0.15F - 0.35F), 0.40625F).rotate(facingQuat))))
        );
    }

    private void removeSubdisplays() {
        List<String> toRemove = new ArrayList<>();
        for (String name : getHeldEntities().keySet()) {
            if (name.startsWith("subdisplay_")) {
                toRemove.add(name);
            }
        }
        for (String name : toRemove) {
            tryRemoveEntity(name);
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContentsWithName(
                        PylonFluids.BIODIESEL,
                        dieselBuffer,
                        fluidAmount(PylonFluids.BIODIESEL)
                ));
    }

    public static class ButtonEntity extends RebarEntity<ItemDisplay> implements InteractableRebarItemDisplay {

        private static final NamespacedKey LATHE_KEY = pylonKey("lathe");
        private final Lathe lathe;

        public ButtonEntity(@NonNull ItemDisplay entity, @NotNull Lathe lathe) {
            super(PylonKeys.LATHE_BUTTON, entity);
            this.lathe = lathe;
            EntityStorage.add(this);
        }

        @SuppressWarnings("unused")
        public ButtonEntity(@NonNull ItemDisplay entity) {
            super(entity);
            BlockPosition position = entity.getPersistentDataContainer().get(LATHE_KEY, RebarSerializers.BLOCK_POSITION);
            this.lathe = BlockStorage.getAs(Lathe.class, position);
        }

        @Override
        public void write(@NotNull PersistentDataContainer pdc) {
            pdc.set(LATHE_KEY, RebarSerializers.BLOCK_POSITION, new BlockPosition(lathe.getBlock()));
        }

        @Override
        public void onInteract(@NotNull PlayerInteractEvent event, @NotNull Location interactionLocation) {
            if (!event.getAction().isRightClick() || event.getHand() != EquipmentSlot.HAND)
                return;
            lathe.onButtonPressed(event.getPlayer());
        }
    }
}