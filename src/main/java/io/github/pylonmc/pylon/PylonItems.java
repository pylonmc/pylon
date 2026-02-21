package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.armor.BronzeArmor;
import io.github.pylonmc.pylon.content.assembling.RedstoneSolderingIron;
import io.github.pylonmc.pylon.content.assembling.Screwdriver;
import io.github.pylonmc.pylon.content.building.Elevator;
import io.github.pylonmc.pylon.content.building.ExplosiveTarget;
import io.github.pylonmc.pylon.content.building.Immobilizer;
import io.github.pylonmc.pylon.content.combat.BeheadingSword;
import io.github.pylonmc.pylon.content.combat.IceArrow;
import io.github.pylonmc.pylon.content.combat.ReactivatedWitherSkull;
import io.github.pylonmc.pylon.content.combat.RecoilArrow;
import io.github.pylonmc.pylon.content.machines.cargo.*;
import io.github.pylonmc.pylon.content.machines.diesel.machines.*;
import io.github.pylonmc.pylon.content.machines.diesel.production.Biorefinery;
import io.github.pylonmc.pylon.content.machines.diesel.production.Fermenter;
import io.github.pylonmc.pylon.content.machines.fluid.*;
import io.github.pylonmc.pylon.content.machines.hydraulics.*;
import io.github.pylonmc.pylon.content.machines.simple.*;
import io.github.pylonmc.pylon.content.machines.smelting.DieselSmelteryHeater;
import io.github.pylonmc.pylon.content.machines.smelting.PitKiln;
import io.github.pylonmc.pylon.content.resources.IronBloom;
import io.github.pylonmc.pylon.content.science.Loupe;
import io.github.pylonmc.pylon.content.science.ResearchPack;
import io.github.pylonmc.pylon.content.talismans.*;
import io.github.pylonmc.pylon.content.tools.*;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.fluid.FluidPipe;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.recipe.RecipeType;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.potion.PotionType;

import java.util.Objects;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

@SuppressWarnings({"UnstableApiUsage", "OverlyComplexClass"})
public final class PylonItems {

    private PylonItems() {
        throw new AssertionError("Utility class");
    }

    //<editor-fold desc="Research" defaultstate=collapsed>

