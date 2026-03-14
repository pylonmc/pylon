package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.recipes.PotionAltarRecipe;
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
import io.github.pylonmc.rebar.entity.display.BlockDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.recipe.RecipeInput;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author balugaq
 */
public class PotionAltar extends RebarBlock
        implements RebarSimpleMultiblock, RebarInteractBlock, RebarTickingBlock, RebarRecipeProcessor<PotionAltarRecipe> {

    private static final MultiblockComponent POTION_PEDESTAL_COMPONENT = new RebarMultiblockComponent(PylonKeys.POTION_PEDESTAL);
    private static final MultiblockComponent LIT_ORANGE_CANDLE_COMPONENT = new VanillaBlockdataMultiblockComponent(Material.ORANGE_CANDLE.createBlockData("[lit=true]"));
    private static final Sound START_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.start", ConfigAdapter.SOUND);
    private static final Sound FINISH_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.finish", ConfigAdapter.SOUND);
    private static final Sound CANCEL_SOUND = Settings.get(PylonKeys.POTION_ALTAR).getOrThrow("sound.cancel", ConfigAdapter.SOUND);

    private final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

    @SuppressWarnings("unused")
    public PotionAltar(Block block, BlockCreateContext context) {
        super(block, context);

        setTickInterval(tickInterval);
        setRecipeType(PotionAltarRecipe.RECIPE_TYPE);

        addEntity("brewing_stand", new BlockDisplayBuilder()
                .transformation(new TransformBuilder()
                                        .translate(0, 0.5, 0)
                                        .scale(0.5)
                                        .buildForItemDisplay()
                )
                .blockData(Material.BREWING_STAND.createBlockData())
                .build(getBlock().getLocation().toCenterLocation())
        );
        addEntity("item", new ItemDisplayBuilder()
                .transformation(new TransformBuilder()
                        .translate(0, 1, 0)
                        .scale(0.5)
                        .buildForItemDisplay()
                )
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
        return map;
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

        // attempt to start recipe
        ItemStack potion1 = getPedestal1().getItemDisplay().getItemStack();
        ItemStack potion2 = getPedestal2().getItemDisplay().getItemStack();
        if (isInvalidPotion(potion1) || isInvalidPotion(potion2)) {
            event.getPlayer().sendMessage(Component.translatable("rebar.message.command.key.hover.invalid-potion"));
            return;
        }

        if (potion1.getType() != potion2.getType()) {
            event.getPlayer().sendMessage(Component.translatable("rebar.message.command.key.hover.not-same-type"));
            return;
        }

        getPedestal1().setLocked(true);
        getPedestal2().setLocked(true);

        var recipe = new PotionAltarRecipe(RecipeInput.of(potion1), RecipeInput.of(potion2), fusePotion(potion1, potion2), 20 * 20);
        startRecipe(recipe, recipe.timeTicks());
        getBlock().getWorld().playSound(START_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    public boolean isInvalidPotion(ItemStack potion) {
        if (!PylonUtils.isPotion(potion.getType())) {
            return false;
        }

        return potion.hasData(DataComponentTypes.POTION_CONTENTS);
    }

    public ItemStack fusePotion(ItemStack potion1, ItemStack potion2) {
        PotionContents contents1 = potion1.getData(DataComponentTypes.POTION_CONTENTS);
        PotionContents contents2 = potion2.getData(DataComponentTypes.POTION_CONTENTS);
        Preconditions.checkNotNull(contents1);
        Preconditions.checkNotNull(contents2);

        List<PotionEffect> effects = new ArrayList<>();
        effects.addAll(contents1.allEffects());
        effects.addAll(contents2.allEffects());

        Color color1 = contents1.computeEffectiveColor();
        Color color2 = contents1.computeEffectiveColor();
        int srcA = color1.getAlpha();
        int dstA = color2.getAlpha();

        Color mixedColor;
        float computedA = 1 - (1 - srcA) * (1 - dstA);
        if (computedA <= 0) {
            mixedColor = Color.fromARGB(0);
        } else {
            int srcR = color1.getRed();
            int srcG = color1.getGreen();
            int srcB = color1.getBlue();
            int dstR = color2.getRed();
            int dstG = color2.getGreen();
            int dstB = color2.getBlue();
            int a = Math.clamp(Math.round(computedA * 255), 0, 255);
            int r = Math.clamp(Math.round((srcR * srcA + dstR * dstA * (1 - srcA)) / computedA * 255), 0, 255);
            int g = Math.clamp(Math.round((srcG * srcA + dstG * dstA * (1 - srcA)) / computedA * 255), 0, 255);
            int b = Math.clamp(Math.round((srcB * srcA + dstB * dstA * (1 - srcA)) / computedA * 255), 0, 255);
            mixedColor = Color.fromARGB(a, r, g, b);
        }

        PotionContents contents = PotionContents.potionContents().addCustomEffects(effects).customColor(mixedColor).build();
        ItemStack result = new ItemStack(potion1.getType());
        result.setData(DataComponentTypes.POTION_CONTENTS, contents);
        return result;
    }

    @Override
    public void tick() {
        progressRecipe(tickInterval);

        if (isProcessingRecipe() && !isFormedAndFullyLoaded()) {
            cancelRecipe();
            return;
        }

        new ParticleBuilder(Particle.DRAGON_BREATH)
                .count(5)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .spawn();

        new ParticleBuilder(Particle.DUST)
                .count(20)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .data(new Particle.DustOptions(Color.fromRGB(0x0055AAAA), 1))
                .spawn();
    }

    public List<Pedestal> getPedestals() {
        List<Pedestal> pedestals = new ArrayList<>();
        pedestals.add(getPedestal1());
        pedestals.add(getPedestal2());
        return pedestals;
    }

    public Pedestal getPedestal1() {
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(-1, 0, 0));
    }

    public Pedestal getPedestal2() {
        return BlockStorage.getAs(Pedestal.class, getBlock().getRelative(1, 0, 0));
    }

    public ItemDisplay getItemDisplay() {
        return getHeldEntityOrThrow(ItemDisplay.class, "item");
    }

    public void onRecipeFinished(@NotNull final PotionAltarRecipe recipe) {
        for (Pedestal pedestal : getPedestals()) {
            pedestal.getItemDisplay().setItemStack(null);
            pedestal.setLocked(false);
        }
        ItemStack result = recipe.result();

        getItemDisplay().setItemStack(result);

        new ParticleBuilder(Particle.DRAGON_BREATH)
                .count(20)
                .extra(0.02)
                .location(getBlock().getLocation().toCenterLocation())
                .spawn();
        getBlock().getWorld().playSound(FINISH_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    public void cancelRecipe() {
        for (Pedestal pedestal : getPedestals()) {
            if (pedestal != null) {
                pedestal.setLocked(false);
            }
        }

        new ParticleBuilder(Particle.WHITE_SMOKE)
                .count(20)
                .extra(0.05)
                .location(getBlock().getLocation().toCenterLocation())
                .spawn();

        getBlock().getWorld().playSound(CANCEL_SOUND, getBlock().getX() + 0.5, getBlock().getY() + 0.5, getBlock().getZ() + 0.5);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
            RebarArgument.of(
                "processing",
                getCurrentRecipe() == null
                    ? Component.translatable("pylon.waila.potion_altar.idle")
                    : Component.translatable("pylon.waila.potion_altar.processing")
                    .arguments(
                        RebarArgument.of(
                            "bars", PylonUtils.createProgressBar(
                                getCurrentRecipe().timeTicks() - getRecipeTicksRemaining(),
                                getCurrentRecipe().timeTicks(),
                                20,
                                TextColor.color(100, 255, 100)
                            )
                        )
                    )
            )
        ));
    }
}
