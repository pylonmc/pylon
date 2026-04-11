package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.pylon.util.HslColor;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarRecipeProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.RebarBlockSerializeEvent;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author balugaq
 */
public class PotionAltar extends RebarBlock
        implements RebarSimpleMultiblock, RebarInteractBlock, RebarTickingBlock {

    private static final NamespacedKey RECIPE_TICKS_REMAINING_KEY = PylonUtils.pylonKey("potion_altar_recipe_ticks_remaining");
    private static final MultiblockComponent SHIMMER_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.SHIMMER_PEDESTAL);
    private static final MultiblockComponent POTION_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.POTION_PEDESTAL);
    private static final MultiblockComponent LIT_ORANGE_CANDLE_COMPONENT = new VanillaBlockdataMultiblockComponent(Material.ORANGE_CANDLE.createBlockData("[lit=true]"));
    private static final Sound START_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.start", ConfigAdapter.SOUND);
    private static final Sound FINISH_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.finish", ConfigAdapter.SOUND);
    private static final Sound CANCEL_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.cancel", ConfigAdapter.SOUND);
    private static final Sound CANNOT_APPLY_CATALYST_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.cannot_apply_catalyst", ConfigAdapter.SOUND);
    private static final Sound FAILED_APPLY_CATALYST_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.failed_apply_catalyst", ConfigAdapter.SOUND);

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final int recipeTimeTicks = getSettings().getOrThrow("recipe-time-ticks", ConfigAdapter.INTEGER);
    private final int maxEffectTypes = getSettings().getOrThrow("max-effect-types", ConfigAdapter.INTEGER);
    private int ticked = 0;
    private @Nullable Player interactor;
    private @Nullable PotionAltarRecipe currentRecipe;
    private @Nullable Integer recipeTicksRemaining;

    /**
     * @author balugaq
     */
    public static class Item extends RebarItem {
        private final int maxEffectTypes = getSettings().getOrThrow("max-effect-types", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("max-effect-types", maxEffectTypes)
            );
        }
    }

    @SuppressWarnings("unused")
    public PotionAltar(Block block, BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);

        addEntity("brewing_stand", new BlockDisplayBuilder()
                .transformation(new TransformBuilder()
                                        .translate(0, 0.5, 0)
                                        .scale(0.9)
                                        .buildForBlockDisplay()
                )
                .blockData(Material.BREWING_STAND.createBlockData())
                .build(getBlock().getLocation().toCenterLocation())
        );
    }

    @SuppressWarnings("unused")
    public PotionAltar(Block block, PersistentDataContainer pdc) {
        super(block, pdc);
        // recover the recipe
        int lastRecipeTicks = pdc.getOrDefault(RECIPE_TICKS_REMAINING_KEY, PersistentDataType.INTEGER, -1);
        if (lastRecipeTicks > 0) {
            currentRecipe = findRecipe(null);
            if (currentRecipe != null) {
                recipeTicksRemaining = lastRecipeTicks;
            }
            pdc.remove(RECIPE_TICKS_REMAINING_KEY);
        }
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> map = new LinkedHashMap<>();
        map.put(new Vector3i(1, 0, 0), LIT_ORANGE_CANDLE_COMPONENT);
        map.put(new Vector3i(2, 0, 0), POTION_PEDESTAL_COMPONENT);
        map.put(new Vector3i(-1, 0, 0), LIT_ORANGE_CANDLE_COMPONENT);
        map.put(new Vector3i(-2, 0, 0), POTION_PEDESTAL_COMPONENT);
        map.put(new Vector3i(0, 0, -2), SHIMMER_PEDESTAL_COMPONENT);
        return map;
    }

    private boolean isInvalidRecipe(@NotNull ItemStack potion1, @NotNull ItemStack potion2, @Nullable PotionCatalyst catalyst, @Nullable Player interactor) {
        if (catalyst == null) {
            // 2 potions
            if (isInvalidPotion(potion1) || isInvalidPotion(potion2)) {
                if (interactor != null) {
                    interactor.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
                }
                return true;
            }

            if (potion1.getType() != potion2.getType()) {
                if (interactor != null) {
                    interactor.sendMessage(Component.translatable("pylon.message.potion_altar.not-same-type"));
                }
                return true;
            }
        } else {
            if (isInvalidPotion(potion1) && isInvalidPotion(potion2)) {
                // 1 potion + catalyst
                // both potions are invalid
                if (interactor != null) {
                    interactor.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
                }
                return true;
            }
        }
        return false;
    }

    @NotNull
    private Color mixColor(@Nullable PotionContents contents1, @Nullable PotionContents contents2) {
        Color mixedColor;
        if (contents1 == null || contents2 == null) {
            mixedColor = contents1 != null ? contents1.computeEffectiveColor() : contents2.computeEffectiveColor();
        } else {

            HslColor color1 = HslColor.fromRgb(contents1.computeEffectiveColor());
            HslColor color2 = HslColor.fromRgb(contents2.computeEffectiveColor());

            mixedColor = new HslColor(
                    color1.hue(),
                    color1.saturation(),
                    color1.lightness() + color2.lightness() / 2
            ).toRgb();
        }
        return mixedColor;
    }

    private boolean isProcessingRecipe() {
        return currentRecipe != null;
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == Event.Result.DENY
        ) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY);
            return;
        }

        if (!isFormedAndFullyLoaded() || isProcessingRecipe()) {
            return;
        }

        // start recipe
        PotionAltarRecipe recipe = findRecipe(event.getPlayer());
        if (recipe == null) {
            return;
        }
        interactor = event.getPlayer();
        currentRecipe = recipe;
        recipeTicksRemaining = recipe.timeTicks();
        getBlock().getWorld().playSound(START_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    private @Nullable PotionAltarRecipe findRecipe(@Nullable Player interactor) {
        ItemStack potion1 = getPotionPedestal1().getItemDisplay().getItemStack();
        ItemStack potion2 = getPotionPedestal2().getItemDisplay().getItemStack();
        ItemStack catalystItem = getCatalystPedestal().getItemDisplay().getItemStack();
        RebarItem rebar = RebarItem.fromStack(catalystItem);
        @Nullable PotionCatalyst catalyst = null;
        if (!catalystItem.getType().isAir()) {
            if (rebar instanceof PotionCatalyst clyst) {
                catalyst = clyst;
            } else {
                // invalid catalyst
                if (interactor != null) {
                    interactor.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-catalyst"));
                }
                return null;
            }
        }

        // Player could use the altar with:
        // 1 potion + catalyst,
        // 2 potions,
        // 2 potions + catalyst
        // since catalyst is not required.
        if (isInvalidRecipe(potion1, potion2, catalyst, interactor)) {
            return null;
        }

        PotionContents contents1 = potion1.getData(DataComponentTypes.POTION_CONTENTS);
        PotionContents contents2 = potion2.getData(DataComponentTypes.POTION_CONTENTS);
        if (contents1 == null && contents2 == null) {
            if (interactor != null) {
                interactor.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
            }
            return null;
        }

        // attempt to start recipe
        for (Pedestal pedestal : getAllPedestals()) {
            pedestal.setLocked(true);
        }

        Map<PotionEffectType, PotionEffect> effects = new HashMap<>();
        if (contents1 != null) fuseEffects(effects, contents1.allEffects());
        if (contents2 != null) fuseEffects(effects, contents2.allEffects());
        if (effects.size() > maxEffectTypes) {
            if (interactor != null) {
                interactor.sendMessage(Component.translatable("pylon.message.potion_altar.too-many-effects", RebarArgument.of("max_effect_types", maxEffectTypes)));
            }
            return null;
        }
        boolean catalystApplied = true;
        if (catalyst != null) {
            if (!catalyst.apply(effects)) {
                catalystApplied = false;
            }
        }

        Color mixedColor = mixColor(contents1, contents2);

        PotionContents contents = PotionContents.potionContents().addCustomEffects(effects.values().stream().toList()).customColor(mixedColor).build();
        ItemStack fusedPotion = potion1.clone();
        fusedPotion.setData(DataComponentTypes.ITEM_NAME, Component.translatable("pylon.message.potion_altar.fused-potion-name"));
        fusedPotion.setData(DataComponentTypes.POTION_CONTENTS, contents);

        return new PotionAltarRecipe(catalyst, fusedPotion, recipeTimeTicks, catalystApplied);
    }

    private boolean isInvalidPotion(@NotNull ItemStack potion) {
        if (RebarItem.isRebarItem(potion)) {
            return true;
        }

        return !potion.hasData(DataComponentTypes.POTION_CONTENTS);
    }

    private void fuseEffects(@NotNull Map<PotionEffectType, PotionEffect> origin, @NotNull List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            if (origin.containsKey(effect.getType())) {
                origin.compute(effect.getType(), (k, originEffect) -> new PotionEffect(
                   effect.getType(),
                   originEffect.getDuration() + effect.getDuration(),
                   Math.max(originEffect.getAmplifier(), effect.getAmplifier()),
                   originEffect.isAmbient() || effect.isAmbient(),
                   originEffect.hasParticles() || effect.hasParticles(),
                   originEffect.hasIcon() || effect.hasIcon()
               ));
            } else {
                origin.put(effect.getType(), effect);
            }
        }
    }

    private void progressRecipe(int ticks) {
        if (currentRecipe != null && recipeTicksRemaining != null) {
            recipeTicksRemaining -= ticks;
            if (recipeTicksRemaining <= 0) {
                onRecipeFinished(currentRecipe);
                currentRecipe = null;
                recipeTicksRemaining = null;
            }
        }
    }

    @Override
    public void tick() {
        progressRecipe(tickInterval);

        if (isProcessingRecipe() && !isFormedAndFullyLoaded()) {
            cancelRecipe();
            return;
        }

        ticked += 1;

        new ParticleBuilder(Particle.DRAGON_BREATH)
                .count(10)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation().add(Math.sin(10*Math.toRadians(ticked)), 0, Math.cos(10*Math.toRadians(ticked))))
                .data(1f)
                .spawn();

        new ParticleBuilder(Particle.DUST)
                .count(20)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .data(new Particle.DustOptions(Color.fromRGB(0x0055AAAA), 1))
                .spawn();
    }

    @NotNull
    private List<Pedestal> getAllPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        pedestals.add(getCatalystPedestal());
        return pedestals;
    }

    @NotNull
    private List<Pedestal> getPotionPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        return pedestals;
    }

    private Pedestal getPotionPedestal1() {
        Vector3i offset = getBlockOffsets(POTION_PEDESTAL_COMPONENT).get(0);
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    private Pedestal getPotionPedestal2() {
        Vector3i offset = getBlockOffsets(POTION_PEDESTAL_COMPONENT).get(1);
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    private Pedestal getCatalystPedestal() {
        Vector3i offset = getBlockOffsets(SHIMMER_PEDESTAL_COMPONENT).getFirst();
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    @NotNull
    private List<Vector3i> getBlockOffsets(@NotNull MultiblockComponent component) {
        return validStructures().getFirst().entrySet().stream().filter(entry -> entry.getValue() == component).map(Map.Entry::getKey).toList();
    }

    private List<Block> getCandles() {
        List<Block> candles = new ArrayList<>();
        for (Vector3i offset : getBlockOffsets(LIT_ORANGE_CANDLE_COMPONENT)) {
            candles.add(getBlock().getRelative(offset.x(), offset.y(), offset.z()));
        }
        return candles;
    }

    private void onRecipeFinished(@NotNull final PotionAltarRecipe recipe) {
        for (Pedestal pedestal : getPotionPedestals()) {
            pedestal.getItemDisplay().setItemStack(null);
        }
        for (Pedestal pedestal : getAllPedestals()) {
            pedestal.setLocked(false);
        }

        Location location = getBlock().getLocation().toCenterLocation();
        getBlock().getWorld().strikeLightningEffect(location);
        getBlock().getWorld().dropItemNaturally(location, recipe.result());

        new ParticleBuilder(Particle.DRAGON_BREATH)
                .count(40)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .data(1f)
                .spawn();

        if (recipe.catalyst() == null) {
            getBlock().getWorld().playSound(FINISH_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
            return;
        }

        if (ThreadLocalRandom.current().nextDouble() >= recipe.catalyst().getSettings().get("apply-success-rate", ConfigAdapter.DOUBLE, 0.0D)) {
            if (interactor != null) {
                interactor.sendMessage(Component.translatable("pylon.message.potion_altar.failed-apply-catalyst"));
            }
            getBlock().getWorld().playSound(FAILED_APPLY_CATALYST_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
            getCatalystPedestal().getItemDisplay().setItemStack(null);
            return;
        }

        if (!recipe.catalystApplied()) {
            if (interactor != null) {
                interactor.sendMessage(Component.translatable("pylon.message.potion_altar.cannot-apply-catalyst"));
            }
            getBlock().getWorld().playSound(CANNOT_APPLY_CATALYST_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
            return;
        }

        // succeed applying catalyst
        getCatalystPedestal().getItemDisplay().setItemStack(null);
        getBlock().getWorld().playSound(FINISH_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    private void cancelRecipe() {
        for (Pedestal pedestal : getAllPedestals()) {
            if (pedestal != null) {
                pedestal.setLocked(false);
            }
        }

        for (Block candle : getCandles()) {
            new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                    .count(20)
                    .extra(0.05)
                    .location(candle.getLocation().toCenterLocation())
                    .spawn();
        }

        getBlock().getWorld().playSound(CANCEL_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
            RebarArgument.of(
                "processing",
                currentRecipe == null
                    ? Component.translatable("pylon.waila.potion_altar.idle")
                    : Component.translatable("pylon.waila.potion_altar.processing")
                    .arguments(
                        RebarArgument.of(
                            "bars", PylonUtils.createProgressBar(
                                currentRecipe.timeTicks() - recipeTicksRemaining,
                                currentRecipe.timeTicks(),
                                20,
                                TextColor.color(100, 255, 100)
                            )
                        )
                    )
            )
        ));
    }

    /**
     * For internal use only
     *
     * @param result
     *         the output (respects amount)
     *
     * @author balugaq
     */
    private record PotionAltarRecipe(
            @Nullable PotionCatalyst catalyst,
            @NotNull ItemStack result,
            int timeTicks,
            boolean catalystApplied
    ) {}

    /**
     * {@link RebarRecipeProcessor} requires a unique recipe key to recover recipe after restarting server,
     * while this altar doesn't have any static recipe to be loaded or be recovered, which produces tons of
     * error logs for "Couldn't find recipe". So we have to recover recipe manually.
     *
     * @author balugaq
     */
    public static class ProgressRecoveringListener implements Listener {
        @EventHandler
        private void onSerialize(RebarBlockSerializeEvent event) {
            RebarBlock block = event.getRebarBlock();
            if (block instanceof PotionAltar altar) {
                if (altar.recipeTicksRemaining != null) {
                    // save progress
                    event.getPdc().set(RECIPE_TICKS_REMAINING_KEY, RebarSerializers.INTEGER, altar.recipeTicksRemaining);
                    altar.cancelRecipe();
                }
            }
        }
    }
}