    public static final ItemStack LOUPE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LOUPE)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLASS_PANE.getKey())
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .animation(ItemUseAnimation.SPYGLASS)
                    .hasConsumeParticles(false)
                    .consumeSeconds(Settings.get(PylonKeys.LOUPE).getOrThrow("use-ticks", ConfigAdapter.INTEGER) / 20.0F)
                    .sound(SoundEventKeys.INTENTIONALLY_EMPTY)
            )
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(
                            Settings.get(PylonKeys.LOUPE).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F)
                    .cooldownGroup(PylonKeys.LOUPE)
            )
            .build();
    static {
        RebarItem.register(Loupe.class, LOUPE);
        PylonPages.SCIENCE.addItem(LOUPE);
    }

    public static final ItemStack RESEARCH_PACK_1 = ItemStackBuilder.rebar(Material.RED_BANNER, PylonKeys.RESEARCH_PACK_1)
            .useCooldown(Settings.get(PylonKeys.RESEARCH_PACK_1).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.RESEARCH_PACK_1)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();
    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_1);
        PylonPages.SCIENCE.addItem(RESEARCH_PACK_1);
    }

    public static final ItemStack RESEARCH_PACK_2 = ItemStackBuilder.rebar(Material.LIME_BANNER, PylonKeys.RESEARCH_PACK_2)
            .useCooldown(Settings.get(PylonKeys.RESEARCH_PACK_2).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.RESEARCH_PACK_2)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();
    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_2);
        PylonPages.SCIENCE.addItem(RESEARCH_PACK_2);
    }

    public static final ItemStack RESEARCH_PACK_3 = ItemStackBuilder.rebar(Material.GRAY_BANNER, PylonKeys.RESEARCH_PACK_3)
            .useCooldown(Settings.get(PylonKeys.RESEARCH_PACK_3).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.RESEARCH_PACK_3)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();
    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_3);
        PylonPages.SCIENCE.addItem(RESEARCH_PACK_3);
    }

    public static final ItemStack RESEARCH_PACK_4 = ItemStackBuilder.rebar(Material.LIGHT_BLUE_BANNER, PylonKeys.RESEARCH_PACK_4)
            .useCooldown(Settings.get(PylonKeys.RESEARCH_PACK_4).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.RESEARCH_PACK_4)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();
    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_4);
        PylonPages.SCIENCE.addItem(RESEARCH_PACK_4);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Metals" defaultstate=collapsed>

    public static final ItemStack COPPER_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COPPER_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, COPPER_DUST);
        PylonPages.METALS.addItem(COPPER_DUST);
    }

    public static final ItemStack CRUSHED_RAW_COPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_COPPER)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_COPPER);
        PylonPages.METALS.addItem(CRUSHED_RAW_COPPER);
    }

    public static final ItemStack IRON_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.IRON_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, IRON_DUST);
        PylonPages.METALS.addItem(IRON_DUST);
    }

    public static final ItemStack CRUSHED_RAW_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_IRON)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_IRON);
        PylonPages.METALS.addItem(CRUSHED_RAW_IRON);
    }

    public static final ItemStack GOLD_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.GOLD_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, GOLD_DUST);
        PylonPages.METALS.addItem(GOLD_DUST);
    }

    public static final ItemStack CRUSHED_RAW_GOLD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_GOLD)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_GOLD);
        PylonPages.METALS.addItem(CRUSHED_RAW_GOLD);
    }

    public static final ItemStack RAW_TIN = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.RAW_TIN)
            .build();
    static {
        RebarItem.register(RebarItem.class, RAW_TIN);
        PylonPages.METALS.addItem(RAW_TIN);
    }

    public static final ItemStack TIN_INGOT = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.TIN_INGOT)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_INGOT.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_INGOT);
        PylonPages.METALS.addItem(TIN_INGOT);
    }

    public static final ItemStack TIN_NUGGET = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.TIN_NUGGET)
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_NUGGET);
        PylonPages.METALS.addItem(TIN_NUGGET);
    }

    public static final ItemStack TIN_BLOCK = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.TIN_BLOCK)
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_BLOCK, PylonKeys.TIN_BLOCK);
        PylonPages.METALS.addItem(TIN_BLOCK);
    }

    public static final ItemStack CRUSHED_RAW_TIN = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_TIN)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_TIN);
        PylonPages.METALS.addItem(CRUSHED_RAW_TIN);
    }

    public static final ItemStack TIN_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.TIN_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_DUST);
        PylonPages.METALS.addItem(TIN_DUST);
    }

    public static final ItemStack BRONZE_INGOT = ItemStackBuilder.rebar(Material.COPPER_INGOT, PylonKeys.BRONZE_INGOT)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_INGOT);
        PylonPages.METALS.addItem(BRONZE_INGOT);
    }

    public static final ItemStack BRONZE_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRONZE_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_DUST);
        PylonPages.METALS.addItem(BRONZE_DUST);
    }

    public static final ItemStack BRONZE_NUGGET = ItemStackBuilder.rebar(Material.ARMADILLO_SCUTE, PylonKeys.BRONZE_NUGGET)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_NUGGET);
        PylonPages.METALS.addItem(BRONZE_NUGGET);
    }

    public static final ItemStack BRONZE_BLOCK = ItemStackBuilder.rebar(Material.COPPER_BLOCK, PylonKeys.BRONZE_BLOCK)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_BLOCK, PylonKeys.BRONZE_BLOCK);
        PylonPages.METALS.addItem(BRONZE_BLOCK);
    }

    public static final ItemStack SPONGE_IRON = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.SPONGE_IRON)
            .build();
    static {
        RebarItem.register(RebarItem.class, SPONGE_IRON);
        PylonPages.METALS.addItem(SPONGE_IRON);
    }

    public static final ItemStack IRON_BLOOM = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.IRON_BLOOM)
            .build();
    static {
        RebarItem.register(IronBloom.class, IRON_BLOOM);
        PylonPages.METALS.addItem(IRON_BLOOM);
    }

    public static final ItemStack WROUGHT_IRON = ItemStackBuilder.rebar(Material.NETHERITE_SCRAP, PylonKeys.WROUGHT_IRON)
            .build();
    static {
        RebarItem.register(RebarItem.class, WROUGHT_IRON);
        PylonPages.METALS.addItem(WROUGHT_IRON);
    }

    public static final ItemStack STEEL_INGOT = ItemStackBuilder.rebar(Material.NETHERITE_INGOT, PylonKeys.STEEL_INGOT)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_INGOT);
        PylonPages.METALS.addItem(STEEL_INGOT);
    }

    public static final ItemStack STEEL_NUGGET = ItemStackBuilder.rebar(Material.NETHERITE_SCRAP, PylonKeys.STEEL_NUGGET)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_NUGGET);
        PylonPages.METALS.addItem(STEEL_NUGGET);
    }

    public static final ItemStack STEEL_BLOCK = ItemStackBuilder.rebar(Material.NETHERITE_BLOCK, PylonKeys.STEEL_BLOCK)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_BLOCK, PylonKeys.STEEL_BLOCK);
        PylonPages.METALS.addItem(STEEL_BLOCK);
    }

    public static final ItemStack STEEL_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.STEEL_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_DUST);
        PylonPages.METALS.addItem(STEEL_DUST);
    }

    public static final ItemStack PALLADIUM_INGOT = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.PALLADIUM_INGOT)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_INGOT.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_INGOT);
        PylonPages.METALS.addItem(PALLADIUM_INGOT);
    }

    public static final ItemStack PALLADIUM_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.PALLADIUM_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_DUST);
        PylonPages.METALS.addItem(PALLADIUM_DUST);
    }

    public static final ItemStack PALLADIUM_NUGGET = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.PALLADIUM_NUGGET)
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_NUGGET);
        PylonPages.METALS.addItem(PALLADIUM_NUGGET);
    }

    public static final ItemStack PALLADIUM_BLOCK = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.PALLADIUM_BLOCK)
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_BLOCK, PylonKeys.PALLADIUM_BLOCK);
        PylonPages.METALS.addItem(PALLADIUM_BLOCK);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Core Chunks" defaultstate=collapsed>

    public static final ItemStack SHALLOW_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SHALLOW_CORE_CHUNK)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHALLOW_CORE_CHUNK, PylonKeys.SHALLOW_CORE_CHUNK);
        PylonPages.CORE_CHUNKS.addItem(SHALLOW_CORE_CHUNK);
    }

    public static final ItemStack SUBSURFACE_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SUBSURFACE_CORE_CHUNK)
            .build();
    static {
        RebarItem.register(RebarItem.class, SUBSURFACE_CORE_CHUNK, PylonKeys.SUBSURFACE_CORE_CHUNK);
        PylonPages.CORE_CHUNKS.addItem(SUBSURFACE_CORE_CHUNK);
    }

    public static final ItemStack INTERMEDIATE_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.INTERMEDIATE_CORE_CHUNK)
            .build();
    static {
        RebarItem.register(RebarItem.class, INTERMEDIATE_CORE_CHUNK, PylonKeys.INTERMEDIATE_CORE_CHUNK);
        PylonPages.CORE_CHUNKS.addItem(INTERMEDIATE_CORE_CHUNK);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Magic" defaultstate=collapsed>

    public static final ItemStack SHIMMER_DUST_1 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_1)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_1);
        PylonPages.MAGIC.addItem(SHIMMER_DUST_1);
    }


    public static final ItemStack SHIMMER_DUST_2 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_2)
            .set(DataComponentTypes.ITEM_MODEL, Material.REDSTONE.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_2);
        PylonPages.MAGIC.addItem(SHIMMER_DUST_2);
    }

    public static final ItemStack SHIMMER_DUST_3 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_3)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_3);
        PylonPages.MAGIC.addItem(SHIMMER_DUST_3);
    }

    public static final ItemStack COVALENT_BINDER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COVALENT_BINDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_BLUE_DYE.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(RebarItem.class, COVALENT_BINDER);
        PylonPages.MAGIC.addItem(COVALENT_BINDER);
    }

    public static final ItemStack COHESIVE_UNIT = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COHESIVE_UNIT)
            .set(DataComponentTypes.ITEM_MODEL, Material.ENDER_PEARL.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, COHESIVE_UNIT);
        PylonPages.MAGIC.addItem(COHESIVE_UNIT);
    }

    public static final ItemStack SHIMMER_BRONZE = ItemStackBuilder.rebar(Material.COPPER_INGOT, PylonKeys.SHIMMER_BRONZE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_BRONZE);
        PylonPages.MAGIC.addItem(SHIMMER_BRONZE);
    }

    public static final ItemStack LISELETTE_CONDUCTOR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_CONDUCTOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.BAMBOO_BUTTON.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, LISELETTE_CONDUCTOR);
        PylonPages.MAGIC.addItem(LISELETTE_CONDUCTOR);
    }

    public static final ItemStack LISELETTE_CATHODE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_CATHODE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRIMSON_BUTTON.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, LISELETTE_CATHODE);
        PylonPages.MAGIC.addItem(LISELETTE_CATHODE);
    }

    public static final ItemStack LISELETTE_ANODE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_ANODE)
            .set(DataComponentTypes.ITEM_MODEL, Material.WARPED_BUTTON.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, LISELETTE_ANODE);
        PylonPages.MAGIC.addItem(LISELETTE_ANODE);
    }

    public static final ItemStack ENRICHED_SOUL_SOIL = ItemStackBuilder.rebar(Material.SOUL_SOIL, PylonKeys.ENRICHED_SOUL_SOIL)
            .build();
    static {
        RebarItem.register(RebarItem.class, ENRICHED_SOUL_SOIL, PylonKeys.ENRICHED_SOUL_SOIL);
        PylonPages.MAGIC.addItem(ENRICHED_SOUL_SOIL);
    }

    public static final ItemStack SHIMMER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.SHIMMER_SKULL)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_SKULL);
        PylonPages.MAGIC.addItem(SHIMMER_SKULL);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Miscellaneous" defaultstate=collapsed>

    public static final ItemStack ROCK_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.ROCK_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, ROCK_DUST);
        PylonPages.MISCELLANEOUS.addItem(ROCK_DUST);
    }

    public static final ItemStack QUARTZ_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.QUARTZ_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, QUARTZ_DUST);
        PylonPages.MISCELLANEOUS.addItem(QUARTZ_DUST);
    }

    public static final ItemStack DIAMOND_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.DIAMOND_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, DIAMOND_DUST);
        PylonPages.MISCELLANEOUS.addItem(DIAMOND_DUST);
    }

    public static final ItemStack EMERALD_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EMERALD_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, EMERALD_DUST);
        PylonPages.MISCELLANEOUS.addItem(EMERALD_DUST);
    }

    public static final ItemStack FIBER = ItemStackBuilder.rebar(Material.BAMBOO_MOSAIC, PylonKeys.FIBER)
            .build();
    static {
        RebarItem.register(RebarItem.class, FIBER);
        PylonPages.MISCELLANEOUS.addItem(FIBER);
    }

    public static final ItemStack COAL_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COAL_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, COAL_DUST);
        PylonPages.MISCELLANEOUS.addItem(COAL_DUST);
    }

    public static final ItemStack CHARCOAL_BLOCK = ItemStackBuilder.rebar(Material.COAL_BLOCK, PylonKeys.CHARCOAL_BLOCK)
            .build();
    static {
        RebarItem.register(RebarItem.class, CHARCOAL_BLOCK, PylonKeys.CHARCOAL_BLOCK);
        PylonPages.MISCELLANEOUS.addItem(CHARCOAL_BLOCK);
    }

    public static final ItemStack CARBON = ItemStackBuilder.rebar(Material.CHARCOAL, PylonKeys.CARBON)
            .build();
    static {
        RebarItem.register(RebarItem.class, CARBON);
        PylonPages.MISCELLANEOUS.addItem(CARBON);
    }

    public static final ItemStack OBSIDIAN_CHIP = ItemStackBuilder.rebar(Material.POLISHED_BLACKSTONE_BUTTON, PylonKeys.OBSIDIAN_CHIP)
            .build();
    static {
        RebarItem.register(RebarItem.class, OBSIDIAN_CHIP);
        PylonPages.MISCELLANEOUS.addItem(OBSIDIAN_CHIP);
    }

    public static final ItemStack SULFUR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SULFUR)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_DYE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, SULFUR);
        PylonPages.MISCELLANEOUS.addItem(SULFUR);
    }

    public static final ItemStack GYPSUM = ItemStackBuilder.rebar(Material.QUARTZ, PylonKeys.GYPSUM)
            .build();
    static {
        RebarItem.register(RebarItem.class, GYPSUM);
        PylonPages.MISCELLANEOUS.addItem(GYPSUM);
    }

    public static final ItemStack GYPSUM_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.GYPSUM_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, GYPSUM_DUST);
        PylonPages.MISCELLANEOUS.addItem(GYPSUM_DUST);
    }

    public static final ItemStack REFRACTORY_MIX = ItemStackBuilder.rebar(Material.SMOOTH_RED_SANDSTONE, PylonKeys.REFRACTORY_MIX)
            .build();
    static {
        RebarItem.register(RebarItem.class, REFRACTORY_MIX, PylonKeys.REFRACTORY_MIX);
        PylonPages.MISCELLANEOUS.addItem(REFRACTORY_MIX);
    }

    public static final ItemStack UNFIRED_REFRACTORY_BRICK = ItemStackBuilder.rebar(Material.BRICK, PylonKeys.UNFIRED_REFRACTORY_BRICK)
            .build();
    static {
        RebarItem.register(RebarItem.class, UNFIRED_REFRACTORY_BRICK, PylonKeys.UNFIRED_REFRACTORY_BRICK);
        PylonPages.MISCELLANEOUS.addItem(UNFIRED_REFRACTORY_BRICK);
    }

    public static final ItemStack REFRACTORY_BRICK = ItemStackBuilder.rebar(Material.NETHERITE_INGOT, PylonKeys.REFRACTORY_BRICK)
            .build();
    static {
        RebarItem.register(RebarItem.class, REFRACTORY_BRICK, PylonKeys.REFRACTORY_BRICK);
        PylonPages.MISCELLANEOUS.addItem(REFRACTORY_BRICK);
    }

    public static final ItemStack REFRACTORY_BRICKS = ItemStackBuilder.rebar(Material.DEEPSLATE_TILES, PylonKeys.REFRACTORY_BRICKS)
            .build();
    static {
        RebarItem.register(RebarItem.class, REFRACTORY_BRICKS, PylonKeys.REFRACTORY_BRICKS);
        PylonPages.MISCELLANEOUS.addItem(REFRACTORY_BRICKS);
    }

    // </editor-fold>

    //<editor-fold desc="Components" defaultstate=collapsed>

    public static final ItemStack COPPER_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.COPPER_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, COPPER_SHEET);
        PylonPages.COMPONENTS.addItem(COPPER_SHEET);
    }

    public static final ItemStack GOLD_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.GOLD_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, GOLD_SHEET);
        PylonPages.COMPONENTS.addItem(GOLD_SHEET);
    }

    public static final ItemStack IRON_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.IRON_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, IRON_SHEET);
        PylonPages.COMPONENTS.addItem(IRON_SHEET);
    }

    public static final ItemStack TIN_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.TIN_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_SHEET);
        PylonPages.COMPONENTS.addItem(TIN_SHEET);
    }

    public static final ItemStack BRONZE_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.BRONZE_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_SHEET);
        PylonPages.COMPONENTS.addItem(BRONZE_SHEET);
    }

    public static final ItemStack STEEL_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.STEEL_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_SHEET);
        PylonPages.COMPONENTS.addItem(STEEL_SHEET);
    }

    public static final ItemStack PALLADIUM_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.PALLADIUM_SHEET)
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_SHEET);
        PylonPages.COMPONENTS.addItem(PALLADIUM_SHEET);
    }

    public static final ItemStack COPPER_DRILL_BIT = ItemStackBuilder.rebar(Material.LIGHTNING_ROD, PylonKeys.COPPER_DRILL_BIT)
            .build();
    static {
        RebarItem.register(RebarItem.class, COPPER_DRILL_BIT);
        PylonPages.COMPONENTS.addItem(COPPER_DRILL_BIT);
    }

    public static final ItemStack BRONZE_DRILL_BIT = ItemStackBuilder.rebar(Material.LIGHTNING_ROD, PylonKeys.BRONZE_DRILL_BIT)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_DRILL_BIT);
        PylonPages.COMPONENTS.addItem(BRONZE_DRILL_BIT);
    }

    public static final ItemStack ROTOR = ItemStackBuilder.rebar(Material.IRON_TRAPDOOR, PylonKeys.ROTOR)
            .build();
    static {
        RebarItem.register(RebarItem.class, ROTOR);
        PylonPages.COMPONENTS.addItem(ROTOR);
    }

    public static final ItemStack BACKFLOW_VALVE = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.BACKFLOW_VALVE)
            .build();
    static {
        RebarItem.register(RebarItem.class, BACKFLOW_VALVE);
        PylonPages.COMPONENTS.addItem(BACKFLOW_VALVE);
    }

    public static final ItemStack ANALOGUE_DISPLAY = ItemStackBuilder.rebar(Material.LIME_STAINED_GLASS_PANE, PylonKeys.ANALOGUE_DISPLAY)
            .build();
    static {
        RebarItem.register(RebarItem.class, ANALOGUE_DISPLAY);
        PylonPages.COMPONENTS.addItem(ANALOGUE_DISPLAY);
    }

    public static final ItemStack FILTER_MESH = ItemStackBuilder.rebar(Material.IRON_BARS, PylonKeys.FILTER_MESH)
            .build();
    static {
        RebarItem.register(RebarItem.class, FILTER_MESH);
        PylonPages.COMPONENTS.addItem(FILTER_MESH);
    }

    public static final ItemStack NOZZLE = ItemStackBuilder.rebar(Material.LEVER, PylonKeys.NOZZLE)
            .build();
    static {
        RebarItem.register(RebarItem.class, NOZZLE);
        PylonPages.COMPONENTS.addItem(NOZZLE);
    }

    public static final ItemStack ABYSSAL_CATALYST = ItemStackBuilder.rebar(Material.BLACK_CANDLE, PylonKeys.ABYSSAL_CATALYST)
            .build();
    static {
        RebarItem.register(RebarItem.class, ABYSSAL_CATALYST);
        PylonPages.COMPONENTS.addItem(ABYSSAL_CATALYST);
    }

    public static final ItemStack HYDRAULIC_MOTOR = ItemStackBuilder.rebar(Material.PISTON, PylonKeys.HYDRAULIC_MOTOR)
            .build();
    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_MOTOR);
        PylonPages.COMPONENTS.addItem(HYDRAULIC_MOTOR);
    }

    public static final ItemStack AXLE = ItemStackBuilder.rebar(Material.OAK_FENCE, PylonKeys.AXLE)
            .build();
    static {
        RebarItem.register(RebarItem.class, AXLE);
        PylonPages.COMPONENTS.addItem(AXLE);
    }

    public static final ItemStack SAWBLADE = ItemStackBuilder.rebar(Material.IRON_BARS, PylonKeys.SAWBLADE)
            .build();
    static {
        RebarItem.register(RebarItem.class, SAWBLADE);
        PylonPages.COMPONENTS.addItem(SAWBLADE);
    }

    public static final ItemStack WEIGHTED_SHAFT = ItemStackBuilder.rebar(Material.DEEPSLATE_TILE_WALL, PylonKeys.WEIGHTED_SHAFT)
            .build();
    static {
        RebarItem.register(RebarItem.class, WEIGHTED_SHAFT);
        PylonPages.COMPONENTS.addItem(WEIGHTED_SHAFT);
    }

    public static final ItemStack HYDRAULIC_CANNON_CHAMBER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HYDRAULIC_CANNON_CHAMBER)
            .set(DataComponentTypes.ITEM_MODEL, Material.SNOWBALL.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_CANNON_CHAMBER);
        PylonPages.COMPONENTS.addItem(HYDRAULIC_CANNON_CHAMBER);
    }

    public static final ItemStack PORTABILITY_CATALYST = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.PORTABILITY_CATALYST)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(RebarItem.class, PORTABILITY_CATALYST);
        PylonPages.COMPONENTS.addItem(PORTABILITY_CATALYST);
    }

    public static final ItemStack FLUID_INPUT_HATCH = ItemStackBuilder.rebar(Material.LIGHT_BLUE_TERRACOTTA, PylonKeys.FLUID_INPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, FLUID_INPUT_HATCH, PylonKeys.FLUID_INPUT_HATCH);
        PylonPages.COMPONENTS.addItem(FLUID_INPUT_HATCH);
    }

    public static final ItemStack FLUID_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.ORANGE_TERRACOTTA, PylonKeys.FLUID_OUTPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, FLUID_OUTPUT_HATCH, PylonKeys.FLUID_OUTPUT_HATCH);
        PylonPages.COMPONENTS.addItem(FLUID_OUTPUT_HATCH);
    }

    public static final ItemStack ITEM_INPUT_HATCH = ItemStackBuilder.rebar(Material.GREEN_TERRACOTTA, PylonKeys.ITEM_INPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, ITEM_INPUT_HATCH, PylonKeys.ITEM_INPUT_HATCH);
        PylonPages.COMPONENTS.addItem(ITEM_INPUT_HATCH);
    }

    public static final ItemStack ITEM_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.RED_TERRACOTTA, PylonKeys.ITEM_OUTPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, ITEM_OUTPUT_HATCH, PylonKeys.ITEM_OUTPUT_HATCH);
        PylonPages.COMPONENTS.addItem(ITEM_OUTPUT_HATCH);
    }

    public static final ItemStack STEEL_CYLINDER = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.STEEL_CYLINDER)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_CYLINDER, PylonKeys.STEEL_CYLINDER);
        PylonPages.COMPONENTS.addItem(STEEL_CYLINDER);
    }

    public static final ItemStack STEEL_CRANKSHAFT = ItemStackBuilder.rebar(Material.GRAY_CANDLE, PylonKeys.STEEL_CRANKSHAFT)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_CRANKSHAFT, PylonKeys.STEEL_CRANKSHAFT);
        PylonPages.COMPONENTS.addItem(STEEL_CRANKSHAFT);
    }

    public static final ItemStack KINETIC_CONTROL_MECHANISM = ItemStackBuilder.rebar(Material.CLOCK, PylonKeys.KINETIC_CONTROL_MECHANISM)
            .build();
    static {
        RebarItem.register(RebarItem.class, KINETIC_CONTROL_MECHANISM, PylonKeys.KINETIC_CONTROL_MECHANISM);
        PylonPages.COMPONENTS.addItem(KINETIC_CONTROL_MECHANISM);
    }

    public static final ItemStack INTERNAL_COMBUSTION_ENGINE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.INTERNAL_COMBUSTION_ENGINE)
            .set(DataComponentTypes.ITEM_MODEL, Material.DROPPER.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, INTERNAL_COMBUSTION_ENGINE, PylonKeys.INTERNAL_COMBUSTION_ENGINE);
        PylonPages.COMPONENTS.addItem(INTERNAL_COMBUSTION_ENGINE);
    }

    public static final ItemStack CARGO_CONTROLLER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CARGO_CONTROLLER)
            .set(DataComponentTypes.ITEM_MODEL, Material.NOTE_BLOCK.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CARGO_CONTROLLER, PylonKeys.CARGO_CONTROLLER);
        PylonPages.COMPONENTS.addItem(CARGO_CONTROLLER);
    }

    //</editor-fold>

    //<editor-fold desc="Tools" defaultstate=collapsed>
    public static final ItemStack STONE_HAMMER = ItemStackBuilder.rebarWeapon(Material.STONE_PICKAXE, PylonKeys.STONE_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();
    static {
        RebarItem.register(Hammer.class, STONE_HAMMER);
        PylonPages.TOOLS.addItem(STONE_HAMMER);
    }

    public static final ItemStack IRON_HAMMER = ItemStackBuilder.rebarWeapon(Material.IRON_PICKAXE, PylonKeys.IRON_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();
    static {
        RebarItem.register(Hammer.class, IRON_HAMMER);
        PylonPages.TOOLS.addItem(IRON_HAMMER);
    }

    public static final ItemStack DIAMOND_HAMMER = ItemStackBuilder.rebarWeapon(Material.DIAMOND_PICKAXE, PylonKeys.DIAMOND_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();
    static {
        RebarItem.register(Hammer.class, DIAMOND_HAMMER);
        PylonPages.TOOLS.addItem(DIAMOND_HAMMER);
    }

    public static final ItemStack BRONZE_AXE = ItemStackBuilder.rebarToolWeapon(Material.CLAY_BALL, PylonKeys.BRONZE_AXE, RebarUtils.axeMineable(), true, false, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_AXE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_AXE);
        PylonPages.TOOLS.addItem(BRONZE_AXE);
    }

    public static final ItemStack BRONZE_PICKAXE = ItemStackBuilder.rebarToolWeapon(Material.CLAY_BALL, PylonKeys.BRONZE_PICKAXE, RebarUtils.pickaxeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_PICKAXE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_PICKAXE);
        PylonPages.TOOLS.addItem(BRONZE_PICKAXE);
    }

    public static final ItemStack BRONZE_SHOVEL = ItemStackBuilder.rebarToolWeapon(Material.CLAY_BALL, PylonKeys.BRONZE_SHOVEL, RebarUtils.shovelMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SHOVEL.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_SHOVEL);
        PylonPages.TOOLS.addItem(BRONZE_SHOVEL);
    }

    public static final ItemStack BRONZE_HOE = ItemStackBuilder.rebarToolWeapon(Material.CLAY_BALL, PylonKeys.BRONZE_HOE, RebarUtils.hoeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_HOE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_HOE);
        PylonPages.TOOLS.addItem(BRONZE_HOE);
    }

    public static final ItemStack WATERING_CAN = ItemStackBuilder.rebar(Material.BUCKET, PylonKeys.WATERING_CAN)
            .build();
    static {
        RebarItem.register(WateringCan.class, WATERING_CAN);
        PylonPages.TOOLS.addItem(WATERING_CAN);
    }

    public static final ItemStack LUMBER_AXE = ItemStackBuilder.rebar(Material.WOODEN_AXE, PylonKeys.LUMBER_AXE)
            .durability(Settings.get(PylonKeys.LUMBER_AXE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(LumberAxe.class, LUMBER_AXE);
        PylonPages.TOOLS.addItem(LUMBER_AXE);
    }

    public static final ItemStack PORTABLE_CRAFTING_TABLE = ItemStackBuilder.rebar(Material.CRAFTING_TABLE, PylonKeys.PORTABLE_CRAFTING_TABLE)
            .build();
    static {
        RebarItem.register(PortableCraftingTable.class, PORTABLE_CRAFTING_TABLE);
        PylonPages.TOOLS.addItem(PORTABLE_CRAFTING_TABLE);
    }

    public static final ItemStack PORTABLE_DUSTBIN = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.PORTABLE_DUSTBIN)
            .build();
    static {
        RebarItem.register(PortableDustbin.class, PORTABLE_DUSTBIN);
        PylonPages.TOOLS.addItem(PORTABLE_DUSTBIN);
    }

    public static final ItemStack PORTABLE_ENDER_CHEST = ItemStackBuilder.rebar(Material.ENDER_CHEST, PylonKeys.PORTABLE_ENDER_CHEST)
            .build();
    static {
        RebarItem.register(PortableEnderChest.class, PORTABLE_ENDER_CHEST);
        PylonPages.TOOLS.addItem(PORTABLE_ENDER_CHEST);
    }

    public static final ItemStack CLIMBING_PICK = ItemStackBuilder.rebar(Material.DIAMOND_HOE, PylonKeys.CLIMBING_PICK)
            .build();
    static {
        RebarItem.register(ClimbingPick.class, CLIMBING_PICK);
        PylonPages.TOOLS.addItem(CLIMBING_PICK);
    }

    public static final ItemStack BRICK_MOLD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRICK_MOLD)
            .useCooldown(Settings.get(PylonKeys.BRICK_MOLD).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.BRICK_MOLD)
            .set(DataComponentTypes.ITEM_MODEL, Material.OAK_FENCE_GATE.getKey())
            .build();
    static {
        RebarItem.register(BrickMold.class, BRICK_MOLD);
        PylonPages.TOOLS.addItem(BRICK_MOLD);
    }

    public static final ItemStack TONGS = ItemStackBuilder.rebar(Material.SHEARS, PylonKeys.TONGS)
            .build();
    static {
        RebarItem.register(RebarItem.class, TONGS);
        PylonPages.TOOLS.addItem(TONGS);
    }

    public static final ItemStack SHIMMER_MAGNET = ItemStackBuilder.rebar(Material.BREEZE_ROD, PylonKeys.SHIMMER_MAGNET)
        .set(DataComponentTypes.MAX_STACK_SIZE, 1)
        .build();
    static {
        RebarItem.register(ShimmerMagnet.class, SHIMMER_MAGNET);
        PylonPages.TOOLS.addItem(SHIMMER_MAGNET);
    }

    public static final ItemStack FIREPROOF_RUNE = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.FIREPROOF_RUNE)
            .set(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(DamageTypeTagKeys.IS_FIRE))
            .set(DataComponentTypes.FIREWORK_EXPLOSION, FireworkEffect.builder()
                    .withColor(Color.fromRGB(0xff5e00))
                    .build())
            .hideFromTooltip(DataComponentTypes.FIREWORK_EXPLOSION)
            .build();
    static {
        RebarItem.register(FireproofRune.class, FIREPROOF_RUNE);
        PylonPages.TOOLS.addItem(FIREPROOF_RUNE);
    }

    public static final ItemStack SOULBOUND_RUNE = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SOULBOUND_RUNE)
            .set(DataComponentTypes.FIREWORK_EXPLOSION, FireworkEffect.builder()
                    .withColor(Color.PURPLE)
                    .build())
            .hideFromTooltip(DataComponentTypes.FIREWORK_EXPLOSION)
            .build();
    static {
        RebarItem.register(SoulboundRune.class, SOULBOUND_RUNE);
        PylonPages.TOOLS.addItem(SOULBOUND_RUNE);
    }

    @SuppressWarnings("ConstantConditions")
    public static final ItemStack CONFETTI_POPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CONFETTI_POPPER)
            .set(DataComponentTypes.ITEM_MODEL, Material.FIREWORK_ROCKET.getKey())
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(
                            Settings.get(PylonKeys.CONFETTI_POPPER).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue()
                    )
                    .sound(Registry.SOUNDS.getKey(Sound.ITEM_CROSSBOW_LOADING_START))
                    .animation(ItemUseAnimation.TOOT_HORN)
                    .hasConsumeParticles(false)
            )
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(
                            Settings.get(PylonKeys.CONFETTI_POPPER).getOrThrow("cooldown-seconds", ConfigAdapter.DOUBLE).floatValue()
                    )
                    .cooldownGroup(PylonKeys.CONFETTI_POPPER)
                    .build())
            .build();
    static {
        RebarItem.register(ConfettiPopper.class, CONFETTI_POPPER);
        PylonPages.TOOLS.addItem(CONFETTI_POPPER);
    }

    //</editor-fold>

    //<editor-fold desc="Combat" defaultstate=collapsed>

    public static final ItemStack BRONZE_SWORD = ItemStackBuilder.rebarWeapon(Material.CLAY_BALL, PylonKeys.BRONZE_SWORD, true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SWORD.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_SWORD);
        PylonPages.COMBAT.addItem(BRONZE_SWORD);
    }

    public static final ItemStack BEHEADING_SWORD = ItemStackBuilder.rebar(Material.DIAMOND_SWORD, PylonKeys.BEHEADING_SWORD)
            .durability(Settings.get(PylonKeys.BEHEADING_SWORD).getOrThrow("durability", ConfigAdapter.INTEGER)) // todo: weapon stats?
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(BeheadingSword.class, BEHEADING_SWORD);
        PylonPages.COMBAT.addItem(BEHEADING_SWORD);
    }

    public static final ItemStack BANDAGE = ItemStackBuilder.rebar(Material.COBWEB, PylonKeys.BANDAGE)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(Settings.get(PylonKeys.BANDAGE).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .build())
            .build();
    static {
        RebarItem.register(HealingConsumable.class, BANDAGE);
        PylonPages.COMBAT.addItem(BANDAGE);
    }

    public static final ItemStack SPLINT = ItemStackBuilder.rebar(Material.STICK, PylonKeys.SPLINT)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(Settings.get(PylonKeys.SPLINT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .build())
            .build();
    static {
        RebarItem.register(HealingConsumable.class, SPLINT);
        PylonPages.COMBAT.addItem(SPLINT);
    }

    public static final ItemStack DISINFECTANT = ItemStackBuilder.rebar(Material.BREWER_POTTERY_SHERD, PylonKeys.DISINFECTANT)
            // Using the actual potion material doesn't let you set the name properly, gives you a
            // class string of a nonexistant potion type for some reason
            .set(DataComponentTypes.ITEM_MODEL, Material.POTION.getKey())
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .hasConsumeParticles(false)
                    .consumeSeconds(Settings.get(PylonKeys.DISINFECTANT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .addEffect(ConsumeEffect.clearAllStatusEffects())
                    .build())
            .build();
    static {
        RebarItem.register(HealingConsumable.class, DISINFECTANT);
        PylonPages.COMBAT.addItem(DISINFECTANT);
    }

    public static final ItemStack MEDKIT = ItemStackBuilder.rebar(Material.SHULKER_SHELL, PylonKeys.MEDKIT)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(Settings.get(PylonKeys.MEDKIT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .addEffect(ConsumeEffect.clearAllStatusEffects())
            )
            .build();
    static {
        RebarItem.register(HealingConsumable.class, MEDKIT);
        PylonPages.COMBAT.addItem(MEDKIT);
    }

    public static final ItemStack REACTIVATED_WITHER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.REACTIVATED_WITHER_SKULL)
            .durability(Settings.get(PylonKeys.REACTIVATED_WITHER_SKULL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .useCooldown(Settings.get(PylonKeys.REACTIVATED_WITHER_SKULL).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.REACTIVATED_WITHER_SKULL)
            .build();
    static {
        RebarItem.register(ReactivatedWitherSkull.class, REACTIVATED_WITHER_SKULL);
        PylonPages.COMBAT.addItem(REACTIVATED_WITHER_SKULL);
    }

    public static final ItemStack HYPER_ACTIVATED_WITHER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.HYPER_ACTIVATED_WITHER_SKULL)
            .durability(Settings.get(PylonKeys.HYPER_ACTIVATED_WITHER_SKULL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .useCooldown(Settings.get(PylonKeys.HYPER_ACTIVATED_WITHER_SKULL).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.REACTIVATED_WITHER_SKULL)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();
    static {
        RebarItem.register(ReactivatedWitherSkull.class, HYPER_ACTIVATED_WITHER_SKULL);
        PylonPages.COMBAT.addItem(HYPER_ACTIVATED_WITHER_SKULL);
    }

    public static final ItemStack ICE_ARROW = ItemStackBuilder.rebar(Material.ARROW, PylonKeys.ICE_ARROW).build();
    static {
        RebarItem.register(IceArrow.class, ICE_ARROW, PylonKeys.ICE_ARROW);
        PylonPages.COMBAT.addItem(ICE_ARROW);
    }

    public static final ItemStack RECOIL_ARROW = ItemStackBuilder.rebar(Material.ARROW, PylonKeys.RECOIL_ARROW)
            .build();
    static {
        RebarItem.register(RecoilArrow.class, RECOIL_ARROW);
        PylonPages.COMBAT.addItem(RECOIL_ARROW);
    }

    public static final ItemStack HYDRAULIC_CANNON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HYDRAULIC_CANNON)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_HORSE_ARMOR.getKey())
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown
                    .useCooldown(
                            Settings.get(PylonKeys.HYDRAULIC_CANNON).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    )
                    .cooldownGroup(PylonKeys.HYDRAULIC_CANNON.key())
                    .build())
            .editPdc(pdc -> {
                pdc.set(PylonFluids.HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, 0.0);
                pdc.set(PylonFluids.DIRTY_HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, 0.0);
            })
            .build();
    static {
        RebarItem.register(HydraulicCannon.class, HYDRAULIC_CANNON);
        PylonPages.COMBAT.addItem(HYDRAULIC_CANNON);
    }

    public static final ItemStack TIN_PROJECTILE = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.TIN_PROJECTILE)
            .build();
    static {
        RebarItem.register(RebarItem.class, TIN_PROJECTILE);
        PylonPages.COMBAT.addItem(TIN_PROJECTILE);
    }
    //</editor-fold>

    //<editor-fold desc="Talismans" defaultstate=collapsed>

    public static final ItemStack HEALTH_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(HEALTH_TALISMAN_SIMPLE);
    }

    public static final ItemStack HEALTH_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(HEALTH_TALISMAN_ADVANCED);
    }

    public static final ItemStack HEALTH_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(HEALTH_TALISMAN_ULTIMATE);
    }

    public static final ItemStack HEALTH_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_PALLADIUM)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(HEALTH_TALISMAN_PALLADIUM);
    }

    public static final ItemStack HUNGER_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HUNGER_TALISMAN_SIMPLE)
            .set(DataComponentTypes.ITEM_MODEL, Objects.requireNonNull(Material.GOLDEN_APPLE.getDefaultData(DataComponentTypes.ITEM_MODEL)))
            .build();
    static {
        RebarItem.register(HungerTalisman.class, HUNGER_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(HUNGER_TALISMAN_SIMPLE);
    }

    public static final ItemStack HUNGER_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HUNGER_TALISMAN_ADVANCED)
            .set(DataComponentTypes.ITEM_MODEL, Objects.requireNonNull(Material.GOLDEN_APPLE.getDefaultData(DataComponentTypes.ITEM_MODEL)))
            .build();
    static {
        RebarItem.register(HungerTalisman.class, HUNGER_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(HUNGER_TALISMAN_ADVANCED);
    }

    public static final ItemStack HUNGER_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HUNGER_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.ITEM_MODEL, Objects.requireNonNull(Material.GOLDEN_APPLE.getDefaultData(DataComponentTypes.ITEM_MODEL)))
            .build();
    static {
        RebarItem.register(HungerTalisman.class, HUNGER_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(HUNGER_TALISMAN_ULTIMATE);
    }

    public static final ItemStack HUNGER_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HUNGER_TALISMAN_PALLADIUM)
            .set(DataComponentTypes.ITEM_MODEL, Objects.requireNonNull(Material.GOLDEN_APPLE.getDefaultData(DataComponentTypes.ITEM_MODEL)))
            .build();
    static {
        RebarItem.register(HungerTalisman.class, HUNGER_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(HUNGER_TALISMAN_PALLADIUM);
    }

    public static final ItemStack FARMING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(FARMING_TALISMAN_SIMPLE);
    }

    public static final ItemStack FARMING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(FARMING_TALISMAN_ADVANCED);
    }

    public static final ItemStack FARMING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(FARMING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack FARMING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(FARMING_TALISMAN_PALLADIUM);
    }

    public static final ItemStack BARTERING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(BARTERING_TALISMAN_SIMPLE);
    }

    public static final ItemStack BARTERING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(BARTERING_TALISMAN_ADVANCED);
    }

    public static final ItemStack BARTERING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(BARTERING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack BARTERING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(BARTERING_TALISMAN_PALLADIUM);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(WATER_BREATHING_TALISMAN_SIMPLE);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(WATER_BREATHING_TALISMAN_ADVANCED);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(WATER_BREATHING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(WATER_BREATHING_TALISMAN_PALLADIUM);
    }

    public static final ItemStack LUCK_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(LUCK_TALISMAN_SIMPLE);
    }

    public static final ItemStack LUCK_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(LUCK_TALISMAN_ADVANCED);
    }

    public static final ItemStack LUCK_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(LUCK_TALISMAN_ULTIMATE);
    }

    public static final ItemStack LUCK_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(LUCK_TALISMAN_PALLADIUM);
    }

    public static final ItemStack BREEDING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(BREEDING_TALISMAN_SIMPLE);
    }

    public static final ItemStack BREEDING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(BREEDING_TALISMAN_ADVANCED);
    }

    public static final ItemStack BREEDING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(BREEDING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack BREEDING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(BREEDING_TALISMAN_PALLADIUM);
    }

    public static final ItemStack ENCHANTING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .build();
    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(ENCHANTING_TALISMAN_SIMPLE);
    }

    public static final ItemStack ENCHANTING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .build();
    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(ENCHANTING_TALISMAN_ADVANCED);
    }

    public static final ItemStack ENCHANTING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .build();
    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(ENCHANTING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack ENCHANTING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_PALLADIUM)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .build();
    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(ENCHANTING_TALISMAN_PALLADIUM);
    }

    public static final ItemStack HUNTING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_SIMPLE)
            .build();
    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_SIMPLE);
        PylonPages.TALISMANS.addItem(HUNTING_TALISMAN_SIMPLE);
    }

    public static final ItemStack HUNTING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_ADVANCED)
            .build();
    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_ADVANCED);
        PylonPages.TALISMANS.addItem(HUNTING_TALISMAN_ADVANCED);
    }

    public static final ItemStack HUNTING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_ULTIMATE)
            .build();
    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_ULTIMATE);
        PylonPages.TALISMANS.addItem(HUNTING_TALISMAN_ULTIMATE);
    }

    public static final ItemStack HUNTING_TALISMAN_PALLADIUM = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_PALLADIUM)
            .build();
    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_PALLADIUM);
        PylonPages.TALISMANS.addItem(HUNTING_TALISMAN_PALLADIUM);
    }

    //</editor-fold>

    //<editor-fold desc="Armour" defaultstate=collapsed>

    public static final ItemStack BRONZE_HELMET = ItemStackBuilder.rebarHelmet(Material.CLAY_BALL, PylonKeys.BRONZE_HELMET, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_HELMET.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD)
                    .assetId(Key.key("gold"))
                    .build())
            .build();
    static {
        RebarItem.register(BronzeArmor.class, BRONZE_HELMET);
        PylonPages.ARMOUR.addItem(BRONZE_HELMET);
    }

    public static final ItemStack BRONZE_CHESTPLATE = ItemStackBuilder.rebarChestplate(Material.CLAY_BALL, PylonKeys.BRONZE_CHESTPLATE, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_CHESTPLATE.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.CHEST)
                    .assetId(Key.key("gold"))
                    .build())
            .build();
    static {
        RebarItem.register(BronzeArmor.class, BRONZE_CHESTPLATE);
        PylonPages.ARMOUR.addItem(BRONZE_CHESTPLATE);
    }

    public static final ItemStack BRONZE_LEGGINGS = ItemStackBuilder.rebarLeggings(Material.CLAY_BALL, PylonKeys.BRONZE_LEGGINGS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_LEGGINGS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.LEGS)
                    .assetId(Key.key("gold"))
                    .build())
            .build();
    static {
        RebarItem.register(BronzeArmor.class, BRONZE_LEGGINGS);
        PylonPages.ARMOUR.addItem(BRONZE_LEGGINGS);
    }

    public static final ItemStack BRONZE_BOOTS = ItemStackBuilder.rebarBoots(Material.GOLDEN_BOOTS, PylonKeys.BRONZE_BOOTS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_BOOTS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.FEET)
                    .assetId(Key.key("gold"))
                    .build())
            .build();
    static {
        RebarItem.register(BronzeArmor.class, BRONZE_BOOTS);
        PylonPages.ARMOUR.addItem(BRONZE_BOOTS);
    }

    //</editor-fold>

    //<editor-fold desc="Food" defaultstate=collapsed>

    public static final ItemStack FLOUR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLOUR)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, FLOUR);
        PylonPages.MISCELLANEOUS.addItem(FLOUR);
    }

    public static final ItemStack DOUGH = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.DOUGH)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_DYE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, DOUGH);
        PylonPages.MISCELLANEOUS.addItem(DOUGH);
    }

    public static final ItemStack MONSTER_JERKY = ItemStackBuilder.rebar(Material.ROTTEN_FLESH, PylonKeys.MONSTER_JERKY)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable().build())
            .set(DataComponentTypes.FOOD, FoodProperties.food()
                    .canAlwaysEat(false)
                    .nutrition(Settings.get(PylonKeys.MONSTER_JERKY).getOrThrow("nutrition", ConfigAdapter.INTEGER))
                    .saturation(Settings.get(PylonKeys.MONSTER_JERKY).getOrThrow("saturation", ConfigAdapter.DOUBLE).floatValue())
                    .build()
            )
            .build();
    static {
        RebarItem.register(RebarItem.class, MONSTER_JERKY);
        PylonPages.FOOD.addItem(MONSTER_JERKY);
    }

    //</editor-fold>

    //<editor-fold desc="Building" defaultstate=collapsed>

    public static final ItemStack IGNEOUS_COMPOSITE = ItemStackBuilder.rebar(Material.OBSIDIAN, PylonKeys.IGNEOUS_COMPOSITE)
            .build();
    static {
        RebarItem.register(RebarItem.class, IGNEOUS_COMPOSITE, PylonKeys.IGNEOUS_COMPOSITE);
        PylonPages.BUILDING.addItem(IGNEOUS_COMPOSITE);
    }

    public static final ItemStack PEDESTAL = ItemStackBuilder.rebar(Material.STONE_BRICK_WALL, PylonKeys.PEDESTAL)
            .build();
    static {
        RebarItem.register(RebarItem.class, PEDESTAL, PylonKeys.PEDESTAL);
        PylonPages.BUILDING.addItem(PEDESTAL);
    }

    public static final ItemStack ELEVATOR_1 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_1)
            .build();
    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_1, PylonKeys.ELEVATOR_1);
        PylonPages.BUILDING.addItem(ELEVATOR_1);
    }

    public static final ItemStack ELEVATOR_2 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_2)
            .build();
    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_2, PylonKeys.ELEVATOR_2);
        PylonPages.BUILDING.addItem(ELEVATOR_2);
    }

    public static final ItemStack ELEVATOR_3 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_3)
            .build();
    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_3, PylonKeys.ELEVATOR_3);
        PylonPages.BUILDING.addItem(ELEVATOR_3);
    }

    public static final ItemStack EXPLOSIVE_TARGET = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET)
            .build();
    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET, PylonKeys.EXPLOSIVE_TARGET);
        PylonPages.BUILDING.addItem(EXPLOSIVE_TARGET);
    }

    public static final ItemStack EXPLOSIVE_TARGET_FIERY = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_FIERY)
            .build();
    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_FIERY, PylonKeys.EXPLOSIVE_TARGET_FIERY);
        PylonPages.BUILDING.addItem(EXPLOSIVE_TARGET_FIERY);
    }

    public static final ItemStack EXPLOSIVE_TARGET_SUPER = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_SUPER)
            .build();
    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_SUPER, PylonKeys.EXPLOSIVE_TARGET_SUPER);
        PylonPages.BUILDING.addItem(EXPLOSIVE_TARGET_SUPER);
    }

    public static final ItemStack EXPLOSIVE_TARGET_SUPER_FIERY = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_SUPER_FIERY)
            .build();
    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_SUPER_FIERY, PylonKeys.EXPLOSIVE_TARGET_SUPER_FIERY);
        PylonPages.BUILDING.addItem(EXPLOSIVE_TARGET_SUPER_FIERY);
    }

    public static final ItemStack IMMOBILIZER = ItemStackBuilder.rebar(Material.PISTON, PylonKeys.IMMOBILIZER)
            .build();
    static {
        RebarItem.register(Immobilizer.Item.class, IMMOBILIZER, PylonKeys.IMMOBILIZER);
        PylonPages.BUILDING.addItem(IMMOBILIZER);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Simple Machines" defaultstate=collapsed>

    public static final ItemStack GRINDSTONE = ItemStackBuilder.rebar(Material.SMOOTH_STONE_SLAB, PylonKeys.GRINDSTONE)
            .build();
    static {
        RebarItem.register(RebarItem.class, GRINDSTONE, PylonKeys.GRINDSTONE);
        PylonPages.SIMPLE_MACHINES.addItem(GRINDSTONE);
    }

    public static final ItemStack GRINDSTONE_HANDLE = ItemStackBuilder.rebar(Material.OAK_FENCE, PylonKeys.GRINDSTONE_HANDLE)
            .build();
    static {
        RebarItem.register(RebarItem.class, GRINDSTONE_HANDLE, PylonKeys.GRINDSTONE_HANDLE);
        PylonPages.SIMPLE_MACHINES.addItem(GRINDSTONE_HANDLE);
    }

    public static final ItemStack MIXING_POT = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.MIXING_POT)
            .build();
    static {
        RebarItem.register(MixingPot.MixingPotItem.class, MIXING_POT, PylonKeys.MIXING_POT);
        PylonPages.SIMPLE_MACHINES.addItem(MIXING_POT);
    }

    public static final ItemStack MANUAL_CORE_DRILL_LEVER = ItemStackBuilder.rebar(Material.LEVER, PylonKeys.MANUAL_CORE_DRILL_LEVER)
            .build();
    static {
        RebarItem.register(RebarItem.class, MANUAL_CORE_DRILL_LEVER, PylonKeys.MANUAL_CORE_DRILL_LEVER);
        PylonPages.SIMPLE_MACHINES.addItem(MANUAL_CORE_DRILL_LEVER);
    }

    public static final ItemStack MANUAL_CORE_DRILL = ItemStackBuilder.rebar(Material.CHISELED_STONE_BRICKS, PylonKeys.MANUAL_CORE_DRILL)
            .build();
    static {
        RebarItem.register(CoreDrill.Item.class, MANUAL_CORE_DRILL, PylonKeys.MANUAL_CORE_DRILL);
        PylonPages.SIMPLE_MACHINES.addItem(MANUAL_CORE_DRILL);
    }

    public static final ItemStack IMPROVED_MANUAL_CORE_DRILL = ItemStackBuilder.rebar(Material.WAXED_OXIDIZED_COPPER, PylonKeys.IMPROVED_MANUAL_CORE_DRILL)
            .build();
    static {
        RebarItem.register(ImprovedManualCoreDrill.Item.class, IMPROVED_MANUAL_CORE_DRILL, PylonKeys.IMPROVED_MANUAL_CORE_DRILL);
        PylonPages.SIMPLE_MACHINES.addItem(IMPROVED_MANUAL_CORE_DRILL);
    }

    public static final ItemStack PRESS = ItemStackBuilder.rebar(Material.COMPOSTER, PylonKeys.PRESS)
            .build();
    static {
        RebarItem.register(Press.PressItem.class, PRESS, PylonKeys.PRESS);
        PylonPages.SIMPLE_MACHINES.addItem(PRESS);
    }

    public static final ItemStack SPRINKLER = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.SPRINKLER)
            .build();
    static {
        RebarItem.register(Sprinkler.Item.class, SPRINKLER, PylonKeys.SPRINKLER);
        PylonPages.FLUID_MACHINES.addItem(SPRINKLER);
    }

    public static final ItemStack CRUCIBLE = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.CRUCIBLE)
            .build();
    static {
        RebarItem.register(RebarItem.class, CRUCIBLE, PylonKeys.CRUCIBLE);
        PylonPages.SIMPLE_MACHINES.addItem(CRUCIBLE);
    }

    public static final ItemStack FLUID_STRAINER = ItemStackBuilder.rebar(Material.COPPER_GRATE, PylonKeys.FLUID_STRAINER)
            .build();
    static {
        RebarItem.register(FluidStrainer.Item.class, FLUID_STRAINER, PylonKeys.FLUID_STRAINER);
        PylonPages.FLUID_MACHINES.addItem(FLUID_STRAINER);
    }

    public static final ItemStack VACUUM_HOPPER_1 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_1)
            .build();
    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_1, PylonKeys.VACUUM_HOPPER_1);
        PylonPages.SIMPLE_MACHINES.addItem(VACUUM_HOPPER_1);
    }

    public static final ItemStack VACUUM_HOPPER_2 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_2)
            .build();
    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_2, PylonKeys.VACUUM_HOPPER_2);
        PylonPages.SIMPLE_MACHINES.addItem(VACUUM_HOPPER_2);
    }

    public static final ItemStack VACUUM_HOPPER_3 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_3)
            .build();
    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_3, PylonKeys.VACUUM_HOPPER_3);
        PylonPages.SIMPLE_MACHINES.addItem(VACUUM_HOPPER_3);
    }

    public static final ItemStack VACUUM_HOPPER_4 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_4)
            .build();
    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_4, PylonKeys.VACUUM_HOPPER_4);
        PylonPages.SIMPLE_MACHINES.addItem(VACUUM_HOPPER_4);
    }

    public static final ItemStack SHIMMER_PEDESTAL = ItemStackBuilder.rebar(Material.MOSSY_STONE_BRICK_WALL, PylonKeys.SHIMMER_PEDESTAL)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_PEDESTAL, PylonKeys.SHIMMER_PEDESTAL);
        PylonPages.SIMPLE_MACHINES.addItem(SHIMMER_PEDESTAL);
    }

    public static final ItemStack SHIMMER_ALTAR = ItemStackBuilder.rebar(Material.SMOOTH_STONE_SLAB, PylonKeys.SHIMMER_ALTAR)
            .build();
    static {
        RebarItem.register(RebarItem.class, SHIMMER_ALTAR, PylonKeys.SHIMMER_ALTAR);
        PylonPages.SIMPLE_MACHINES.addItem(SHIMMER_ALTAR);
    }

    public static final ItemStack COLLIMATOR = ItemStackBuilder.rebar(Material.OBSIDIAN, PylonKeys.COLLIMATOR)
            .build();
    static {
        RebarItem.register(Collimator.Item.class, COLLIMATOR, PylonKeys.COLLIMATOR);
        PylonPages.SIMPLE_MACHINES.addItem(COLLIMATOR);
    }

    public static final ItemStack COLLIMATOR_PILLAR = ItemStackBuilder.rebar(Material.DEEPSLATE_TILE_WALL, PylonKeys.COLLIMATOR_PILLAR)
            .build();
    static {
        RebarItem.register(RebarItem.class, COLLIMATOR_PILLAR, PylonKeys.COLLIMATOR_PILLAR);
        PylonPages.SIMPLE_MACHINES.addItem(COLLIMATOR_PILLAR);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Smelting" defaultstate=collapsed>
    public static final ItemStack PIT_KILN = ItemStackBuilder.rebar(Material.DECORATED_POT, PylonKeys.PIT_KILN)
            .build();
    static {
        RebarItem.register(PitKiln.Item.class, PIT_KILN, PylonKeys.PIT_KILN);
        PylonPages.SMELTING.addItem(PIT_KILN);
    }

    public static final ItemStack BLOOMERY = ItemStackBuilder.rebar(Material.MAGMA_BLOCK, PylonKeys.BLOOMERY)
            .build();
    static {
        RebarItem.register(RebarItem.class, BLOOMERY, PylonKeys.BLOOMERY);
        PylonPages.SMELTING.addItem(BLOOMERY);
    }

    public static final ItemStack BRONZE_ANVIL = ItemStackBuilder.rebar(Material.ANVIL, PylonKeys.BRONZE_ANVIL)
            .build();
    static {
        RebarItem.register(RebarItem.class, BRONZE_ANVIL, PylonKeys.BRONZE_ANVIL);
        PylonPages.SMELTING.addItem(BRONZE_ANVIL);
    }

    public static final ItemStack SMELTERY_CONTROLLER = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.SMELTERY_CONTROLLER)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_CONTROLLER, PylonKeys.SMELTERY_CONTROLLER);
        PylonPages.SMELTING.addItem(SMELTERY_CONTROLLER);
    }

    public static final ItemStack SMELTERY_INPUT_HATCH = ItemStackBuilder.rebar(Material.LIGHT_BLUE_TERRACOTTA, PylonKeys.SMELTERY_INPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_INPUT_HATCH, PylonKeys.SMELTERY_INPUT_HATCH);
        PylonPages.SMELTING.addItem(SMELTERY_INPUT_HATCH);
    }

    public static final ItemStack SMELTERY_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.ORANGE_TERRACOTTA, PylonKeys.SMELTERY_OUTPUT_HATCH)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_OUTPUT_HATCH, PylonKeys.SMELTERY_OUTPUT_HATCH);
        PylonPages.SMELTING.addItem(SMELTERY_OUTPUT_HATCH);
    }

    public static final ItemStack SMELTERY_HOPPER = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.SMELTERY_HOPPER)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_HOPPER, PylonKeys.SMELTERY_HOPPER);
        PylonPages.SMELTING.addItem(SMELTERY_HOPPER);
    }

    public static final ItemStack SMELTERY_CASTER = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.SMELTERY_CASTER)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_CASTER, PylonKeys.SMELTERY_CASTER);
        PylonPages.SMELTING.addItem(SMELTERY_CASTER);
    }

    public static final ItemStack SMELTERY_BURNER = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.SMELTERY_BURNER)
            .build();
    static {
        RebarItem.register(RebarItem.class, SMELTERY_BURNER, PylonKeys.SMELTERY_BURNER);
        PylonPages.SMELTING.addItem(SMELTERY_BURNER);
    }

    public static final ItemStack DIESEL_SMELTERY_HEATER = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.DIESEL_SMELTERY_HEATER)
            .build();
    static {
        RebarItem.register(DieselSmelteryHeater.Item.class, DIESEL_SMELTERY_HEATER, PylonKeys.DIESEL_SMELTERY_HEATER);
        PylonPages.SMELTING.addItem(DIESEL_SMELTERY_HEATER);
    }


    //</editor-fold>

    //<editor-fold desc="Machines - Fluid Pipes and Tanks" defaultstate=collapsed>

    public static final ItemStack FLUID_PIPE_WOOD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_WOOD)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_WOOD).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_WOOD);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_WOOD);
    }

    public static final ItemStack FLUID_PIPE_COPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_COPPER)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_COPPER).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_COPPER);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_COPPER);
    }

    public static final ItemStack FLUID_PIPE_TIN = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_TIN)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_TIN).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_TIN);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_TIN);
    }

    public static final ItemStack FLUID_PIPE_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_IRON)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_IRON).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_IRON);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_IRON);
    }

    public static final ItemStack FLUID_PIPE_BRONZE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_BRONZE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_BRONZE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_BRONZE);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_BRONZE);
    }

    public static final ItemStack FLUID_PIPE_IGNEOUS_COMPOSITE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_IGNEOUS_COMPOSITE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_IGNEOUS_COMPOSITE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_IGNEOUS_COMPOSITE);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_IGNEOUS_COMPOSITE);
    }

    public static final ItemStack FLUID_PIPE_STEEL = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_STEEL)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_STEEL).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_STEEL);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_STEEL);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_WOOD
            = ItemStackBuilder.rebar(Material.BROWN_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_WOOD)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_WOOD,
                PylonKeys.PORTABLE_FLUID_TANK_WOOD
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_WOOD);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_COPPER
            = ItemStackBuilder.rebar(Material.ORANGE_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_COPPER)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_COPPER,
                PylonKeys.PORTABLE_FLUID_TANK_COPPER
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_COPPER);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_TIN
            = ItemStackBuilder.rebar(Material.GREEN_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_TIN)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_TIN,
                PylonKeys.PORTABLE_FLUID_TANK_TIN
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_TIN);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_IRON
            = ItemStackBuilder.rebar(Material.LIGHT_GRAY_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_IRON)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_IRON,
                PylonKeys.PORTABLE_FLUID_TANK_IRON
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_IRON);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_BRONZE
            = ItemStackBuilder.rebar(Material.ORANGE_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_BRONZE)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_BRONZE,
                PylonKeys.PORTABLE_FLUID_TANK_BRONZE
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_BRONZE);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE
            = ItemStackBuilder.rebar(Material.BLACK_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE,
                PylonKeys.PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_STEEL
            = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_STEEL)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .build();
    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_STEEL,
                PylonKeys.PORTABLE_FLUID_TANK_STEEL
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_STEEL);
    }

    public static final ItemStack FLUID_TANK
            = ItemStackBuilder.rebar(Material.GRAY_TERRACOTTA, PylonKeys.FLUID_TANK)
            .build();
    static {
        RebarItem.register(FluidTank.Item.class, FLUID_TANK, PylonKeys.FLUID_TANK);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK);
    }

    public static final ItemStack FLUID_TANK_CASING_WOOD
            = ItemStackBuilder.rebar(Material.BROWN_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_WOOD)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_WOOD, PylonKeys.FLUID_TANK_CASING_WOOD);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_WOOD);
    }

    public static final ItemStack FLUID_TANK_CASING_COPPER
            = ItemStackBuilder.rebar(Material.ORANGE_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_COPPER)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_COPPER, PylonKeys.FLUID_TANK_CASING_COPPER);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_COPPER);
    }

    public static final ItemStack FLUID_TANK_CASING_TIN
            = ItemStackBuilder.rebar(Material.GREEN_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_TIN)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_TIN, PylonKeys.FLUID_TANK_CASING_TIN);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_TIN);
    }

    public static final ItemStack FLUID_TANK_CASING_IRON
            = ItemStackBuilder.rebar(Material.LIGHT_GRAY_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_IRON)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_IRON, PylonKeys.FLUID_TANK_CASING_IRON);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_IRON);
    }

    public static final ItemStack FLUID_TANK_CASING_BRONZE
            = ItemStackBuilder.rebar(Material.ORANGE_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_BRONZE)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_BRONZE, PylonKeys.FLUID_TANK_CASING_BRONZE);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_BRONZE);
    }

    public static final ItemStack FLUID_TANK_CASING_IGNEOUS_COMPOSITE
            = ItemStackBuilder.rebar(Material.BLACK_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_IGNEOUS_COMPOSITE)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_IGNEOUS_COMPOSITE, PylonKeys.FLUID_TANK_CASING_IGNEOUS_COMPOSITE);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_IGNEOUS_COMPOSITE);
    }

    public static final ItemStack FLUID_TANK_CASING_STEEL
            = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_STEEL)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_STEEL, PylonKeys.FLUID_TANK_CASING_STEEL);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_STEEL);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Fluid Machines" defaultstate=collapsed>

    public static final ItemStack WATER_PUMP = ItemStackBuilder.rebar(Material.BLUE_TERRACOTTA, PylonKeys.WATER_PUMP)
            .build();
    static {
        RebarItem.register(WaterPump.Item.class, WATER_PUMP, PylonKeys.WATER_PUMP);
        PylonPages.FLUID_MACHINES.addItem(WATER_PUMP);
    }

    public static final ItemStack FLUID_VALVE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_VALVE)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(FluidValve.Item.class, FLUID_VALVE, PylonKeys.FLUID_VALVE);
        PylonPages.FLUID_MACHINES.addItem(FLUID_VALVE);
    }

    public static final ItemStack FLUID_FILTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_FILTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(FluidFilter.Item.class, FLUID_FILTER, PylonKeys.FLUID_FILTER);
        PylonPages.FLUID_MACHINES.addItem(FLUID_FILTER);
    }

    public static final ItemStack FLUID_LIMITER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_LIMITER)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(FluidLimiter.Item.class, FLUID_LIMITER, PylonKeys.FLUID_LIMITER);
        PylonPages.FLUID_MACHINES.addItem(FLUID_LIMITER);
    }

    public static final ItemStack FLUID_METER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_METER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_BLUE_STAINED_GLASS.getKey())
            .build();
    static {
        RebarItem.register(FluidMeter.Item.class, FLUID_METER, PylonKeys.FLUID_METER);
        PylonPages.FLUID_MACHINES.addItem(FLUID_METER);
    }

    public static final ItemStack FLUID_ACCUMULATOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_ACCUMULATOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(FluidAccumulator.Item.class, FLUID_ACCUMULATOR, PylonKeys.FLUID_ACCUMULATOR);
        PylonPages.FLUID_MACHINES.addItem(FLUID_ACCUMULATOR);
    }

    public static final ItemStack WATER_PLACER = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.WATER_PLACER)
            .build();
    static {
        RebarItem.register(FluidPlacer.Item.class, WATER_PLACER, PylonKeys.WATER_PLACER);
        PylonPages.FLUID_MACHINES.addItem(WATER_PLACER);
    }

    public static final ItemStack LAVA_PLACER = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.LAVA_PLACER)
            .build();
    static {
        RebarItem.register(FluidPlacer.Item.class, LAVA_PLACER, PylonKeys.LAVA_PLACER);
        PylonPages.FLUID_MACHINES.addItem(LAVA_PLACER);
    }

    public static final ItemStack WATER_DRAINER = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.WATER_DRAINER)
            .build();
    static {
        RebarItem.register(FluidDrainer.Item.class, WATER_DRAINER, PylonKeys.WATER_DRAINER);
        PylonPages.FLUID_MACHINES.addItem(WATER_DRAINER);
    }

    public static final ItemStack LAVA_DRAINER = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.LAVA_DRAINER)
            .build();
    static {
        RebarItem.register(FluidDrainer.Item.class, LAVA_DRAINER, PylonKeys.LAVA_DRAINER);
        PylonPages.FLUID_MACHINES.addItem(LAVA_DRAINER);
    }

    public static final ItemStack FLUID_VOIDER_1 = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_VOIDER_1)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLACK_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(FluidVoider.Item.class, FLUID_VOIDER_1, PylonKeys.FLUID_VOIDER_1);
        PylonPages.FLUID_MACHINES.addItem(FLUID_VOIDER_1);
    }

    public static final ItemStack FLUID_VOIDER_2 = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_VOIDER_2)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLACK_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(FluidVoider.Item.class, FLUID_VOIDER_2, PylonKeys.FLUID_VOIDER_2);
        PylonPages.FLUID_MACHINES.addItem(FLUID_VOIDER_2);
    }

    public static final ItemStack FLUID_VOIDER_3 = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.FLUID_VOIDER_3)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLACK_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(FluidVoider.Item.class, FLUID_VOIDER_3, PylonKeys.FLUID_VOIDER_3);
        PylonPages.FLUID_MACHINES.addItem(FLUID_VOIDER_3);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Hydraulic Machines" defaultstate=collapsed>

    public static final ItemStack HYDRAULIC_GRINDSTONE_TURNER = ItemStackBuilder.rebar(Material.SMOOTH_STONE, PylonKeys.HYDRAULIC_GRINDSTONE_TURNER)
            .build();
    static {
        RebarItem.register(HydraulicGrindstoneTurner.Item.class, HYDRAULIC_GRINDSTONE_TURNER, PylonKeys.HYDRAULIC_GRINDSTONE_TURNER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_GRINDSTONE_TURNER);
    }

    public static final ItemStack HYDRAULIC_MIXING_ATTACHMENT = ItemStackBuilder.rebar(Material.CHISELED_STONE_BRICKS, PylonKeys.HYDRAULIC_MIXING_ATTACHMENT)
            .build();
    static {
        RebarItem.register(HydraulicMixingAttachment.Item.class, HYDRAULIC_MIXING_ATTACHMENT, PylonKeys.HYDRAULIC_MIXING_ATTACHMENT);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_MIXING_ATTACHMENT);
    }

    public static final ItemStack HYDRAULIC_PRESS_PISTON = ItemStackBuilder.rebar(Material.BROWN_TERRACOTTA, PylonKeys.HYDRAULIC_PRESS_PISTON)
            .build();
    static {
        RebarItem.register(HydraulicPressPiston.Item.class, HYDRAULIC_PRESS_PISTON, PylonKeys.HYDRAULIC_PRESS_PISTON);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_PRESS_PISTON);
    }

    public static final ItemStack HYDRAULIC_HAMMER_HEAD = ItemStackBuilder.rebar(Material.STONE_BRICKS, PylonKeys.HYDRAULIC_HAMMER_HEAD)
            .build();
    static {
        RebarItem.register(HydraulicHammerHead.Item.class, HYDRAULIC_HAMMER_HEAD, PylonKeys.HYDRAULIC_HAMMER_HEAD);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_HAMMER_HEAD);
    }

    public static final ItemStack HYDRAULIC_PIPE_BENDER = ItemStackBuilder.rebar(Material.WAXED_CHISELED_COPPER, PylonKeys.HYDRAULIC_PIPE_BENDER)
            .build();
    static {
        RebarItem.register(HydraulicPipeBender.Item.class, HYDRAULIC_PIPE_BENDER, PylonKeys.HYDRAULIC_PIPE_BENDER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_PIPE_BENDER);
    }

    public static final ItemStack HYDRAULIC_TABLE_SAW = ItemStackBuilder.rebar(Material.WAXED_CUT_COPPER, PylonKeys.HYDRAULIC_TABLE_SAW)
            .build();
    static {
        RebarItem.register(HydraulicTableSaw.Item.class, HYDRAULIC_TABLE_SAW, PylonKeys.HYDRAULIC_TABLE_SAW);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_TABLE_SAW);
    }

    public static final ItemStack HYDRAULIC_FARMER = ItemStackBuilder.rebar(Material.WAXED_EXPOSED_COPPER_BULB, PylonKeys.HYDRAULIC_FARMER)
            .build();
    static {
        RebarItem.register(HydraulicFarmer.Item.class, HYDRAULIC_FARMER, PylonKeys.HYDRAULIC_FARMER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_FARMER);
    }

    public static final ItemStack HYDRAULIC_MINER = ItemStackBuilder.rebar(Material.WAXED_EXPOSED_CHISELED_COPPER, PylonKeys.HYDRAULIC_MINER)
            .build();
    static {
        RebarItem.register(HydraulicMiner.Item.class, HYDRAULIC_MINER, PylonKeys.HYDRAULIC_MINER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_MINER);
    }

    public static final ItemStack HYDRAULIC_BREAKER = ItemStackBuilder.rebar(Material.WAXED_EXPOSED_CUT_COPPER, PylonKeys.HYDRAULIC_BREAKER)
            .build();
    static {
        RebarItem.register(HydraulicBreaker.Item.class, HYDRAULIC_BREAKER, PylonKeys.HYDRAULIC_BREAKER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_BREAKER);
    }

    public static final ItemStack HYDRAULIC_REFUELING_STATION = ItemStackBuilder.rebar(Material.WAXED_CUT_COPPER_SLAB, PylonKeys.HYDRAULIC_REFUELING_STATION)
            .build();
    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_REFUELING_STATION, PylonKeys.HYDRAULIC_REFUELING_STATION);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_REFUELING_STATION);
    }

    public static final ItemStack HYDRAULIC_CORE_DRILL = ItemStackBuilder.rebar(Material.WAXED_COPPER_BULB, PylonKeys.HYDRAULIC_CORE_DRILL)
            .build();
    static {
        RebarItem.register(HydraulicCoreDrill.Item.class, HYDRAULIC_CORE_DRILL, PylonKeys.HYDRAULIC_CORE_DRILL);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_CORE_DRILL);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Hydraulic Purification" defaultstate="collapsed">

    public static final ItemStack COAL_FIRED_PURIFICATION_TOWER = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.COAL_FIRED_PURIFICATION_TOWER)
            .build();
    static {
        RebarItem.register(CoalFiredPurificationTower.Item.class, COAL_FIRED_PURIFICATION_TOWER, PylonKeys.COAL_FIRED_PURIFICATION_TOWER);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(COAL_FIRED_PURIFICATION_TOWER);
    }

    public static final ItemStack SOLAR_PURIFICATION_TOWER_1 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_PURIFICATION_TOWER_1)
            .build();
    static {
        RebarItem.register(SolarPurificationTower.Item.class, SOLAR_PURIFICATION_TOWER_1, PylonKeys.SOLAR_PURIFICATION_TOWER_1);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_PURIFICATION_TOWER_1);
    }

    public static final ItemStack SOLAR_PURIFICATION_TOWER_2 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_PURIFICATION_TOWER_2)
            .build();
    static {
        RebarItem.register(SolarPurificationTower.Item.class, SOLAR_PURIFICATION_TOWER_2, PylonKeys.SOLAR_PURIFICATION_TOWER_2);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_PURIFICATION_TOWER_2);
    }

    public static final ItemStack SOLAR_PURIFICATION_TOWER_3 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_PURIFICATION_TOWER_3)
            .build();
    static {
        RebarItem.register(SolarPurificationTower.Item.class, SOLAR_PURIFICATION_TOWER_3, PylonKeys.SOLAR_PURIFICATION_TOWER_3);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_PURIFICATION_TOWER_3);
    }

    public static final ItemStack SOLAR_PURIFICATION_TOWER_4 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_PURIFICATION_TOWER_4)
            .build();
    static {
        RebarItem.register(SolarPurificationTower.Item.class, SOLAR_PURIFICATION_TOWER_4, PylonKeys.SOLAR_PURIFICATION_TOWER_4);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_PURIFICATION_TOWER_4);
    }

    public static final ItemStack SOLAR_PURIFICATION_TOWER_5 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_PURIFICATION_TOWER_5)
            .build();
    static {
        RebarItem.register(SolarPurificationTower.Item.class, SOLAR_PURIFICATION_TOWER_5, PylonKeys.SOLAR_PURIFICATION_TOWER_5);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_PURIFICATION_TOWER_5);
    }

    public static final ItemStack SOLAR_LENS = ItemStackBuilder.rebar(Material.GLASS_PANE, PylonKeys.SOLAR_LENS)
            .build();
    static {
        RebarItem.register(RebarItem.class, SOLAR_LENS, PylonKeys.SOLAR_LENS);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_LENS);
    }

    public static final ItemStack PURIFICATION_TOWER_GLASS = ItemStackBuilder.rebar(Material.LIGHT_GRAY_STAINED_GLASS, PylonKeys.PURIFICATION_TOWER_GLASS)
            .build();
    static {
        RebarItem.register(RebarItem.class, PURIFICATION_TOWER_GLASS, PylonKeys.PURIFICATION_TOWER_GLASS);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(PURIFICATION_TOWER_GLASS);
    }

    public static final ItemStack PURIFICATION_TOWER_CAP = ItemStackBuilder.rebar(Material.QUARTZ_SLAB, PylonKeys.PURIFICATION_TOWER_CAP)
            .build();
    static {
        RebarItem.register(RebarItem.class, PURIFICATION_TOWER_CAP, PylonKeys.PURIFICATION_TOWER_CAP);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(PURIFICATION_TOWER_CAP);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Cargo" defaultstate="collapsed">

    public static final ItemStack CARGO_DUCT = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_DUCT)
            .set(DataComponentTypes.ITEM_MODEL, Material.GRAY_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CARGO_DUCT, PylonKeys.CARGO_DUCT);
        PylonPages.CARGO.addItem(CARGO_DUCT);
    }

    public static final ItemStack CARGO_EXTRACTOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_EXTRACTOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.RED_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(CargoExtractor.Item.class, CARGO_EXTRACTOR, PylonKeys.CARGO_EXTRACTOR);
        PylonPages.CARGO.addItem(CARGO_EXTRACTOR);
    }

    public static final ItemStack CARGO_INSERTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_INSERTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIME_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(CargoInserter.Item.class, CARGO_INSERTER, PylonKeys.CARGO_INSERTER);
        PylonPages.CARGO.addItem(CARGO_INSERTER);
    }

    public static final ItemStack CARGO_VALVE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_VALVE)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(CargoValve.Item.class, CARGO_VALVE, PylonKeys.CARGO_VALVE);
        PylonPages.CARGO.addItem(CARGO_VALVE);
    }

    public static final ItemStack CARGO_BUFFER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_BUFFER)
            .set(DataComponentTypes.ITEM_MODEL, Material.BARREL.getKey())
            .build();
    static {
        RebarItem.register(CargoBuffer.Item.class, CARGO_BUFFER, PylonKeys.CARGO_BUFFER);
        PylonPages.CARGO.addItem(CARGO_BUFFER);
    }

    public static final ItemStack CARGO_MONITOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_MONITOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_STAINED_GLASS.getKey())
            .build();
    static {
        RebarItem.register(CargoMonitor.Item.class, CARGO_MONITOR, PylonKeys.CARGO_MONITOR);
        PylonPages.CARGO.addItem(CARGO_MONITOR);
    }

    public static final ItemStack CARGO_METER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_METER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_BLUE_STAINED_GLASS.getKey())
            .build();
    static {
        RebarItem.register(CargoMeter.Item.class, CARGO_METER, PylonKeys.CARGO_METER);
        PylonPages.CARGO.addItem(CARGO_METER);
    }

    public static final ItemStack CARGO_SPLITTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_SPLITTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.STRIPPED_CRIMSON_STEM.getKey())
            .build();
    static {
        RebarItem.register(CargoSplitter.Item.class, CARGO_SPLITTER, PylonKeys.CARGO_SPLITTER);
        PylonPages.CARGO.addItem(CARGO_SPLITTER);
    }

    public static final ItemStack CARGO_MERGER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_MERGER)
            .set(DataComponentTypes.ITEM_MODEL, Material.STRIPPED_WARPED_STEM.getKey())
            .build();
    static {
        RebarItem.register(CargoSplitter.Item.class, CARGO_MERGER, PylonKeys.CARGO_MERGER);
        PylonPages.CARGO.addItem(CARGO_MERGER);
    }

    public static final ItemStack CARGO_FILTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_FILTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPARATOR.getKey())
            .build();
    static {
        RebarItem.register(CargoValve.Item.class, CARGO_FILTER, PylonKeys.CARGO_FILTER);
        PylonPages.CARGO.addItem(CARGO_FILTER);
    }

    public static final ItemStack CARGO_OVERFLOW_GATE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_OVERFLOW_GATE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRIMSON_STEM.getKey())
            .build();
    static {
        RebarItem.register(CargoOverflowGate.Item.class, CARGO_OVERFLOW_GATE, PylonKeys.CARGO_OVERFLOW_GATE);
        PylonPages.CARGO.addItem(CARGO_OVERFLOW_GATE);
    }

    public static final ItemStack CARGO_GATE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_GATE)
            .set(DataComponentTypes.ITEM_MODEL, Material.REPEATER.getKey())
            .build();
    static {
        RebarItem.register(CargoGate.Item.class, CARGO_GATE, PylonKeys.CARGO_GATE);
        PylonPages.CARGO.addItem(CARGO_GATE);
    }

    public static final ItemStack CARGO_ACCUMULATOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_ACCUMULATOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.REDSTONE_LAMP.getKey())
            .build();
    static {
        RebarItem.register(CargoAccumulator.Item.class, CARGO_ACCUMULATOR, PylonKeys.CARGO_ACCUMULATOR);
        PylonPages.CARGO.addItem(CARGO_ACCUMULATOR);
    }

    public static final ItemStack CARGO_FLUID_ACCUMULATOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_FLUID_ACCUMULATOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.NOTE_BLOCK.getKey())
            .build();
    static {
        RebarItem.register(CargoFluidAccumulator.Item.class, CARGO_FLUID_ACCUMULATOR, PylonKeys.CARGO_FLUID_ACCUMULATOR);
        PylonPages.CARGO.addItem(CARGO_FLUID_ACCUMULATOR);
    }
    //</editor-fold>

    //<editor-fold desc="Machines - Diesel Machines" defaultstate="collapsed">

    public static final ItemStack DIESEL_GRINDSTONE = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_GRINDSTONE)
            .set(DataComponentTypes.ITEM_MODEL, Material.SMOOTH_STONE_SLAB.getKey())
            .build();
    static {
        RebarItem.register(DieselGrindstone.Item.class, DIESEL_GRINDSTONE, PylonKeys.DIESEL_GRINDSTONE);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_GRINDSTONE);
    }

    public static final ItemStack DIESEL_MIXING_ATTACHMENT = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_MIXING_ATTACHMENT)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_GRAY_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(DieselMixingAttachment.Item.class, DIESEL_MIXING_ATTACHMENT, PylonKeys.DIESEL_MIXING_ATTACHMENT);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_MIXING_ATTACHMENT);
    }

    public static final ItemStack DIESEL_PRESS = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_PRESS)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPOSTER.getKey())
            .build();
    static {
        RebarItem.register(DieselPress.Item.class, DIESEL_PRESS, PylonKeys.DIESEL_PRESS);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_PRESS);
    }

    public static final ItemStack DIESEL_HAMMER_HEAD = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_HAMMER_HEAD)
            .set(DataComponentTypes.ITEM_MODEL, Material.GRAY_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(DieselHammerHead.Item.class, DIESEL_HAMMER_HEAD, PylonKeys.DIESEL_HAMMER_HEAD);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_HAMMER_HEAD);
    }

    public static final ItemStack DIESEL_PIPE_BENDER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_PIPE_BENDER)
            .build();
    static {
        RebarItem.register(DieselPipeBender.Item.class, DIESEL_PIPE_BENDER, PylonKeys.DIESEL_PIPE_BENDER);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_PIPE_BENDER);
    }

    public static final ItemStack DIESEL_TABLE_SAW = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_TABLE_SAW)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_BARS.getKey())
            .build();
    static {
        RebarItem.register(DieselTableSaw.Item.class, DIESEL_TABLE_SAW, PylonKeys.DIESEL_TABLE_SAW);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_TABLE_SAW);
    }

    public static final ItemStack DIESEL_MINER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_MINER)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(DieselMiner.Item.class, DIESEL_MINER, PylonKeys.DIESEL_MINER);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_MINER);
    }

    public static final ItemStack DIESEL_BREAKER = ItemStackBuilder.rebar(Material.DROPPER, PylonKeys.DIESEL_BREAKER)
            .build();
    static {
        RebarItem.register(DieselBreaker.Item.class, DIESEL_BREAKER, PylonKeys.DIESEL_BREAKER);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_BREAKER);
    }

    public static final ItemStack DIESEL_FURNACE = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.DIESEL_FURNACE)
            .build();
    static {
        RebarItem.register(DieselFurnace.Item.class, DIESEL_FURNACE, PylonKeys.DIESEL_FURNACE);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_FURNACE);
    }

    public static final ItemStack DIESEL_BRICK_MOLDER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_BRICK_MOLDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.OAK_PLANKS.getKey())
            .build();
    static {
        RebarItem.register(DieselBrickMolder.Item.class, DIESEL_BRICK_MOLDER, PylonKeys.DIESEL_BRICK_MOLDER);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_BRICK_MOLDER);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Diesel Production" defaultstate="collapsed">

    public static final ItemStack FERMENTER = ItemStackBuilder.rebar(Material.PINK_TERRACOTTA, PylonKeys.FERMENTER)
            .build();
    static {
        RebarItem.register(Fermenter.Item.class, FERMENTER, PylonKeys.FERMENTER);
        PylonPages.DIESEL_PRODUCTION.addItem(FERMENTER);
    }

    public static final ItemStack FERMENTER_CORE = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS, PylonKeys.FERMENTER_CORE)
            .build();
    static {
        RebarItem.register(RebarItem.class, FERMENTER_CORE, PylonKeys.FERMENTER_CORE);
        PylonPages.DIESEL_PRODUCTION.addItem(FERMENTER_CORE);
    }

    public static final ItemStack FERMENTER_CASING = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS_PANE, PylonKeys.FERMENTER_CASING)
            .build();
    static {
        RebarItem.register(RebarItem.class, FERMENTER_CASING, PylonKeys.FERMENTER_CASING);
        PylonPages.DIESEL_PRODUCTION.addItem(FERMENTER_CASING);
    }

    public static final ItemStack BIOREFINERY = ItemStackBuilder.rebar(Material.PURPLE_TERRACOTTA, PylonKeys.BIOREFINERY)
            .build();
    static {
        RebarItem.register(Biorefinery.Item.class, BIOREFINERY, PylonKeys.BIOREFINERY);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY);
    }

    public static final ItemStack BIOREFINERY_FOUNDATION = ItemStackBuilder.rebar(Material.LIGHT_GRAY_CONCRETE, PylonKeys.BIOREFINERY_FOUNDATION)
            .build();
    static {
        RebarItem.register(RebarItem.class, BIOREFINERY_FOUNDATION, PylonKeys.BIOREFINERY_FOUNDATION);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY_FOUNDATION);
    }

    public static final ItemStack BIOREFINERY_PLATING = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS_PANE, PylonKeys.BIOREFINERY_PLATING)
            .build();
    static {
        RebarItem.register(RebarItem.class, BIOREFINERY_PLATING, PylonKeys.BIOREFINERY_PLATING);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY_PLATING);
    }

    public static final ItemStack BIOREFINERY_TOWER_RING = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.BIOREFINERY_TOWER_RING)
            .build();
    static {
        RebarItem.register(RebarItem.class, BIOREFINERY_TOWER_RING, PylonKeys.BIOREFINERY_TOWER_RING);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY_TOWER_RING);
    }

    public static final ItemStack BIOREFINERY_SMOKESTACK_RING = ItemStackBuilder.rebar(Material.POLISHED_DEEPSLATE_WALL, PylonKeys.BIOREFINERY_SMOKESTACK_RING)
            .build();
    static {
        RebarItem.register(RebarItem.class, BIOREFINERY_SMOKESTACK_RING, PylonKeys.BIOREFINERY_SMOKESTACK_RING);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY_SMOKESTACK_RING);
    }

    public static final ItemStack BIOREFINERY_SMOKESTACK_CAP = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.BIOREFINERY_SMOKESTACK_CAP)
            .build();
    static {
        RebarItem.register(RebarItem.class, BIOREFINERY_SMOKESTACK_CAP, PylonKeys.BIOREFINERY_SMOKESTACK_CAP);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY_SMOKESTACK_CAP);
    }

    //</editor-fold>

    //<editor-fold desc="Assembling" defaultstate=collapsed>

    public static final ItemStack ASSEMBLY_TABLE = ItemStackBuilder.rebar(Material.ANVIL, PylonKeys.ASSEMBLY_TABLE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRAFTING_TABLE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, ASSEMBLY_TABLE, PylonKeys.ASSEMBLY_TABLE);
        PylonPages.ASSEMBLING.addItem(ASSEMBLY_TABLE);
    }

    public static final ItemStack COPPER_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COPPER_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.COPPER_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            Settings.get(PylonKeys.COPPER_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();
    static {
        RebarItem.register(Screwdriver.class, COPPER_SCREWDRIVER, PylonKeys.COPPER_SCREWDRIVER);
        PylonPages.ASSEMBLING.addItem(COPPER_SCREWDRIVER);
    }

    public static final ItemStack IRON_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.IRON_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.IRON_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            Settings.get(PylonKeys.IRON_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();
    static {
        RebarItem.register(Screwdriver.class, IRON_SCREWDRIVER, PylonKeys.IRON_SCREWDRIVER);
        PylonPages.ASSEMBLING.addItem(IRON_SCREWDRIVER);
    }

    public static final ItemStack BRONZE_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRONZE_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.BRONZE_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            Settings.get(PylonKeys.BRONZE_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();
    static {
        RebarItem.register(Screwdriver.class, BRONZE_SCREWDRIVER, PylonKeys.BRONZE_SCREWDRIVER);
        PylonPages.ASSEMBLING.addItem(BRONZE_SCREWDRIVER);
    }

    public static final ItemStack STEEL_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.STEEL_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.STEEL_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            Settings.get(PylonKeys.STEEL_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();
    static {
        RebarItem.register(Screwdriver.class, STEEL_SCREWDRIVER, PylonKeys.STEEL_SCREWDRIVER);
        PylonPages.ASSEMBLING.addItem(STEEL_SCREWDRIVER);
    }

    public static final ItemStack REDSTONE_SOLDERING_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.REDSTONE_SOLDERING_IRON)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPASS.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.REDSTONE_SOLDERING_IRON).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            Settings.get(PylonKeys.REDSTONE_SOLDERING_IRON).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.REDSTONE_SOLDERING_IRON)
            )
            .build();
    static {
        RebarItem.register(RedstoneSolderingIron.class, REDSTONE_SOLDERING_IRON, PylonKeys.REDSTONE_SOLDERING_IRON);
        PylonPages.ASSEMBLING.addItem(REDSTONE_SOLDERING_IRON);
    }

    //</editor-fold>

    //<editor-fold desc="Creative Items" defaultstate=collapsed>

    public static final ItemStack CREATIVE_FLUID_VOIDER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_FLUID_VOIDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_CONCRETE.getKey())
            .build();
    static {
        RebarItem.register(FluidVoider.Item.class, CREATIVE_FLUID_VOIDER, PylonKeys.CREATIVE_FLUID_VOIDER);
        RebarGuide.hideItemUnlessAdmin(PylonKeys.CREATIVE_FLUID_VOIDER);
        PylonPages.CREATIVE_ITEMS.addItem(CREATIVE_FLUID_VOIDER);
    }

    public static final ItemStack CREATIVE_FLUID_SOURCE = ItemStackBuilder.rebar(Material.PINK_CONCRETE, PylonKeys.CREATIVE_FLUID_SOURCE)
            .build();
    static {
        RebarItem.register(RebarItem.class, CREATIVE_FLUID_SOURCE, PylonKeys.CREATIVE_FLUID_SOURCE);
        RebarGuide.hideItemUnlessAdmin(PylonKeys.CREATIVE_FLUID_SOURCE);
        PylonPages.CREATIVE_ITEMS.addItem(CREATIVE_FLUID_SOURCE);
    }

    public static final ItemStack FLUID_PIPE_CREATIVE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_CREATIVE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    Settings.get(PylonKeys.FLUID_PIPE_CREATIVE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_CREATIVE, PylonKeys.FLUID_PIPE_CREATIVE);
        RebarGuide.hideItemUnlessAdmin(PylonKeys.CREATIVE_FLUID_SOURCE);
        PylonPages.CREATIVE_ITEMS.addItem(FLUID_PIPE_CREATIVE);
    }

    public static final ItemStack CREATIVE_ITEM_SOURCE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_ITEM_SOURCE)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CREATIVE_ITEM_SOURCE, PylonKeys.CREATIVE_ITEM_SOURCE);
        RebarGuide.hideItem(PylonKeys.CREATIVE_ITEM_SOURCE);
        PylonPages.CREATIVE_ITEMS.addItem(CREATIVE_ITEM_SOURCE);
    }

    public static final ItemStack CREATIVE_ITEM_VOIDER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_ITEM_VOIDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_TERRACOTTA.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, CREATIVE_ITEM_VOIDER, PylonKeys.CREATIVE_ITEM_VOIDER);
        RebarGuide.hideItem(PylonKeys.CREATIVE_ITEM_VOIDER);
        PylonPages.CREATIVE_ITEMS.addItem(CREATIVE_ITEM_VOIDER);
    }

    //</editor-fold>

    public static final ItemStack CLEANSING_POTION = ItemStackBuilder.rebar(Material.SPLASH_POTION, PylonKeys.CLEANSING_POTION)
            .set(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                    .customColor(Color.FUCHSIA)
                    .build())
            .build();
    static {
        RebarItem.register(CleansingPotion.class, CLEANSING_POTION);
        PylonPages.TOOLS.addItem(CLEANSING_POTION);

        // This recipe isn't configured because we currently have no way to set the healing potion data on it
        ItemStack healingPotion = ItemStackBuilder.of(Material.SPLASH_POTION)
                .set(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                        .potion(PotionType.HEALING)
                        .build())
                .build();
        ShapelessRecipe recipe = new ShapelessRecipe(PylonKeys.CLEANSING_POTION, CLEANSING_POTION)
                .addIngredient(healingPotion)
                .addIngredient(DISINFECTANT);
        recipe.setCategory(CraftingBookCategory.MISC);
        RecipeType.VANILLA_SHAPELESS.addRecipe(recipe);
    }

    public static final ItemStack PALLADIUM_HELMET = ItemStackBuilder.rebar(Material.DIAMOND_HELMET, PylonKeys.PALLADIUM_HELMET)
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_HELMET).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.PROTECTION, Settings.get(PylonKeys.PALLADIUM_HELMET).getOrThrow("prot-level", ConfigAdapter.INTEGER))
                    .build())
            .addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    pylonKey("palladium_helmet_speed"),
                    Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("speed-percentage-increase", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.HEAD
            ))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_HELMET);
        PylonPages.ARMOUR.addItem(PALLADIUM_HELMET);
    }

    public static final ItemStack PALLADIUM_CHESTPLATE = ItemStackBuilder.rebar(Material.DIAMOND_CHESTPLATE, PylonKeys.PALLADIUM_CHESTPLATE)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.PROTECTION, Settings.get(PylonKeys.PALLADIUM_CHESTPLATE).getOrThrow("prot-level", ConfigAdapter.INTEGER))
                    .build())
            .addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    pylonKey("palladium_chestplate_speed"),
                    Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("speed-percentage-increase", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.CHEST
            ))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_CHESTPLATE);
        PylonPages.ARMOUR.addItem(PALLADIUM_CHESTPLATE);
    }

    public static final ItemStack PALLADIUM_LEGGINGS = ItemStackBuilder.rebar(Material.DIAMOND_LEGGINGS, PylonKeys.PALLADIUM_LEGGINGS)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.PROTECTION, Settings.get(PylonKeys.PALLADIUM_LEGGINGS).getOrThrow("prot-level", ConfigAdapter.INTEGER))
                    .build())
            .addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    pylonKey("palladium_leggings_speed"),
                    Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("speed-percentage-increase", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.LEGS
            ))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_LEGGINGS);
        PylonPages.ARMOUR.addItem(PALLADIUM_LEGGINGS);
    }

    public static final ItemStack PALLADIUM_BOOTS = ItemStackBuilder.rebarBoots(Material.DIAMOND_BOOTS, PylonKeys.PALLADIUM_BOOTS, true)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.PROTECTION, Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("prot-level", ConfigAdapter.INTEGER))
                    .add(Enchantment.FROST_WALKER, Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("frost-walker-level", ConfigAdapter.INTEGER))
                    .build())
            .addAttributeModifier(Attribute.MOVEMENT_SPEED, new AttributeModifier(
                    pylonKey("palladium_boots_speed"),
                    Settings.get(PylonKeys.PALLADIUM_BOOTS).getOrThrow("speed-percentage-increase", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.FEET
            ))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_BOOTS);
        PylonPages.ARMOUR.addItem(PALLADIUM_BOOTS);
    }

    public static final ItemStack PALLADIUM_SWORD = ItemStackBuilder.rebarWeapon(Material.DIAMOND_SWORD, PylonKeys.PALLADIUM_SWORD, true, true, false)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.SHARPNESS, Settings.get(PylonKeys.PALLADIUM_SWORD).getOrThrow("sharpness-level", ConfigAdapter.INTEGER))
                    .build()
            )
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_SWORD).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_SWORD);
        PylonPages.COMBAT.addItem(PALLADIUM_SWORD);
    }

    public static final ItemStack PALLADIUM_AXE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_AXE, PylonKeys.PALLADIUM_AXE, RebarUtils.axeMineable(), true, true, true)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.EFFICIENCY, Settings.get(PylonKeys.PALLADIUM_AXE).getOrThrow("efficiency-level", ConfigAdapter.INTEGER))
                    .build())
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_AXE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_AXE);
        PylonPages.TOOLS.addItem(PALLADIUM_AXE);
    }

    public static final ItemStack PALLADIUM_PICKAXE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_PICKAXE, PylonKeys.PALLADIUM_PICKAXE, RebarUtils.pickaxeMineable(), true, true, false)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.EFFICIENCY, Settings.get(PylonKeys.PALLADIUM_PICKAXE).getOrThrow("efficiency-level", ConfigAdapter.INTEGER))
                    .build())
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_PICKAXE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_PICKAXE);
        PylonPages.TOOLS.addItem(PALLADIUM_PICKAXE);
    }

    public static final ItemStack PALLADIUM_SHOVEL = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_SHOVEL, PylonKeys.PALLADIUM_SHOVEL, RebarUtils.shovelMineable(), true, true, false)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.EFFICIENCY, Settings.get(PylonKeys.PALLADIUM_SHOVEL).getOrThrow("efficiency-level", ConfigAdapter.INTEGER))
                    .build()
            )
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_SHOVEL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_SHOVEL);
        PylonPages.TOOLS.addItem(PALLADIUM_SHOVEL);
    }

    public static final ItemStack PALLADIUM_HOE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_HOE, PylonKeys.PALLADIUM_HOE, RebarUtils.hoeMineable(), true, true, false)
            .set(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments()
                    .add(Enchantment.EFFICIENCY, Settings.get(PylonKeys.PALLADIUM_HOE).getOrThrow("efficiency-level", ConfigAdapter.INTEGER))
                    .build())
            .set(DataComponentTypes.MAX_DAMAGE, Settings.get(PylonKeys.PALLADIUM_HOE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();
    static {
        RebarItem.register(RebarItem.class, PALLADIUM_HOE);
        PylonPages.TOOLS.addItem(PALLADIUM_HOE);
    }

    public static final ItemStack FLUID_PIPE_PALLADIUM = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_PALLADIUM)
            .set(DataComponentTypes.ITEM_MODEL, Settings.get(PylonKeys.FLUID_PIPE_PALLADIUM).getOrThrow("material", ConfigAdapter.MATERIAL).key())
            .build();
    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_PALLADIUM);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_PIPE_PALLADIUM);
    }

    public static final ItemStack FLUID_TANK_CASING_PALLADIUM = ItemStackBuilder.rebar(Material.BLUE_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_PALLADIUM)
            .build();
    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_PALLADIUM, PylonKeys.FLUID_TANK_CASING_PALLADIUM);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_PALLADIUM);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_PALLADIUM = ItemStackBuilder.rebar(Material.BLUE_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_PALLADIUM)
            .build();
    static {
        RebarItem.register(PortableFluidTank.Item.class, PORTABLE_FLUID_TANK_PALLADIUM, PylonKeys.PORTABLE_FLUID_TANK_PALLADIUM);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_PALLADIUM);
    }

    static {
        PylonPages.initialise();
    }

    // Calling this method forces all the static blocks to run, which initializes our items
    public static void initialize() {
    }
}
