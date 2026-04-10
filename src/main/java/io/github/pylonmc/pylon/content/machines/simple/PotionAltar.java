package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.content.tools.base.PotionCatalyst;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.base.RebarUnloadBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent;
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
        implements RebarSimpleMultiblock, RebarInteractBlock, RebarTickingBlock, RebarUnloadBlock {

    private static final MultiblockComponent SHIMMER_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.SHIMMER_PEDESTAL);
    private static final MultiblockComponent POTION_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.POTION_PEDESTAL);
    private static final MultiblockComponent LIT_ORANGE_CANDLE_COMPONENT = new VanillaBlockdataMultiblockComponent(Material.ORANGE_CANDLE.createBlockData("[lit=true]"));
    private static final Sound START_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.start", ConfigAdapter.SOUND);
    private static final Sound FINISH_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.finish", ConfigAdapter.SOUND);
    private static final Sound CANCEL_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.cancel", ConfigAdapter.SOUND);
    private static final Sound CANNOT_APPLY_CATALYST_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.cannot_apply_catalyst", ConfigAdapter.SOUND);
    private static final Sound FAILED_APPLY_CATALYST_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.failed_apply_catalyst", ConfigAdapter.SOUND);
    private static final NamespacedKey FUSED_POTION_KEY = new NamespacedKey(Pylon.getInstance(), "fused-potion");

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    private final int maxEffectTypes = getSettings().getOrThrow("max-effect-types", ConfigAdapter.INTEGER);
    private int ticked = 0;
    private PotionAltarRecipe currentRecipe;
    private Integer recipeTicksRemaining;

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

    private boolean isInvalidRecipe(@NotNull Player player, @NotNull ItemStack potion1, @NotNull ItemStack potion2, @Nullable PotionCatalyst catalyst) {
        if (catalyst == null) {
            // 2 potions
            if (isInvalidPotion(potion1) || isInvalidPotion(potion2)) {
                player.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
                return true;
            }

            if (potion1.getType() != potion2.getType()) {
                player.sendMessage(Component.translatable("pylon.message.potion_altar.not-same-type"));
                return true;
            }
        } else {
            if (isInvalidPotion(potion1) && isInvalidPotion(potion2)) {
                // 1 potion + catalyst
                // both potions are invalid
                player.sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
                return true;
            }
        }
        return false;
    }

    @NotNull
    private Color mixColor(PotionContents contents1, PotionContents contents2) {
        Color mixedColor;
        if (contents1 == null || contents2 == null) {
            mixedColor = contents1 != null ? contents1.computeEffectiveColor() : contents2.computeEffectiveColor();
        } else {
            Color color1 = contents1.computeEffectiveColor();
            Color color2 = contents2.computeEffectiveColor();

            int srcR = color1.getRed();
            int srcG = color1.getGreen();
            int srcB = color1.getBlue();
            int dstR = color2.getRed();
            int dstG = color2.getGreen();
            int dstB = color2.getBlue();
            int r = Math.clamp(Math.round((srcR + dstR) / 255.0f), 0, 255);
            int g = Math.clamp(Math.round((srcG + dstG) / 255.0f), 0, 255);
            int b = Math.clamp(Math.round((srcB + dstB) / 255.0f), 0, 255);
            mixedColor = Color.fromRGB(r, g, b);
        }
        return mixedColor;
    }

    private boolean isProcessingRecipe() {
        return currentRecipe != null;
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteract(PlayerInteractEvent event, @NotNull EventPriority priority) {
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
                event.getPlayer().sendMessage(Component.translatable("pylon.message.potion_altar.invalid-catalyst"));
                return;
            }
        }

        // Player could use the altar with:
        // 1 potion + catalyst,
        // 2 potions,
        // 2 potions + catalyst
        // since catalyst is not required.
        if (isInvalidRecipe(event.getPlayer(), potion1, potion2, catalyst)) {
            return;
        }

        PotionContents contents1 = potion1.getData(DataComponentTypes.POTION_CONTENTS);
        PotionContents contents2 = potion2.getData(DataComponentTypes.POTION_CONTENTS);
        if (contents1 == null && contents2 == null) {
            event.getPlayer().sendMessage(Component.translatable("pylon.message.potion_altar.invalid-potion"));
            return;
        }

        // attempt to start recipe
        for (Pedestal pedestal : getAllPedestals()) {
            pedestal.setLocked(true);
        }

        Map<PotionEffectType, PotionEffect> effects = new HashMap<>();
        if (contents1 != null) fuseEffects(effects, contents1.allEffects());
        if (contents2 != null) fuseEffects(effects, contents2.allEffects());
        if (effects.size() > maxEffectTypes) {
            event.getPlayer().sendMessage(Component.translatable("pylon.message.potion_altar.too-many-effects", RebarArgument.of("max_effect_types", maxEffectTypes)));
            return;
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
        fusedPotion.editPersistentDataContainer(pdc -> pdc.set(FUSED_POTION_KEY, PersistentDataType.BOOLEAN, true));


        PotionAltarRecipe recipe = new PotionAltarRecipe(event.getPlayer(), catalyst, fusedPotion, 20 * 20, catalystApplied);
        startRecipe(recipe, recipe.timeTicks());
        getBlock().getWorld().playSound(START_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    private void startRecipe(PotionAltarRecipe recipe, int timeTicks) {
        currentRecipe = recipe;
        recipeTicksRemaining = timeTicks;
    }

    private boolean isInvalidPotion(ItemStack potion) {
        if (!PylonUtils.isPotion(potion.getType())) {
            return !potion.getPersistentDataContainer().has(FUSED_POTION_KEY);
        }

        if (RebarItem.isRebarItem(potion)) {
            return true;
        }

        return !potion.hasData(DataComponentTypes.POTION_CONTENTS);
    }

    private void fuseEffects(Map<PotionEffectType, PotionEffect> origin, List<PotionEffect> effects) {
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

    private List<Pedestal> getAllPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        pedestals.add(getCatalystPedestal());
        return pedestals;
    }

    private List<Pedestal> getPotionPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPotionPedestal1());
        pedestals.add(getPotionPedestal2());
        return pedestals;
    }

    private Pedestal getPotionPedestal1() {
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(-2, 0, 0));
    }

    private Pedestal getPotionPedestal2() {
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(2, 0, 0));
    }

    private Pedestal getCatalystPedestal() {
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(0, 0, -2));
    }

    private List<Block> getCandles() {
        List<Block> candles = new ArrayList<>();
        candles.add(getCandle1());
        candles.add(getCandle2());
        return candles;
    }

    private Block getCandle1() {
        return getBlock().getRelative(-1, 0, 0);
    }

    private Block getCandle2() {
        return getBlock().getRelative(1, 0, 0);
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
            recipe.player().sendMessage(Component.translatable("pylon.message.potion_altar.failed-apply-catalyst"));
            getBlock().getWorld().playSound(FAILED_APPLY_CATALYST_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
            getCatalystPedestal().getItemDisplay().setItemStack(null);
            return;
        }

        if (!recipe.catalystApplied()) {
            recipe.player().sendMessage(Component.translatable("pylon.message.potion_altar.cannot-apply-catalyst"));
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

    @Override
    public void onUnload(@NotNull final RebarBlockUnloadEvent event, @NotNull final EventPriority priority) {
        cancelRecipe();
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
            @NotNull Player player,
            @Nullable PotionCatalyst catalyst,
            @NotNull ItemStack result,
            int timeTicks,
            boolean catalystApplied
    ) {}
}