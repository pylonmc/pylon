package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.pylon.util.HslColor;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarRecipeProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
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
        implements RebarSimpleMultiblock, RebarInteractBlock, RebarTickingBlock, RebarDirectionalBlock {

    private static final NamespacedKey RECIPE_TICKS_REMAINING_KEY = PylonUtils.pylonKey("potion_altar_recipe_ticks_remaining");
    private static final MultiblockComponent SHIMMER_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.SHIMMER_PEDESTAL);
    private static final MultiblockComponent POTION_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.POTION_PEDESTAL);
    private static final MultiblockComponent LIT_ORANGE_CANDLE_COMPONENT = new VanillaBlockdataMultiblockComponent(Material.ORANGE_CANDLE.createBlockData("[lit=true]"));
    private final Sound START_SOUND = getSettings().getOrThrow("sound.start", ConfigAdapter.SOUND);
    private final Sound FINISH_SOUND = getSettings().getOrThrow("sound.finish", ConfigAdapter.SOUND);
    private final Sound CANCEL_SOUND = getSettings().getOrThrow("sound.cancel", ConfigAdapter.SOUND);
    private final Sound PROCESSING_SOUND = getSettings().getOrThrow("sound.processing", ConfigAdapter.SOUND);
    private final Sound CANNOT_APPLY_CATALYST_SOUND = getSettings().getOrThrow("sound.cannot_apply_catalyst", ConfigAdapter.SOUND);
    private final Sound FAILED_APPLY_CATALYST_SOUND = getSettings().getOrThrow("sound.failed_apply_catalyst", ConfigAdapter.SOUND);

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final int recipeTimeTicks = getSettings().getOrThrow("recipe-time-ticks", ConfigAdapter.INTEGER);
    private final int maxEffectTypes = getSettings().getOrThrow("max-effect-types", ConfigAdapter.INTEGER);
    private int ticked = 0;
    private @Nullable Player interactor;
    private @Nullable AltarProgress altarProgress;

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
        setFacing(context.getFacing());
        setMultiblockDirection(getFacing());

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
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                altarProgress = tryStartProgress(null);
                if (altarProgress != null) {
                    altarProgress.ticksRemaining = lastRecipeTicks;
                }
                pdc.remove(RECIPE_TICKS_REMAINING_KEY);
            }, 20L);
        }
    }

    @Override
    public @NotNull Map<Vector3i, MultiblockComponent> getComponents() {
        Map<Vector3i, MultiblockComponent> map = new LinkedHashMap<>();
        map.put(new Vector3i(1, 0, 0), LIT_ORANGE_CANDLE_COMPONENT);
        map.put(new Vector3i(2, 0, 0), POTION_PEDESTAL_COMPONENT);
        map.put(new Vector3i(-1, 0, 0), LIT_ORANGE_CANDLE_COMPONENT);
        map.put(new Vector3i(-2, 0, 0), POTION_PEDESTAL_COMPONENT);
        map.put(new Vector3i(0, 0, 2), SHIMMER_PEDESTAL_COMPONENT);
        return map;
    }

    private boolean isInvalidRecipe(@NotNull ItemStack potion1, @NotNull ItemStack potion2, @Nullable PotionCatalyst catalyst, @Nullable Player interactor) {
        if (catalyst == null) {
            // 2 potions
            if (isInvalidPotion(potion1) || isInvalidPotion(potion2)) {
                sendMessage("invalid-potion");
                return true;
            }

            if (potion1.getType() != potion2.getType()) {
                sendMessage("not-same-type");
                return true;
            }
        } else {
            if (isInvalidPotion(potion1) && isInvalidPotion(potion2)) {
                // 1 potion + catalyst
                // both potions are invalid
                sendMessage("invalid-potion");
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
                    (color1.hue() + color2.hue()) / 2,
                    (color1.saturation() + color2.saturation()) / 2,
                    (color1.lightness() + color2.lightness()) / 2
            ).toRgb();
        }
        return mixedColor;
    }

    private boolean isProcessingRecipe() {
        return altarProgress != null;
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
        interactor = event.getPlayer();
        AltarProgress recipe = tryStartProgress(event.getPlayer());
        if (recipe == null) {
            return;
        }
        altarProgress = recipe;
        playSound(START_SOUND);
    }

    private @Nullable AltarProgress tryStartProgress(@Nullable Player interactor) {
        if (getPotionPedestal1() == null || getPotionPedestal2() == null || getCatalystPedestal() == null) {
            return null;
        }

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
                sendMessage("invalid-catalyst");
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
            sendMessage("invalid-potion");
            return null;
        }

        // attempt to start recipe
        for (Pedestal pedestal : getAllPedestals()) {
            if (pedestal != null) {
                pedestal.setLocked(true);
            }
        }

        Map<PotionEffectType, PotionEffect> effects = new HashMap<>();
        if (contents1 != null) fuseEffects(effects, contents1.allEffects());
        if (contents2 != null) fuseEffects(effects, contents2.allEffects());
        if (effects.size() > maxEffectTypes) {
            sendMessage("too-many-effects", RebarArgument.of("max_effect_types", maxEffectTypes));
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

        return new AltarProgress(catalyst, fusedPotion, recipeTimeTicks, catalystApplied);
    }

    private boolean isInvalidPotion(@NotNull ItemStack potion) {
        return RebarItem.isRebarItem(potion)
                || !potion.hasData(DataComponentTypes.POTION_CONTENTS)
                || potion.getData(DataComponentTypes.POTION_CONTENTS).allEffects().isEmpty();
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
        if (altarProgress != null) {
            altarProgress.ticksRemaining -= ticks;
            if (altarProgress.ticksRemaining <= 0) {
                onRecipeFinished(altarProgress);
                altarProgress = null;
            }
        }
    }

    @Override
    public void tick() {
        progressRecipe(tickInterval);

        if (isProcessingRecipe()) {
            if (!isFormedAndFullyLoaded()) {
                cancelRecipe();
                return;
            }

            // flames on the candle
            for (Block candle : getCandles()) {
                new ParticleBuilder(Particle.FLAME)
                        .count(10)
                        .extra(0.02)
                        .location(candle.getLocation().toCenterLocation())
                        .spawn();
            }

            new ParticleBuilder(Particle.WAX_OFF)
                    .location(getBlock().getLocation().toCenterLocation().add(2*Math.sin(30*Math.toRadians(ticked)), 0.25+0.25*Math.sin(30*Math.toRadians(ticked)), 2*Math.cos(30*Math.toRadians(ticked))))
                    .count(50)
                    .extra(0.5)
                    .spawn();

            new ParticleBuilder(Particle.WAX_ON)
                    .location(getBlock().getLocation().toCenterLocation().add(2*Math.sin(30*Math.toRadians(ticked)+Math.PI), 0.25+0.25*Math.sin(30*Math.toRadians(ticked)), 2*Math.cos(30*Math.toRadians(ticked)+Math.PI)))
                    .count(50)
                    .extra(0.5)
                    .spawn();

            // sound
            if (ticked % 2 == 0) {
                playSound(PROCESSING_SOUND);
            }
        }

        ticked += 1;

        int speed = isProcessingRecipe() ? 40 : 10;
        new ParticleBuilder(Particle.DRAGON_BREATH)
                .count(10)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation().add(Math.sin(speed*Math.toRadians(ticked)), 0, Math.cos(speed*Math.toRadians(ticked))))
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
    private List<@Nullable Pedestal> getAllPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        pedestals.add(getCatalystPedestal());
        return pedestals;
    }

    @NotNull
    private List<@Nullable Pedestal> getPotionPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        return pedestals;
    }

    @Nullable
    private Pedestal getPotionPedestal1() {
        Vector3i offset = getBlockOffsets(POTION_PEDESTAL_COMPONENT).get(0);
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    @Nullable
    private Pedestal getPotionPedestal2() {
        Vector3i offset = getBlockOffsets(POTION_PEDESTAL_COMPONENT).get(1);
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    @Nullable
    private Pedestal getCatalystPedestal() {
        Vector3i offset = getBlockOffsets(SHIMMER_PEDESTAL_COMPONENT).getFirst();
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(offset.x(), offset.y(), offset.z()));
    }

    @NotNull
    private List<Vector3i> getBlockOffsets(@NotNull MultiblockComponent component) {
        return validStructures().getFirst().entrySet().stream().filter(entry -> entry.getValue() == component).map(Map.Entry::getKey).toList();
    }

    @NotNull
    private List<Block> getCandles() {
        List<Block> candles = new ArrayList<>();
        for (Vector3i offset : getBlockOffsets(LIT_ORANGE_CANDLE_COMPONENT)) {
            candles.add(getBlock().getRelative(offset.x(), offset.y(), offset.z()));
        }
        return candles;
    }

    private void onRecipeFinished(@NotNull final AltarProgress recipe) {
        for (Pedestal pedestal : getPotionPedestals()) {
            if (pedestal != null) {
                pedestal.getItemDisplay().setItemStack(null);
            }
        }
        for (Pedestal pedestal : getAllPedestals()) {
            if (pedestal != null) {
                pedestal.setLocked(false);
            }
        }

        Location location = getBlock().getLocation().toCenterLocation();
        getBlock().getWorld().strikeLightningEffect(location);
        getBlock().getWorld().dropItemNaturally(location, recipe.result);

        new ParticleBuilder(Particle.VIBRATION)
                .count(40)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .data(new Vibration(new BlockDestination(getBlock().getLocation().toCenterLocation().add(0, 3, 0)), 10))
                .spawn();

        if (recipe.catalyst == null || getCatalystPedestal() == null) {
            playSound(FINISH_SOUND);
            return;
        }

        // catalysts
        if (ThreadLocalRandom.current().nextDouble() >= recipe.catalyst.getSettings().get("apply-success-rate", ConfigAdapter.DOUBLE, 0.0D)) {
            sendMessage("failed-apply-catalyst");
            playSound(FAILED_APPLY_CATALYST_SOUND);
            getCatalystPedestal().getItemDisplay().setItemStack(null);
            return;
        }

        if (!recipe.catalystApplied) {
            sendMessage("cannot-apply-catalyst");
            playSound(CANNOT_APPLY_CATALYST_SOUND);
            return;
        }

        // succeed applying catalyst
        getCatalystPedestal().getItemDisplay().setItemStack(null);
        playSound(FINISH_SOUND);
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

        playSound(CANCEL_SOUND);
    }

    private void playSound(@NotNull Sound sound) {
        getBlock().getWorld().playSound(sound, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    private void sendMessage(@NotNull String key, RebarArgument... arguments) {
        if (interactor != null) {
            interactor.sendMessage(Component.translatable("pylon.message.potion_altar." + key, arguments));
        }
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
            RebarArgument.of(
                    "processing",
                    altarProgress == null
                    ? Component.translatable("pylon.waila.potion_altar.idle")
                    : Component.translatable("pylon.waila.potion_altar.processing")
                    .arguments(
                        RebarArgument.of(
                            "bars", PylonUtils.createProgressBar(
                                        altarProgress.timeTicks - altarProgress.ticksRemaining,
                                        altarProgress.timeTicks,
                                        20,
                                        TextColor.color(100, 255, 100)
                            )
                        )
                    )
            )
        ));
    }

    /**
     * Represents a variable recipe, for internal use only
     *
     * @author balugaq
     */
    @Getter
    private static class AltarProgress {
        private final @Nullable PotionCatalyst catalyst;
        private final @NotNull ItemStack result;
        private final int timeTicks;
        private final boolean catalystApplied;
        private int ticksRemaining;

        /**
         * Creates a new altar progress
         *
         * @param result
         *         the output (respects amount)
         */
        protected AltarProgress(@Nullable PotionCatalyst catalyst, @NotNull ItemStack result, int timeTicks, boolean catalystApplied) {
            this.catalyst = catalyst;
            this.result = result;
            this.timeTicks = timeTicks;
            this.catalystApplied = catalystApplied;
            this.ticksRemaining = timeTicks;
        }
    }

    /**
     * {@link RebarRecipeProcessor} requires a unique recipe key to recover recipe progress after restarting server,
     * while this altar doesn't have any static recipe to be loaded or be recovered, which produces tons of
     * error logs for "Couldn't find recipe". So we have to recover recipe progress manually.
     *
     * @see AltarProgress
     */
    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        if (altarProgress != null) {
            pdc.set(RECIPE_TICKS_REMAINING_KEY, RebarSerializers.INTEGER, altarProgress.ticksRemaining);
        }
    }
}