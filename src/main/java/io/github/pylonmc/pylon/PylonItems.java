package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.armor.BronzeArmor;
import io.github.pylonmc.pylon.content.armor.SteelArmor;
import io.github.pylonmc.pylon.content.assembling.AssemblyTable;
import io.github.pylonmc.pylon.content.assembling.RedstoneSolderingIron;
import io.github.pylonmc.pylon.content.assembling.Screwdriver;
import io.github.pylonmc.pylon.content.building.Elevator;
import io.github.pylonmc.pylon.content.building.ExplosiveTarget;
import io.github.pylonmc.pylon.content.building.Immobilizer;
import io.github.pylonmc.pylon.content.building.Pedestal;
import io.github.pylonmc.pylon.content.building.WitherProofBlock;
import io.github.pylonmc.pylon.content.combat.BeheadingSword;
import io.github.pylonmc.pylon.content.combat.IceArrow;
import io.github.pylonmc.pylon.content.combat.ReactivatedWitherSkull;
import io.github.pylonmc.pylon.content.combat.RecoilArrow;
import io.github.pylonmc.pylon.content.components.FluidInputHatch;
import io.github.pylonmc.pylon.content.components.FluidOutputHatch;
import io.github.pylonmc.pylon.content.components.ItemInputHatch;
import io.github.pylonmc.pylon.content.components.ItemOutputHatch;
import io.github.pylonmc.pylon.content.components.LiseletteCollector;
import io.github.pylonmc.pylon.content.components.ReinforcedGlassCasing;
import io.github.pylonmc.pylon.content.machines.boiler.AbstractBoiler;
import io.github.pylonmc.pylon.content.machines.cargo.*;
import io.github.pylonmc.pylon.content.machines.diesel.machines.*;
import io.github.pylonmc.pylon.content.machines.diesel.production.Biorefinery;
import io.github.pylonmc.pylon.content.machines.diesel.production.Fermenter;
import io.github.pylonmc.pylon.content.machines.electricity.Capacitor;
import io.github.pylonmc.pylon.content.machines.electricity.generation.CombustionTower;
import io.github.pylonmc.pylon.content.machines.electricity.generation.CreativePowerSource;
import io.github.pylonmc.pylon.content.machines.electricity.generation.SteamEngine;
import io.github.pylonmc.pylon.content.machines.electricity.machines.*;
import io.github.pylonmc.pylon.content.machines.experience.ExperienceDrain;
import io.github.pylonmc.pylon.content.machines.experience.ExperienceFountain;
import io.github.pylonmc.pylon.content.machines.experience.FluidExperienceBottler;
import io.github.pylonmc.pylon.content.machines.experience.LiquidXPBottle;
import io.github.pylonmc.pylon.content.machines.fluid.*;
import io.github.pylonmc.pylon.content.machines.hydraulics.*;
import io.github.pylonmc.pylon.content.machines.petrochemicals.HydraulicFractureSeal;
import io.github.pylonmc.pylon.content.machines.petrochemicals.HydraulicFracturingDrill;
import io.github.pylonmc.pylon.content.machines.petrochemicals.HydraulicPumpjack;
import io.github.pylonmc.pylon.content.machines.petrochemicals.OilSampleDrill;
import io.github.pylonmc.pylon.content.machines.simple.*;
import io.github.pylonmc.pylon.content.machines.smelting.Bloomery;
import io.github.pylonmc.pylon.content.machines.smelting.BronzeAnvil;
import io.github.pylonmc.pylon.content.machines.smelting.CastingUnit;
import io.github.pylonmc.pylon.content.machines.smelting.DieselSmelteryHeater;
import io.github.pylonmc.pylon.content.machines.smelting.FormingTable;
import io.github.pylonmc.pylon.content.machines.smelting.Kiln;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryBurner;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryComponent;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryController;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryHopper;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryInputHatch;
import io.github.pylonmc.pylon.content.machines.smelting.SmelteryOutputHatch;
import io.github.pylonmc.pylon.content.machines.storage.Silo;
import io.github.pylonmc.pylon.content.machines.storage.SiloConverter;
import io.github.pylonmc.pylon.content.resources.CharcoalBlock;
import io.github.pylonmc.pylon.content.resources.IronBloom;
import io.github.pylonmc.pylon.content.resources.RefractoryMix;
import io.github.pylonmc.pylon.content.science.Loupe;
import io.github.pylonmc.pylon.content.science.ResearchPack;
import io.github.pylonmc.pylon.content.talismans.*;
import io.github.pylonmc.pylon.content.tools.*;
import io.github.pylonmc.pylon.guide.HydraulicRefuelableItemsPage;
import io.github.pylonmc.pylon.guide.LiquidFuelsPage;
import io.github.pylonmc.pylon.guide.PressableItemsPage;
import io.github.pylonmc.pylon.recipes.*;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.content.cargo.CargoDuct;
import io.github.pylonmc.rebar.content.fluid.FluidPipe;
import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.guide.button.MachineRecipesButton;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.keys.SoundEventKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

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
                    .consumeSeconds(ConfigSection.fromSettings(PylonKeys.LOUPE).getOrThrow("use-ticks", ConfigAdapter.INTEGER) / 20.0F)
                    .sound(SoundEventKeys.INTENTIONALLY_EMPTY)
            )
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.LOUPE).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F)
                    .cooldownGroup(PylonKeys.LOUPE)
            )
            .build();

    static {
        RebarItem.register(Loupe.class, LOUPE)
                .withGuidePage(PylonPages.SCIENCE);
    }

    public static final ItemStack RESEARCH_PACK_1 = ItemStackBuilder.rebar(Material.RED_BANNER, PylonKeys.RESEARCH_PACK_1)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();

    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_1)
                .withGuidePage(PylonPages.SCIENCE);
    }

    public static final ItemStack RESEARCH_PACK_2 = ItemStackBuilder.rebar(Material.LIME_BANNER, PylonKeys.RESEARCH_PACK_2)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();

    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_2)
                .withGuidePage(PylonPages.SCIENCE);
    }

    public static final ItemStack RESEARCH_PACK_3 = ItemStackBuilder.rebar(Material.GRAY_BANNER, PylonKeys.RESEARCH_PACK_3)
            .set(DataComponentTypes.MAX_STACK_SIZE, 3)
            .build();

    static {
        RebarItem.register(ResearchPack.class, RESEARCH_PACK_3)
                .withGuidePage(PylonPages.SCIENCE);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Metals" defaultstate=collapsed>

    public static final ItemStack COPPER_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COPPER_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, COPPER_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack CRUSHED_RAW_COPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_COPPER)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_COPPER)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack IRON_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.IRON_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, IRON_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack CRUSHED_RAW_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_IRON)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_IRON)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack GOLD_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.GOLD_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, GOLD_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack CRUSHED_RAW_GOLD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_GOLD)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_GOLD)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack RAW_TIN = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.RAW_TIN)
            .build();

    static {
        RebarItem.register(RebarItem.class, RAW_TIN)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack RAW_TIN_BLOCK = ItemStackBuilder.rebar(Material.RAW_IRON_BLOCK, PylonKeys.RAW_TIN_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, RAW_TIN_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack TIN_INGOT = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.TIN_INGOT)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_INGOT.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_INGOT)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack TIN_NUGGET = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.TIN_NUGGET)
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_NUGGET)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack TIN_BLOCK = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.TIN_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack CRUSHED_RAW_TIN = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CRUSHED_RAW_TIN)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUSHED_RAW_TIN)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack TIN_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.TIN_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack BRONZE_INGOT = ItemStackBuilder.rebar(Material.COPPER_INGOT, PylonKeys.BRONZE_INGOT)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_INGOT)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack BRONZE_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRONZE_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack BRONZE_NUGGET = ItemStackBuilder.rebar(Material.ARMADILLO_SCUTE, PylonKeys.BRONZE_NUGGET)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_NUGGET)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack BRONZE_BLOCK = ItemStackBuilder.rebar(Material.COPPER_BLOCK, PylonKeys.BRONZE_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack SPONGE_IRON = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.SPONGE_IRON)
            .build();

    static {
        RebarItem.register(RebarItem.class, SPONGE_IRON)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack IRON_BLOOM = ItemStackBuilder.rebar(Material.RAW_IRON, PylonKeys.IRON_BLOOM)
            .build();

    static {
        RebarItem.register(IronBloom.class, IRON_BLOOM)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack WROUGHT_IRON = ItemStackBuilder.rebar(Material.NETHERITE_SCRAP, PylonKeys.WROUGHT_IRON)
            .build();

    static {
        RebarItem.register(RebarItem.class, WROUGHT_IRON)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack STEEL_INGOT = ItemStackBuilder.rebar(Material.NETHERITE_INGOT, PylonKeys.STEEL_INGOT)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_INGOT)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack STEEL_NUGGET = ItemStackBuilder.rebar(Material.NETHERITE_SCRAP, PylonKeys.STEEL_NUGGET)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_NUGGET)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack STEEL_BLOCK = ItemStackBuilder.rebar(Material.NETHERITE_BLOCK, PylonKeys.STEEL_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.METALS);
    }

    public static final ItemStack STEEL_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.STEEL_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_DUST)
                .withGuidePage(PylonPages.METALS);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Core Chunks" defaultstate=collapsed>

    public static final ItemStack SHALLOW_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SHALLOW_CORE_CHUNK)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHALLOW_CORE_CHUNK)
                .withGuidePage(PylonPages.CORE_CHUNKS);
    }

    public static final ItemStack SUBSURFACE_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SUBSURFACE_CORE_CHUNK)
            .build();

    static {
        RebarItem.register(RebarItem.class, SUBSURFACE_CORE_CHUNK)
                .withGuidePage(PylonPages.CORE_CHUNKS);
    }

    public static final ItemStack INTERMEDIATE_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.INTERMEDIATE_CORE_CHUNK)
            .build();

    static {
        RebarItem.register(RebarItem.class, INTERMEDIATE_CORE_CHUNK)
                .withGuidePage(PylonPages.CORE_CHUNKS);
    }

    public static final ItemStack DEEP_CORE_CHUNK = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.DEEP_CORE_CHUNK)
            .build();

    static {
        RebarItem.register(RebarItem.class, DEEP_CORE_CHUNK)
                .withGuidePage(PylonPages.CORE_CHUNKS);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Magic" defaultstate=collapsed>

    public static final ItemStack SHIMMER_DUST_1 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_1)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_1)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack SHIMMER_DUST_2 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_2)
            .set(DataComponentTypes.ITEM_MODEL, Material.REDSTONE.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_2)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack SHIMMER_DUST_3 = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SHIMMER_DUST_3)
            .set(DataComponentTypes.ITEM_MODEL, Material.GLOWSTONE_DUST.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_DUST_3)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack COVALENT_BINDER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COVALENT_BINDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_BLUE_DYE.getKey())
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, COVALENT_BINDER)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack COHESIVE_UNIT = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COHESIVE_UNIT)
            .set(DataComponentTypes.ITEM_MODEL, Material.ENDER_PEARL.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, COHESIVE_UNIT)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack SHIMMER_BRONZE = ItemStackBuilder.rebar(Material.COPPER_INGOT, PylonKeys.SHIMMER_BRONZE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_BRONZE)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack LISELETTE_CONDUCTOR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_CONDUCTOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.BAMBOO_BUTTON.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, LISELETTE_CONDUCTOR)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack LISELETTE_CATHODE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_CATHODE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRIMSON_BUTTON.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, LISELETTE_CATHODE)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack LISELETTE_ANODE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LISELETTE_ANODE)
            .set(DataComponentTypes.ITEM_MODEL, Material.WARPED_BUTTON.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, LISELETTE_ANODE)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack ENRICHED_SOUL_SOIL = ItemStackBuilder.rebar(Material.SOUL_SOIL, PylonKeys.ENRICHED_SOUL_SOIL)
            .build();

    static {
        RebarItem.register(RebarItem.class, ENRICHED_SOUL_SOIL)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.MAGIC);
    }

    public static final ItemStack SHIMMER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.SHIMMER_SKULL)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_SKULL)
                .withGuidePage(PylonPages.MAGIC);
    }

    //</editor-fold>

    //<editor-fold desc="Resources - Miscellaneous" defaultstate=collapsed>

    public static final ItemStack ROCK_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.ROCK_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, ROCK_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack FINE_SEDIMENT = ItemStackBuilder.rebar(Material.GLOWSTONE_DUST, PylonKeys.FINE_SEDIMENT)
            .build();

    static {
        RebarItem.register(RebarItem.class, FINE_SEDIMENT)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack SLAG = ItemStackBuilder.rebar(Material.FLINT, PylonKeys.SLAG)
            .build();

    static {
        RebarItem.register(RebarItem.class, SLAG)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack QUARTZ_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.QUARTZ_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, QUARTZ_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack DIAMOND_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.DIAMOND_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, DIAMOND_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack EMERALD_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EMERALD_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, EMERALD_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack FIBER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FIBER)
            .set(DataComponentTypes.ITEM_MODEL, Material.BAMBOO_MOSAIC.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, FIBER)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack COAL_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COAL_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.GUNPOWDER.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, COAL_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack CHARCOAL_BLOCK = ItemStackBuilder.rebar(Material.COAL_BLOCK, PylonKeys.CHARCOAL_BLOCK)
            .build();

    static {
        RebarItem.register(CharcoalBlock.class, CHARCOAL_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack CARBON = ItemStackBuilder.rebar(Material.CHARCOAL, PylonKeys.CARBON)
            .build();

    static {
        RebarItem.register(RebarItem.class, CARBON)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack OBSIDIAN_CHIP = ItemStackBuilder.rebar(Material.POLISHED_BLACKSTONE_BUTTON, PylonKeys.OBSIDIAN_CHIP)
            .build();

    static {
        RebarItem.register(RebarItem.class, OBSIDIAN_CHIP)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack SULFUR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.SULFUR)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_DYE.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, SULFUR)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack SULFUR_BLOCK = ItemStackBuilder.rebar(Material.YELLOW_TERRACOTTA, PylonKeys.SULFUR_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, SULFUR_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack GYPSUM = ItemStackBuilder.rebar(Material.QUARTZ, PylonKeys.GYPSUM)
            .build();

    static {
        RebarItem.register(RebarItem.class, GYPSUM)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack GYPSUM_BLOCK = ItemStackBuilder.rebar(Material.QUARTZ_BLOCK, PylonKeys.GYPSUM_BLOCK)
            .build();

    static {
        RebarItem.register(RebarItem.class, GYPSUM_BLOCK)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack GYPSUM_DUST = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.GYPSUM_DUST)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, GYPSUM_DUST)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack REFRACTORY_MIX = ItemStackBuilder.rebar(Material.SMOOTH_RED_SANDSTONE, PylonKeys.REFRACTORY_MIX)
            .build();

    static {
        RebarItem.register(RebarItem.class, REFRACTORY_MIX)
                .withBlock(RefractoryMix.class)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack UNFIRED_REFRACTORY_BRICK = ItemStackBuilder.rebar(Material.BRICK, PylonKeys.UNFIRED_REFRACTORY_BRICK)
            .build();

    static {
        RebarItem.register(RebarItem.class, UNFIRED_REFRACTORY_BRICK)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack REFRACTORY_BRICK = ItemStackBuilder.rebar(Material.NETHERITE_INGOT, PylonKeys.REFRACTORY_BRICK)
            .build();

    static {
        RebarItem.register(RebarItem.class, REFRACTORY_BRICK)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    public static final ItemStack REFRACTORY_BRICKS = ItemStackBuilder.rebar(Material.DEEPSLATE_TILES, PylonKeys.REFRACTORY_BRICKS)
            .build();

    static {
        RebarItem.register(RebarItem.class, REFRACTORY_BRICKS)
                .withBlock(SmelteryComponent.class)
                .withGuidePage(PylonPages.MISCELLANEOUS);
    }

    // </editor-fold>

    //<editor-fold desc="Components" defaultstate=collapsed>

    public static final ItemStack COPPER_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.COPPER_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, COPPER_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack GOLD_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.GOLD_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, GOLD_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack IRON_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.IRON_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, IRON_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack TIN_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.TIN_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack BRONZE_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.BRONZE_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_SHEET = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.STEEL_SHEET)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_SHEET)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack COPPER_DRILL_BIT = ItemStackBuilder.rebar(Material.LIGHTNING_ROD, PylonKeys.COPPER_DRILL_BIT)
            .build();

    static {
        RebarItem.register(RebarItem.class, COPPER_DRILL_BIT)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack BRONZE_DRILL_BIT = ItemStackBuilder.rebar(Material.LIGHTNING_ROD, PylonKeys.BRONZE_DRILL_BIT)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_DRILL_BIT)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_DRILL_BIT = ItemStackBuilder.rebar(Material.LIGHTNING_ROD, PylonKeys.STEEL_DRILL_BIT)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_DRILL_BIT)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack ROTOR = ItemStackBuilder.rebar(Material.IRON_TRAPDOOR, PylonKeys.ROTOR)
            .build();

    static {
        RebarItem.register(RebarItem.class, ROTOR)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack BACKFLOW_VALVE = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.BACKFLOW_VALVE)
            .build();

    static {
        RebarItem.register(RebarItem.class, BACKFLOW_VALVE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack LEAF_VALVE = ItemStackBuilder.rebar(Material.DROPPER, PylonKeys.LEAF_VALVE)
            .build();

    static {
        RebarItem.register(RebarItem.class, LEAF_VALVE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack ANALOGUE_DISPLAY = ItemStackBuilder.rebar(Material.LIME_STAINED_GLASS_PANE, PylonKeys.ANALOGUE_DISPLAY)
            .build();

    static {
        RebarItem.register(RebarItem.class, ANALOGUE_DISPLAY)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FILTER_MESH = ItemStackBuilder.rebar(Material.IRON_BARS, PylonKeys.FILTER_MESH)
            .build();

    static {
        RebarItem.register(RebarItem.class, FILTER_MESH)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack NOZZLE = ItemStackBuilder.rebar(Material.LEVER, PylonKeys.NOZZLE)
            .build();

    static {
        RebarItem.register(RebarItem.class, NOZZLE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack ABYSSAL_CATALYST = ItemStackBuilder.rebar(Material.BLACK_CANDLE, PylonKeys.ABYSSAL_CATALYST)
            .build();

    static {
        RebarItem.register(RebarItem.class, ABYSSAL_CATALYST)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack HYDRAULIC_MOTOR = ItemStackBuilder.rebar(Material.PISTON, PylonKeys.HYDRAULIC_MOTOR)
            .build();

    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_MOTOR)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack AXLE = ItemStackBuilder.rebar(Material.OAK_FENCE, PylonKeys.AXLE)
            .build();

    static {
        RebarItem.register(RebarItem.class, AXLE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack SAWBLADE = ItemStackBuilder.rebar(Material.IRON_BARS, PylonKeys.SAWBLADE)
            .build();

    static {
        RebarItem.register(RebarItem.class, SAWBLADE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack WEIGHTED_SHAFT = ItemStackBuilder.rebar(Material.DEEPSLATE_TILE_WALL, PylonKeys.WEIGHTED_SHAFT)
            .build();

    static {
        RebarItem.register(RebarItem.class, WEIGHTED_SHAFT)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack HYDRAULIC_CANNON_CHAMBER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HYDRAULIC_CANNON_CHAMBER)
            .set(DataComponentTypes.ITEM_MODEL, Material.SNOWBALL.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_CANNON_CHAMBER)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack PORTABILITY_CATALYST = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.PORTABILITY_CATALYST)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(RebarItem.class, PORTABILITY_CATALYST)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FLUID_INPUT_HATCH = ItemStackBuilder.rebar(Material.LIGHT_BLUE_TERRACOTTA, PylonKeys.FLUID_INPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, FLUID_INPUT_HATCH)
                .withBlock(FluidInputHatch.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FLUID_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.ORANGE_TERRACOTTA, PylonKeys.FLUID_OUTPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, FLUID_OUTPUT_HATCH)
                .withBlock(FluidOutputHatch.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack ITEM_INPUT_HATCH = ItemStackBuilder.rebar(Material.GREEN_TERRACOTTA, PylonKeys.ITEM_INPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, ITEM_INPUT_HATCH)
                .withBlock(ItemInputHatch.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack ITEM_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.RED_TERRACOTTA, PylonKeys.ITEM_OUTPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, ITEM_OUTPUT_HATCH)
                .withBlock(ItemOutputHatch.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_CYLINDER = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.STEEL_CYLINDER)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_CYLINDER)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_CRANKSHAFT = ItemStackBuilder.rebar(Material.GRAY_CANDLE, PylonKeys.STEEL_CRANKSHAFT)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_CRANKSHAFT)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack KINETIC_CONTROL_MECHANISM = ItemStackBuilder.rebar(Material.CLOCK, PylonKeys.KINETIC_CONTROL_MECHANISM)
            .build();

    static {
        RebarItem.register(RebarItem.class, KINETIC_CONTROL_MECHANISM)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack INTERNAL_COMBUSTION_ENGINE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.INTERNAL_COMBUSTION_ENGINE)
            .set(DataComponentTypes.ITEM_MODEL, Material.DROPPER.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, INTERNAL_COMBUSTION_ENGINE)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack CARGO_CONTROLLER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CARGO_CONTROLLER)
            .set(DataComponentTypes.ITEM_MODEL, Material.NOTE_BLOCK.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CARGO_CONTROLLER)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack REINFORCED_GLASS = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS, PylonKeys.REINFORCED_GLASS)
            .build();

    static {
        RebarItem.register(RebarItem.class, REINFORCED_GLASS)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack REINFORCED_GLASS_CASING = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS_PANE, PylonKeys.REINFORCED_GLASS_CASING)
            .build();

    static {
        RebarItem.register(RebarItem.class, REINFORCED_GLASS_CASING)
                .withBlock(ReinforcedGlassCasing.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack IRON_SUPPORT_BEAM = ItemStackBuilder.rebar(Material.POLISHED_DEEPSLATE_WALL, PylonKeys.IRON_SUPPORT_BEAM)
            .build();

    static {
        RebarItem.register(RebarItem.class, IRON_SUPPORT_BEAM)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_SUPPORT_BEAM = ItemStackBuilder.rebar(Material.POLISHED_TUFF_WALL, PylonKeys.STEEL_SUPPORT_BEAM)
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_SUPPORT_BEAM)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack BRONZE_FOUNDATION = ItemStackBuilder.rebar(Material.LIGHT_GRAY_CONCRETE, PylonKeys.BRONZE_FOUNDATION)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_FOUNDATION)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack BRONZE_GRATING = ItemStackBuilder.rebar(Material.WAXED_COPPER_BARS, PylonKeys.BRONZE_GRATING)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_GRATING)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FIREBOX = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FIREBOX)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLAST_FURNACE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, FIREBOX)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FLUID_FIREBOX = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_FIREBOX)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLAST_FURNACE.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, FLUID_FIREBOX)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack COPPER_PIPE_ASSEMBLY = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COPPER_PIPE_ASSEMBLY)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_CHAIN.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, COPPER_PIPE_ASSEMBLY)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack LARGE_COPPER_PIPE_ASSEMBLY = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.LARGE_COPPER_PIPE_ASSEMBLY)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_CHAIN.getKey())
            .build();
    static {
        RebarItem.register(RebarItem.class, LARGE_COPPER_PIPE_ASSEMBLY)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack REINFORCED_PLATING = ItemStackBuilder.rebar(Material.GRAY_STAINED_GLASS_PANE, PylonKeys.REINFORCED_PLATING)
            .build();

    static {
        RebarItem.register(RebarItem.class, REINFORCED_PLATING)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack DISTILLATION_TOWER_RING = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DISTILLATION_TOWER_RING)
            .build();

    static {
        RebarItem.register(RebarItem.class, DISTILLATION_TOWER_RING)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack SMOKESTACK_RING = ItemStackBuilder.rebar(Material.BRICK_WALL, PylonKeys.SMOKESTACK_RING)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMOKESTACK_RING)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack SMOKESTACK_CAP = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.SMOKESTACK_CAP)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMOKESTACK_CAP)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack COPPER_FRAMED_GLASS = ItemStackBuilder.rebar(Material.ORANGE_STAINED_GLASS, PylonKeys.COPPER_FRAMED_GLASS)
            .build();

    static {
        RebarItem.register(RebarItem.class, COPPER_FRAMED_GLASS)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack QUARTZ_CAP = ItemStackBuilder.rebar(Material.QUARTZ_SLAB, PylonKeys.QUARTZ_CAP)
            .build();

    static {
        RebarItem.register(RebarItem.class, QUARTZ_CAP)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack SOLAR_LENS = ItemStackBuilder.rebar(Material.GLASS_PANE, PylonKeys.SOLAR_LENS)
            .build();

    static {
        RebarItem.register(RebarItem.class, SOLAR_LENS)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack LISELETTE_COLLECTOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.LISELETTE_COLLECTOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.BLACK_CONCRETE.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, LISELETTE_COLLECTOR)
                .withBlock(LiseletteCollector.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack HYDRAULIC_FRACTURE_CAP = ItemStackBuilder.rebar(Material.POLISHED_ANDESITE, PylonKeys.HYDRAULIC_FRACTURE_CAP)
            .build();
    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_FRACTURE_CAP)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack INJECTION_PIPE = ItemStackBuilder.rebar(Material.IRON_CHAIN, PylonKeys.INJECTION_PIPE)
            .build();
    static {
        RebarItem.register(RebarItem.class, INJECTION_PIPE)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack STEEL_FOUNDATION = ItemStackBuilder.rebar(Material.GRAY_CONCRETE, PylonKeys.STEEL_FOUNDATION)
            .build();
    static {
        RebarItem.register(RebarItem.class, STEEL_FOUNDATION)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    public static final ItemStack FLARE_STACK_STRUCTURE = ItemStackBuilder.rebar(Material.NETHER_BRICK_FENCE, PylonKeys.FLARE_STACK_STRUCTURE)
            .build();
    static {
        RebarItem.register(RebarItem.class, FLARE_STACK_STRUCTURE)
                .withBlock(RebarBlock.class)
                .withGuidePage(PylonPages.COMPONENTS);
    }

    //</editor-fold>

    //<editor-fold desc="Tools" defaultstate=collapsed>
    public static final ItemStack STONE_HAMMER = ItemStackBuilder.rebarWeapon(Material.STONE_PICKAXE, PylonKeys.STONE_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();

    static {
        RebarItem.register(Hammer.class, STONE_HAMMER)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack IRON_HAMMER = ItemStackBuilder.rebarWeapon(Material.IRON_PICKAXE, PylonKeys.IRON_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();

    static {
        RebarItem.register(Hammer.class, IRON_HAMMER)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack BRONZE_HAMMER = ItemStackBuilder.rebarWeapon(Material.DIAMOND_PICKAXE, PylonKeys.BRONZE_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_PICKAXE.getKey())
            .noTool().build();
    static {
        RebarItem.register(Hammer.class, BRONZE_HAMMER)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack DIAMOND_HAMMER = ItemStackBuilder.rebarWeapon(Material.DIAMOND_PICKAXE, PylonKeys.DIAMOND_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .noTool().build();

    static {
        RebarItem.register(Hammer.class, DIAMOND_HAMMER)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE)));
    }


    public static final ItemStack STEEL_HAMMER = ItemStackBuilder.rebarWeapon(Material.DIAMOND_PICKAXE, PylonKeys.STEEL_HAMMER, true, true, false)
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.00001f)
                    .cooldownGroup(PylonKeys.HAMMER)
                    .build())
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_PICKAXE.getKey())
            .noTool().build();
    static {
        RebarItem.register(Hammer.class, STEEL_HAMMER)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack BRONZE_AXE = ItemStackBuilder.rebarToolWeapon(Material.STONE_AXE, PylonKeys.BRONZE_AXE, RebarUtils.axeMineable(), true, false, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_AXE.getKey())
            .build();

    static {
        RebarItem.register(BronzeTool.class, BRONZE_AXE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack BRONZE_PICKAXE = ItemStackBuilder.rebarToolWeapon(Material.STONE_PICKAXE, PylonKeys.BRONZE_PICKAXE, RebarUtils.pickaxeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_PICKAXE.getKey())
            .build();

    static {
        RebarItem.register(BronzeTool.class, BRONZE_PICKAXE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack BRONZE_SHOVEL = ItemStackBuilder.rebarToolWeapon(Material.STONE_SHOVEL, PylonKeys.BRONZE_SHOVEL, RebarUtils.shovelMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SHOVEL.getKey())
            .build();

    static {
        RebarItem.register(BronzeTool.class, BRONZE_SHOVEL)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack BRONZE_HOE = ItemStackBuilder.rebarToolWeapon(Material.STONE_HOE, PylonKeys.BRONZE_HOE, RebarUtils.hoeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_HOE.getKey())
            .build();

    static {
        RebarItem.register(BronzeTool.class, BRONZE_HOE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack STEEL_AXE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_AXE, PylonKeys.STEEL_AXE, RebarUtils.axeMineable(), true, false, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_AXE.getKey())
            .build();

    static {
        RebarItem.register(SteelTool.class, STEEL_AXE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack STEEL_PICKAXE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_PICKAXE, PylonKeys.STEEL_PICKAXE, RebarUtils.pickaxeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_PICKAXE.getKey())
            .build();

    static {
        RebarItem.register(SteelTool.class, STEEL_PICKAXE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack STEEL_SHOVEL = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_SHOVEL, PylonKeys.STEEL_SHOVEL, RebarUtils.shovelMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_SHOVEL.getKey())
            .build();

    static {
        RebarItem.register(SteelTool.class, STEEL_SHOVEL)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack STEEL_HOE = ItemStackBuilder.rebarToolWeapon(Material.DIAMOND_HOE, PylonKeys.STEEL_HOE, RebarUtils.hoeMineable(), true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_HOE.getKey())
            .build();

    static {
        RebarItem.register(SteelTool.class, STEEL_HOE)
                .withGuidePage(PylonPages.TOOLS);
    }


    public static final ItemStack WATERING_CAN = ItemStackBuilder.rebar(Material.BUCKET, PylonKeys.WATERING_CAN)
            .build();

    static {
        RebarItem.register(WateringCan.class, WATERING_CAN)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack LUMBER_AXE = ItemStackBuilder.rebar(Material.WOODEN_AXE, PylonKeys.LUMBER_AXE)
            .durability(ConfigSection.fromSettings(PylonKeys.LUMBER_AXE).getOrThrow("durability", ConfigAdapter.INTEGER))
            .build();

    static {
        RebarItem.register(LumberAxe.class, LUMBER_AXE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack PORTABLE_CRAFTING_TABLE = ItemStackBuilder.rebar(Material.CRAFTING_TABLE, PylonKeys.PORTABLE_CRAFTING_TABLE)
            .build();

    static {
        RebarItem.register(PortableCraftingTable.class, PORTABLE_CRAFTING_TABLE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack PORTABLE_DUSTBIN = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.PORTABLE_DUSTBIN)
            .build();

    static {
        RebarItem.register(PortableDustbin.class, PORTABLE_DUSTBIN)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack PORTABLE_ENDER_CHEST = ItemStackBuilder.rebar(Material.ENDER_CHEST, PylonKeys.PORTABLE_ENDER_CHEST)
            .build();

    static {
        RebarItem.register(PortableEnderChest.class, PORTABLE_ENDER_CHEST)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack CLIMBING_PICK = ItemStackBuilder.rebar(Material.DIAMOND_HOE, PylonKeys.CLIMBING_PICK)
            .build();

    static {
        RebarItem.register(ClimbingPick.class, CLIMBING_PICK)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack BRICK_MOLD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRICK_MOLD)
            .useCooldown(ConfigSection.fromSettings(PylonKeys.BRICK_MOLD).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.BRICK_MOLD)
            .set(DataComponentTypes.ITEM_MODEL, Material.OAK_FENCE_GATE.getKey())
            .build();

    static {
        RebarItem.register(BrickMold.class, BRICK_MOLD)
                .withGuidePage(PylonPages.TOOLS)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(MoldingRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack TONGS = ItemStackBuilder.rebar(Material.SHEARS, PylonKeys.TONGS)
            .build();

    static {
        RebarItem.register(RebarItem.class, TONGS)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack SHIMMER_MAGNET = ItemStackBuilder.rebar(Material.BREEZE_ROD, PylonKeys.SHIMMER_MAGNET)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .addCustomModelDataFlag(true)
            .build();

    static {
        RebarItem.register(ShimmerMagnet.class, SHIMMER_MAGNET)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack FIREPROOF_RUNE = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.FIREPROOF_RUNE)
            .set(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(FireproofRune.IS_FIRE_TAG))
            .set(DataComponentTypes.FIREWORK_EXPLOSION, FireworkEffect.builder()
                    .withColor(Color.fromRGB(0xff5e00))
                    .build())
            .hideFromTooltip(DataComponentTypes.FIREWORK_EXPLOSION)
            .build();

    static {
        RebarItem.register(FireproofRune.class, FIREPROOF_RUNE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack SOULBOUND_RUNE = ItemStackBuilder.rebar(Material.FIREWORK_STAR, PylonKeys.SOULBOUND_RUNE)
            .set(DataComponentTypes.FIREWORK_EXPLOSION, FireworkEffect.builder()
                    .withColor(Color.PURPLE)
                    .build())
            .editPdc(pdc -> pdc.set(SoulboundRune.SOULBOUND_KEY, RebarSerializers.BOOLEAN, true))
            .hideFromTooltip(DataComponentTypes.FIREWORK_EXPLOSION)
            .build();

    static {
        RebarItem.register(SoulboundRune.class, SOULBOUND_RUNE)
                .withGuidePage(PylonPages.TOOLS);
    }

    public static final ItemStack DIESEL_BOOSTER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.DIESEL_BOOSTER)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.ITEM_MODEL, Material.FIREWORK_ROCKET.getKey())
            .editPdc(pdc -> {
                pdc.set(PylonFluids.BIODIESEL.getKey(), RebarSerializers.DOUBLE, 0.0);
            })
            .build();

    static {
        RebarItem.register(DieselBooster.class, DIESEL_BOOSTER)
                .withGuidePage(PylonPages.TOOLS);
    }

    @SuppressWarnings("ConstantConditions")
    public static final ItemStack CONFETTI_POPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.CONFETTI_POPPER)
            .set(DataComponentTypes.ITEM_MODEL, Material.FIREWORK_ROCKET.getKey())
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(
                            ConfigSection.fromSettings(PylonKeys.CONFETTI_POPPER).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue()
                    )
                    .sound(Registry.SOUNDS.getKey(Sound.ITEM_CROSSBOW_LOADING_START))
                    .animation(ItemUseAnimation.TOOT_HORN)
                    .hasConsumeParticles(false)
            )
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.CONFETTI_POPPER).getOrThrow("cooldown-seconds", ConfigAdapter.DOUBLE).floatValue()
                    )
                    .cooldownGroup(PylonKeys.CONFETTI_POPPER)
                    .build())
            .build();

    static {
        RebarItem.register(ConfettiPopper.class, CONFETTI_POPPER)
                .withGuidePage(PylonPages.TOOLS);
    }

    // TODO maybe resurrect this at some point without palladium?
//    public static final ItemStack FLIGHT_RING = ItemStackBuilder.rebar(Material.NETHER_STAR, PylonKeys.FLIGHT_RING)
//            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
//            .build();
//    static {
//        RebarItem.register(FlightRing.class, FLIGHT_RING);
//        PylonPages.TOOLS.addItem(FLIGHT_RING);
//    }
    public static final ItemStack TAPE_MEASURE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.TAPE_MEASURE)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_NAUTILUS_ARMOR.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();
    static {
        RebarItem.register(TapeMeasure.class, TAPE_MEASURE)
                .withGuidePage(PylonPages.TOOLS);
    }

    //</editor-fold>

    //<editor-fold desc="Combat" defaultstate=collapsed>

    public static final ItemStack BRONZE_SWORD = ItemStackBuilder.rebarWeapon(Material.STONE_SWORD, PylonKeys.BRONZE_SWORD, true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SWORD.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_SWORD)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack STEEL_SWORD = ItemStackBuilder.rebarWeapon(Material.DIAMOND_SWORD, PylonKeys.STEEL_SWORD, true, false, false)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_SWORD.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, STEEL_SWORD)
                .withGuidePage(PylonPages.COMBAT);
    }


    public static final ItemStack BEHEADING_SWORD = ItemStackBuilder.rebar(Material.DIAMOND_SWORD, PylonKeys.BEHEADING_SWORD)
            .durability(ConfigSection.fromSettings(PylonKeys.BEHEADING_SWORD).getOrThrow("durability", ConfigAdapter.INTEGER)) // todo: weapon stats?
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(BeheadingSword.class, BEHEADING_SWORD)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack BANDAGE = ItemStackBuilder.rebar(Material.COBWEB, PylonKeys.BANDAGE)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(ConfigSection.fromSettings(PylonKeys.BANDAGE).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .build())
            .build();

    static {
        RebarItem.register(HealingConsumable.class, BANDAGE)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack SPLINT = ItemStackBuilder.rebar(Material.STICK, PylonKeys.SPLINT)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(ConfigSection.fromSettings(PylonKeys.SPLINT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .build())
            .build();

    static {
        RebarItem.register(HealingConsumable.class, SPLINT)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack DISINFECTANT = ItemStackBuilder.rebar(Material.BREWER_POTTERY_SHERD, PylonKeys.DISINFECTANT)
            // Using the actual potion material doesn't let you set the name properly, gives you a
            // class string of a nonexistant potion type for some reason
            .set(DataComponentTypes.ITEM_MODEL, Material.POTION.getKey())
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .hasConsumeParticles(false)
                    .consumeSeconds(ConfigSection.fromSettings(PylonKeys.DISINFECTANT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .addEffect(ConsumeEffect.clearAllStatusEffects())
                    .build())
            .build();

    static {
        RebarItem.register(HealingConsumable.class, DISINFECTANT)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack MEDKIT = ItemStackBuilder.rebar(Material.SHULKER_SHELL, PylonKeys.MEDKIT)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds(ConfigSection.fromSettings(PylonKeys.MEDKIT).getOrThrow("consume-seconds", ConfigAdapter.DOUBLE).floatValue())
                    .animation(ItemUseAnimation.BOW)
                    .hasConsumeParticles(false)
                    .addEffect(ConsumeEffect.clearAllStatusEffects())
            )
            .build();

    static {
        RebarItem.register(HealingConsumable.class, MEDKIT)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack REACTIVATED_WITHER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.REACTIVATED_WITHER_SKULL)
            .unset(DataComponentTypes.EQUIPPABLE)
            .durability(ConfigSection.fromSettings(PylonKeys.REACTIVATED_WITHER_SKULL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .useCooldown(ConfigSection.fromSettings(PylonKeys.REACTIVATED_WITHER_SKULL).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.REACTIVATED_WITHER_SKULL)
            .build();

    static {
        RebarItem.register(ReactivatedWitherSkull.class, REACTIVATED_WITHER_SKULL)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack HYPER_ACTIVATED_WITHER_SKULL = ItemStackBuilder.rebar(Material.WITHER_SKELETON_SKULL, PylonKeys.HYPER_ACTIVATED_WITHER_SKULL)
            .unset(DataComponentTypes.EQUIPPABLE)
            .durability(ConfigSection.fromSettings(PylonKeys.HYPER_ACTIVATED_WITHER_SKULL).getOrThrow("durability", ConfigAdapter.INTEGER))
            .useCooldown(ConfigSection.fromSettings(PylonKeys.HYPER_ACTIVATED_WITHER_SKULL).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER), PylonKeys.REACTIVATED_WITHER_SKULL)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .build();

    static {
        RebarItem.register(ReactivatedWitherSkull.class, HYPER_ACTIVATED_WITHER_SKULL)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack ICE_ARROW = ItemStackBuilder.rebar(Material.ARROW, PylonKeys.ICE_ARROW).build();

    static {
        RebarItem.register(IceArrow.class, ICE_ARROW)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack RECOIL_ARROW = ItemStackBuilder.rebar(Material.ARROW, PylonKeys.RECOIL_ARROW)
            .build();

    static {
        RebarItem.register(RecoilArrow.class, RECOIL_ARROW)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack HYDRAULIC_CANNON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.HYDRAULIC_CANNON)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_HORSE_ARMOR.getKey())
            .set(DataComponentTypes.USE_COOLDOWN, UseCooldown
                    .useCooldown(
                            ConfigSection.fromSettings(PylonKeys.HYDRAULIC_CANNON).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    )
                    .cooldownGroup(PylonKeys.HYDRAULIC_CANNON.key())
                    .build())
            .editPdc(pdc -> {
                pdc.set(PylonFluids.HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, 0.0);
                pdc.set(PylonFluids.DIRTY_HYDRAULIC_FLUID.getKey(), RebarSerializers.DOUBLE, 0.0);
            })
            .build();

    static {
        RebarItem.register(HydraulicCannon.class, HYDRAULIC_CANNON)
                .withGuidePage(PylonPages.COMBAT);
    }

    public static final ItemStack TIN_PROJECTILE = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.TIN_PROJECTILE)
            .build();

    static {
        RebarItem.register(RebarItem.class, TIN_PROJECTILE)
                .withGuidePage(PylonPages.COMBAT);
    }

    //</editor-fold>

    //<editor-fold desc="Talismans" defaultstate=collapsed>

    public static final ItemStack HEALTH_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack HEALTH_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack HEALTH_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.AMETHYST_SHARD, PylonKeys.HEALTH_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HealthTalisman.class, HEALTH_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack FARMING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack FARMING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack FARMING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.BOWL, PylonKeys.FARMING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(FarmingTalisman.class, FARMING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BARTERING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BARTERING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BARTERING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.GOLD_INGOT, PylonKeys.BARTERING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BarteringTalisman.class, BARTERING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack WATER_BREATHING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.NAUTILUS_SHELL, PylonKeys.WATER_BREATHING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(WaterBreathingTalisman.class, WATER_BREATHING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack LUCK_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack LUCK_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack LUCK_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.RABBIT_FOOT, PylonKeys.LUCK_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(LuckTalisman.class, LUCK_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BREEDING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BREEDING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack BREEDING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.APPLE, PylonKeys.BREEDING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(BreedingTalisman.class, BREEDING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack ENCHANTING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack ENCHANTING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack ENCHANTING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.ENCHANTED_BOOK, PylonKeys.ENCHANTING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(EnchantingTalisman.class, ENCHANTING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack HUNTING_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_SIMPLE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack HUNTING_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_ADVANCED)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack HUNTING_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.SKELETON_SKULL, PylonKeys.HUNTING_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(HuntingTalisman.class, HUNTING_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack EXPERIENCE_TALISMAN_SIMPLE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EXPERIENCE_TALISMAN_SIMPLE)
            .set(DataComponentTypes.ITEM_MODEL, Material.EXPERIENCE_BOTTLE.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(ExperienceTalisman.class, EXPERIENCE_TALISMAN_SIMPLE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack EXPERIENCE_TALISMAN_ADVANCED = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EXPERIENCE_TALISMAN_ADVANCED)
            .set(DataComponentTypes.ITEM_MODEL, Material.EXPERIENCE_BOTTLE.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(ExperienceTalisman.class, EXPERIENCE_TALISMAN_ADVANCED)
                .withGuidePage(PylonPages.TALISMANS);
    }

    public static final ItemStack EXPERIENCE_TALISMAN_ULTIMATE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EXPERIENCE_TALISMAN_ULTIMATE)
            .set(DataComponentTypes.ITEM_MODEL, Material.EXPERIENCE_BOTTLE.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .build();

    static {
        RebarItem.register(ExperienceTalisman.class, EXPERIENCE_TALISMAN_ULTIMATE)
                .withGuidePage(PylonPages.TALISMANS);
    }

    //</editor-fold>

    //<editor-fold desc="Armour" defaultstate=collapsed>

    public static final ItemStack BRONZE_HELMET = ItemStackBuilder.rebarHelmet(Material.LEATHER_HELMET, PylonKeys.BRONZE_HELMET, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_HELMET.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD)
                    .assetId(Key.key("gold"))
                    .build())
            .build();

    static {
        RebarItem.register(BronzeArmor.class, BRONZE_HELMET)
                        .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack BRONZE_CHESTPLATE = ItemStackBuilder.rebarChestplate(Material.LEATHER_CHESTPLATE, PylonKeys.BRONZE_CHESTPLATE, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_CHESTPLATE.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.CHEST)
                    .assetId(Key.key("gold"))
                    .build())
            .build();

    static {
        RebarItem.register(BronzeArmor.class, BRONZE_CHESTPLATE)
                        .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack BRONZE_LEGGINGS = ItemStackBuilder.rebarLeggings(Material.LEATHER_LEGGINGS, PylonKeys.BRONZE_LEGGINGS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_LEGGINGS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.LEGS)
                    .assetId(Key.key("gold"))
                    .build())
            .build();

    static {
        RebarItem.register(BronzeArmor.class, BRONZE_LEGGINGS)
                .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack BRONZE_BOOTS = ItemStackBuilder.rebarBoots(Material.LEATHER_BOOTS, PylonKeys.BRONZE_BOOTS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_BOOTS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.FEET)
                    .assetId(Key.key("gold"))
                    .build())
            .build();

    static {
        RebarItem.register(BronzeArmor.class, BRONZE_BOOTS)
                .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack STEEL_HELMET = ItemStackBuilder.rebarHelmet(Material.DIAMOND_HELMET, PylonKeys.STEEL_HELMET, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_HELMET.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD)
                    .assetId(Key.key("netherite"))
                    .equipSound(Key.key("item.armor.equip_netherite"))
                    .build())
            .addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    pylonKey("steel_helmet_knockback_resistance"),
                    ConfigSection.fromSettings(PylonKeys.STEEL_HELMET).getOrThrow("knockback-resistance", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.HEAD
            ))
            .build();

    static {
        RebarItem.register(SteelArmor.class, STEEL_HELMET)
                .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack STEEL_CHESTPLATE = ItemStackBuilder.rebarChestplate(Material.DIAMOND_CHESTPLATE, PylonKeys.STEEL_CHESTPLATE, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_CHESTPLATE.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.CHEST)
                    .assetId(Key.key("netherite"))
                    .equipSound(Key.key("item.armor.equip_netherite"))
                    .build())
            .addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    pylonKey("steel_chestplate_knockback_resistance"),
                    ConfigSection.fromSettings(PylonKeys.STEEL_CHESTPLATE).getOrThrow("knockback-resistance", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.CHEST
            ))
            .build();

    static {
        RebarItem.register(SteelArmor.class, STEEL_CHESTPLATE)
                .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack STEEL_LEGGINGS = ItemStackBuilder.rebarLeggings(Material.DIAMOND_LEGGINGS, PylonKeys.STEEL_LEGGINGS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_LEGGINGS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.LEGS)
                    .assetId(Key.key("netherite"))
                    .equipSound(Key.key("item.armor.equip_netherite"))
                    .build())
            .addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    pylonKey("steel_leggings_knockback_resistance"),
                    ConfigSection.fromSettings(PylonKeys.STEEL_LEGGINGS).getOrThrow("knockback-resistance", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.LEGS
            ))
            .build();

    static {
        RebarItem.register(SteelArmor.class, STEEL_LEGGINGS)
                .withGuidePage(PylonPages.ARMOUR);
    }

    public static final ItemStack STEEL_BOOTS = ItemStackBuilder.rebarBoots(Material.DIAMOND_BOOTS, PylonKeys.STEEL_BOOTS, true)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_BOOTS.getKey())
            .set(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.FEET)
                    .assetId(Key.key("netherite"))
                    .equipSound(Key.key("item.armor.equip_netherite"))
                    .build())
            .addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    pylonKey("steel_boots_knockback_resistance"),
                    ConfigSection.fromSettings(PylonKeys.STEEL_BOOTS).getOrThrow("knockback-resistance", ConfigAdapter.DOUBLE),
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.FEET
            ))
            .build();

    static {
        RebarItem.register(SteelArmor.class, STEEL_BOOTS)
                .withGuidePage(PylonPages.ARMOUR);
    }

    //</editor-fold>

    //<editor-fold desc="Food" defaultstate=collapsed>

    public static final ItemStack FLOUR = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLOUR)
            .set(DataComponentTypes.ITEM_MODEL, Material.SUGAR.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, FLOUR)
                .withGuidePage(PylonPages.FOOD);
    }

    public static final ItemStack DOUGH = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.DOUGH)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_DYE.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, DOUGH)
                .withGuidePage(PylonPages.FOOD);
    }

    public static final ItemStack MONSTER_JERKY = ItemStackBuilder.rebar(Material.ROTTEN_FLESH, PylonKeys.MONSTER_JERKY)
            .set(DataComponentTypes.CONSUMABLE, Consumable.consumable().build())
            .set(DataComponentTypes.FOOD, FoodProperties.food()
                    .canAlwaysEat(false)
                    .nutrition(ConfigSection.fromSettings(PylonKeys.MONSTER_JERKY).getOrThrow("nutrition", ConfigAdapter.INTEGER))
                    .saturation(ConfigSection.fromSettings(PylonKeys.MONSTER_JERKY).getOrThrow("saturation", ConfigAdapter.DOUBLE).floatValue())
                    .build()
            )
            .build();

    static {
        RebarItem.register(RebarItem.class, MONSTER_JERKY)
                .withGuidePage(PylonPages.FOOD);
    }

    //</editor-fold>

    //<editor-fold desc="Building" defaultstate=collapsed>

    public static final ItemStack IGNEOUS_COMPOSITE = ItemStackBuilder.rebar(Material.OBSIDIAN, PylonKeys.IGNEOUS_COMPOSITE)
            .build();

    static {
        RebarItem.register(RebarItem.class, IGNEOUS_COMPOSITE)
                .withBlock(WitherProofBlock.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack PEDESTAL = ItemStackBuilder.rebar(Material.STONE_BRICK_WALL, PylonKeys.PEDESTAL)
            .build();

    static {
        RebarItem.register(RebarItem.class, PEDESTAL)
                .withBlock(Pedestal.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack ELEVATOR_1 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_1)
            .build();

    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_1)
                .withBlock(Elevator.class)
        .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack ELEVATOR_2 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_2)
            .build();

    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_2)
                .withBlock(Elevator.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack ELEVATOR_3 = ItemStackBuilder.rebar(Material.SMOOTH_QUARTZ_SLAB, PylonKeys.ELEVATOR_3)
            .build();

    static {
        RebarItem.register(Elevator.Item.class, ELEVATOR_3)
                .withBlock(Elevator.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack EXPLOSIVE_TARGET = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET)
            .build();

    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET)
                .withBlock(ExplosiveTarget.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack EXPLOSIVE_TARGET_FIERY = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_FIERY)
            .build();

    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_FIERY)
                .withBlock(ExplosiveTarget.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack EXPLOSIVE_TARGET_SUPER = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_SUPER)
            .build();

    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_SUPER)
                .withBlock(ExplosiveTarget.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack EXPLOSIVE_TARGET_SUPER_FIERY = ItemStackBuilder.rebar(Material.TARGET, PylonKeys.EXPLOSIVE_TARGET_SUPER_FIERY)
            .build();

    static {
        RebarItem.register(ExplosiveTarget.Item.class, EXPLOSIVE_TARGET_SUPER_FIERY)
                .withBlock(ExplosiveTarget.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    public static final ItemStack IMMOBILIZER = ItemStackBuilder.rebar(Material.PISTON, PylonKeys.IMMOBILIZER)
            .build();

    static {
        RebarItem.register(Immobilizer.Item.class, IMMOBILIZER)
                .withBlock(Immobilizer.class)
                .withGuidePage(PylonPages.BUILDING);
    }

    //</editor-fold>

    //<editor-fold desc="Simple Machines" defaultstate=collapsed>

    public static final ItemStack GRINDSTONE = ItemStackBuilder.rebar(Material.SMOOTH_STONE_SLAB, PylonKeys.GRINDSTONE)
            .build();

    static {
        RebarItem.register(RebarItem.class, GRINDSTONE)
                .withBlock(Grindstone.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(GrindstoneRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack GRINDSTONE_HANDLE = ItemStackBuilder.rebar(Material.OAK_FENCE, PylonKeys.GRINDSTONE_HANDLE)
            .build();

    static {
        RebarItem.register(RebarItem.class, GRINDSTONE_HANDLE)
                .withBlock(GrindstoneHandle.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack PIPED_CAULDRON = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.PIPED_CAULDRON)
            .build();
    static {
        RebarItem.register(RebarItem.class, PIPED_CAULDRON)
                .withBlock(PipedCauldron.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack CRUDE_ALLOY_FURNACE = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.CRUDE_ALLOY_FURNACE)
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUDE_ALLOY_FURNACE)
                .withBlock(CrudeAlloyFurnace.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(CrudeAlloyFurnaceRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack MIXING_POT = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.MIXING_POT)
            .build();

    static {
        RebarItem.register(MixingPot.MixingPotItem.class, MIXING_POT)
                .withBlock(MixingPot.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(MixingPotRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack MANUAL_CORE_DRILL_LEVER = ItemStackBuilder.rebar(Material.LEVER, PylonKeys.MANUAL_CORE_DRILL_LEVER)
            .build();

    static {
        RebarItem.register(RebarItem.class, MANUAL_CORE_DRILL_LEVER)
                .withBlock(ManualCoreDrillLever.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack MANUAL_CORE_DRILL = ItemStackBuilder.rebar(Material.CHISELED_STONE_BRICKS, PylonKeys.MANUAL_CORE_DRILL)
            .build();

    static {
        RebarItem.register(CoreDrill.Item.class, MANUAL_CORE_DRILL)
                .withBlock(CoreDrill.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack IMPROVED_MANUAL_CORE_DRILL = ItemStackBuilder.rebar(Material.WAXED_OXIDIZED_COPPER, PylonKeys.IMPROVED_MANUAL_CORE_DRILL)
            .build();

    static {
        RebarItem.register(ImprovedManualCoreDrill.Item.class, IMPROVED_MANUAL_CORE_DRILL)
                .withBlock(ImprovedManualCoreDrill.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack PRESS = ItemStackBuilder.rebar(Material.COMPOSTER, PylonKeys.PRESS)
            .build();

    static {
        RebarItem.register(Press.PressItem.class, PRESS)
                .withBlock(Press.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(PressableItemsPage.getButton()));
    }

    public static final ItemStack SPRINKLER = ItemStackBuilder.rebar(Material.FLOWER_POT, PylonKeys.SPRINKLER)
            .build();

    static {
        RebarItem.register(Sprinkler.Item.class, SPRINKLER)
                .withBlock(Sprinkler.class)
                .withGuidePage(PylonPages.FLUID_MACHINES);
    }

    public static final ItemStack CRUCIBLE = ItemStackBuilder.rebar(Material.CAULDRON, PylonKeys.CRUCIBLE)
            .build();

    static {
        RebarItem.register(RebarItem.class, CRUCIBLE)
                .withBlock(Crucible.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(CrucibleRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack FLUID_STRAINER = ItemStackBuilder.rebar(Material.COPPER_GRATE, PylonKeys.FLUID_STRAINER)
            .build();

    static {
        RebarItem.register(FluidStrainer.Item.class, FLUID_STRAINER)
                .withBlock(FluidStrainer.class)
                .withGuidePage(PylonPages.FLUID_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(StrainingRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack VACUUM_HOPPER_1 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_1)
            .build();

    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_1)
                .withBlock(VacuumHopper.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack VACUUM_HOPPER_2 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_2)
            .build();

    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_2)
                .withBlock(VacuumHopper.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack VACUUM_HOPPER_3 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_3)
            .build();

    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_3)
                .withBlock(VacuumHopper.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack VACUUM_HOPPER_4 = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.VACUUM_HOPPER_4)
            .build();

    static {
        RebarItem.register(VacuumHopper.Item.class, VACUUM_HOPPER_4)
                .withBlock(VacuumHopper.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack SHIMMER_PEDESTAL = ItemStackBuilder.rebar(Material.MOSSY_STONE_BRICK_WALL, PylonKeys.SHIMMER_PEDESTAL)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_PEDESTAL)
                .withBlock(Pedestal.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack SHIMMER_ALTAR = ItemStackBuilder.rebar(Material.SMOOTH_STONE_SLAB, PylonKeys.SHIMMER_ALTAR)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHIMMER_ALTAR)
                .withBlock(ShimmerAltar.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(ShimmerAltarRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack COLLIMATOR = ItemStackBuilder.rebar(Material.OBSIDIAN, PylonKeys.COLLIMATOR)
            .build();

    static {
        RebarItem.register(Collimator.Item.class, COLLIMATOR)
                .withBlock(Collimator.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    public static final ItemStack COLLIMATOR_PILLAR = ItemStackBuilder.rebar(Material.DEEPSLATE_TILE_WALL, PylonKeys.COLLIMATOR_PILLAR)
            .build();

    static {
        RebarItem.register(RebarItem.class, COLLIMATOR_PILLAR)
                .withBlock(CollimatorPillar.class)
                .withGuidePage(PylonPages.SIMPLE_MACHINES);
    }

    //</editor-fold>

    //<editor-fold desc="Smelting" defaultstate=collapsed>

    public static final ItemStack KILN = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.KILN)
            .build();

    static {
        RebarItem.register(RebarItem.class, KILN)
                .withBlock(Kiln.class)
                .withGuidePage(PylonPages.SMELTING)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(KilnRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack BLOOMERY = ItemStackBuilder.rebar(Material.MAGMA_BLOCK, PylonKeys.BLOOMERY)
            .build();

    static {
        RebarItem.register(RebarItem.class, BLOOMERY)
                .withBlock(Bloomery.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack BRONZE_ANVIL = ItemStackBuilder.rebar(Material.ANVIL, PylonKeys.BRONZE_ANVIL)
            .build();

    static {
        RebarItem.register(RebarItem.class, BRONZE_ANVIL)
                .withBlock(BronzeAnvil.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack SMELTERY_CONTROLLER = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.SMELTERY_CONTROLLER)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMELTERY_CONTROLLER)
                .withBlock(SmelteryController.class)
                .withGuidePage(PylonPages.SMELTING)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(SmelteryRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack SMELTERY_INPUT_HATCH = ItemStackBuilder.rebar(Material.LIGHT_BLUE_TERRACOTTA, PylonKeys.SMELTERY_INPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMELTERY_INPUT_HATCH)
                .withBlock(SmelteryInputHatch.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack SMELTERY_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.ORANGE_TERRACOTTA, PylonKeys.SMELTERY_OUTPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMELTERY_OUTPUT_HATCH)
                .withBlock(SmelteryOutputHatch.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack SMELTERY_HOPPER = ItemStackBuilder.rebar(Material.HOPPER, PylonKeys.SMELTERY_HOPPER)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMELTERY_HOPPER)
                .withBlock(SmelteryHopper.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack SMELTERY_BURNER = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.SMELTERY_BURNER)
            .build();

    static {
        RebarItem.register(RebarItem.class, SMELTERY_BURNER)
                .withBlock(SmelteryBurner.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack DIESEL_SMELTERY_HEATER = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.DIESEL_SMELTERY_HEATER)
            .build();

    static {
        RebarItem.register(DieselSmelteryHeater.Item.class, DIESEL_SMELTERY_HEATER)
                .withBlock(DieselSmelteryHeater.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack CASTING_UNIT = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.CASTING_UNIT)
            .build();

    static {
        RebarItem.register(RebarItem.class, CASTING_UNIT)
                .withBlock(CastingUnit.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack FORMING_TABLE = ItemStackBuilder.rebar(Material.CRAFTING_TABLE, PylonKeys.FORMING_TABLE)
            .build();

    static {
        RebarItem.register(RebarItem.class, FORMING_TABLE)
                .withBlock(FormingTable.class)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack NUGGET_MOLD = ItemStackBuilder.rebar(Material.IRON_NUGGET, PylonKeys.NUGGET_MOLD)
            .build();

    static {
        RebarItem.register(RebarItem.class, NUGGET_MOLD)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack INGOT_MOLD = ItemStackBuilder.rebar(Material.IRON_INGOT, PylonKeys.INGOT_MOLD)
            .build();

    static {
        RebarItem.register(RebarItem.class, INGOT_MOLD)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack BLOCK_MOLD = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.BLOCK_MOLD)
            .build();

    static {
        RebarItem.register(RebarItem.class, BLOCK_MOLD)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack SHEET_MOLD = ItemStackBuilder.rebar(Material.PAPER, PylonKeys.SHEET_MOLD)
            .build();

    static {
        RebarItem.register(RebarItem.class, SHEET_MOLD)
                .withGuidePage(PylonPages.SMELTING);
    }

    public static final ItemStack PIPE_MOLD = ItemStackBuilder.rebar(Material.BROWN_CONCRETE, PylonKeys.PIPE_MOLD)
            .build();

    static {
        RebarItem.register(RebarItem.class, PIPE_MOLD)
                .withGuidePage(PylonPages.SMELTING);
    }

    //</editor-fold>

    //<editor-fold desc="Silos" defaultstate=collapsed>

    public static final ItemStack SILO_CONVERTER = ItemStackBuilder.rebar(Material.STRIPPED_OAK_LOG, PylonKeys.SILO_CONVERTER)
            .build();

    static {
        RebarItem.register(RebarItem.class, SILO_CONVERTER)
                .withBlock(SiloConverter.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack WOODEN_SILO = ItemStackBuilder.rebar(Material.BROWN_TERRACOTTA, PylonKeys.WOODEN_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, WOODEN_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack COPPER_SILO = ItemStackBuilder.rebar(Material.TERRACOTTA, PylonKeys.COPPER_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, COPPER_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack TIN_SILO = ItemStackBuilder.rebar(Material.GREEN_TERRACOTTA, PylonKeys.TIN_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, TIN_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack IRON_SILO = ItemStackBuilder.rebar(Material.LIGHT_GRAY_TERRACOTTA, PylonKeys.IRON_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, IRON_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack BRONZE_SILO = ItemStackBuilder.rebar(Material.ORANGE_TERRACOTTA, PylonKeys.BRONZE_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, BRONZE_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    public static final ItemStack STEEL_SILO = ItemStackBuilder.rebar(Material.GRAY_TERRACOTTA, PylonKeys.STEEL_SILO)
            .build();

    static {
        RebarItem.register(Silo.Item.class, STEEL_SILO)
                .withBlock(Silo.class)
                .withGuidePage(PylonPages.SILOS);
    }

    //</editor-fold>

    //<editor-fold desc="Cargo" defaultstate="collapsed">

    public static final ItemStack CARGO_DUCT = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_DUCT)
            .set(DataComponentTypes.ITEM_MODEL, Material.GRAY_CONCRETE.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CARGO_DUCT)
                .withBlock(CargoDuct.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_EXTRACTOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_EXTRACTOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.RED_TERRACOTTA.getKey())
            .build();

    static {
        RebarItem.register(CargoExtractor.Item.class, CARGO_EXTRACTOR)
                .withBlock(CargoExtractor.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_INSERTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_INSERTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIME_TERRACOTTA.getKey())
            .build();

    static {
        RebarItem.register(CargoInserter.Item.class, CARGO_INSERTER)
                .withBlock(CargoInserter.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_VALVE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_VALVE)
            .set(DataComponentTypes.ITEM_MODEL, Material.WHITE_CONCRETE.getKey())
            .build();

    static {
        RebarItem.register(CargoValve.Item.class, CARGO_VALVE)
                .withBlock(CargoValve.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_BUFFER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_BUFFER)
            .set(DataComponentTypes.ITEM_MODEL, Material.BARREL.getKey())
            .build();

    static {
        RebarItem.register(CargoBuffer.Item.class, CARGO_BUFFER)
                .withBlock(CargoBuffer.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_MONITOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_MONITOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_STAINED_GLASS.getKey())
            .build();

    static {
        RebarItem.register(CargoMonitor.Item.class, CARGO_MONITOR)
                .withBlock(CargoMonitor.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_METER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_METER)
            .set(DataComponentTypes.ITEM_MODEL, Material.LIGHT_BLUE_STAINED_GLASS.getKey())
            .build();

    static {
        RebarItem.register(CargoMeter.Item.class, CARGO_METER)
                .withBlock(CargoMeter.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_SPLITTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_SPLITTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.STRIPPED_CRIMSON_STEM.getKey())
            .build();

    static {
        RebarItem.register(CargoSplitter.Item.class, CARGO_SPLITTER)
                .withBlock(CargoSplitter.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_MERGER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_MERGER)
            .set(DataComponentTypes.ITEM_MODEL, Material.STRIPPED_WARPED_STEM.getKey())
            .build();

    static {
        RebarItem.register(CargoMerger.Item.class, CARGO_MERGER)
                .withBlock(CargoMerger.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_FILTER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_FILTER)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPARATOR.getKey())
            .build();

    static {
        RebarItem.register(CargoFilter.Item.class, CARGO_FILTER)
                .withBlock(CargoFilter.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_OVERFLOW_GATE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_OVERFLOW_GATE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRIMSON_STEM.getKey())
            .build();

    static {
        RebarItem.register(CargoOverflowGate.Item.class, CARGO_OVERFLOW_GATE)
                .withBlock(CargoOverflowGate.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_GATE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_GATE)
            .set(DataComponentTypes.ITEM_MODEL, Material.REPEATER.getKey())
            .build();

    static {
        RebarItem.register(CargoGate.Item.class, CARGO_GATE)
                .withBlock(CargoGate.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_ACCUMULATOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_ACCUMULATOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.REDSTONE_LAMP.getKey())
            .build();

    static {
        RebarItem.register(CargoAccumulator.Item.class, CARGO_ACCUMULATOR)
                .withBlock(CargoAccumulator.class)
                .withGuidePage(PylonPages.CARGO);
    }

    public static final ItemStack CARGO_FLUID_ACCUMULATOR = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CARGO_FLUID_ACCUMULATOR)
            .set(DataComponentTypes.ITEM_MODEL, Material.NOTE_BLOCK.getKey())
            .build();

    static {
        RebarItem.register(CargoFluidAccumulator.Item.class, CARGO_FLUID_ACCUMULATOR)
                .withBlock(CargoFluidAccumulator.class)
                .withGuidePage(PylonPages.CARGO);
    }
    //</editor-fold>

    //<editor-fold desc="Fluid Pipes and Tanks" defaultstate=collapsed>

    public static final ItemStack FLUID_PIPE_WOOD = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_WOOD)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_WOOD).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_WOOD)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_COPPER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_COPPER)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_COPPER).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_COPPER)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_TIN = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_TIN)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_TIN).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_TIN)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_IRON)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_IRON).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_IRON)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_BRONZE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_BRONZE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_BRONZE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_BRONZE)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_OBSIDIAN = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_OBSIDIAN)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_OBSIDIAN).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_OBSIDIAN)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_IGNEOUS_COMPOSITE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_IGNEOUS_COMPOSITE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_IGNEOUS_COMPOSITE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_IGNEOUS_COMPOSITE)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack FLUID_PIPE_STEEL = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_STEEL)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_STEEL).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_STEEL)
                .withGuidePage(PylonPages.FLUID_PIPES_AND_TANKS);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_WOOD
            = ItemStackBuilder.rebar(Material.BROWN_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_WOOD)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .addCustomModelDataString("pylon:fluid:empty")
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
            .addCustomModelDataString("pylon:fluid:empty")
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
            .addCustomModelDataString("pylon:fluid:empty")
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
            .addCustomModelDataString("pylon:fluid:empty")
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
            .addCustomModelDataString("pylon:fluid:empty")
            .build();

    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_BRONZE,
                PylonKeys.PORTABLE_FLUID_TANK_BRONZE
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_BRONZE);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_OBSIDIAN
            = ItemStackBuilder.rebar(Material.BLACK_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_OBSIDIAN)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .addCustomModelDataString("pylon:fluid:empty")
            .build();

    static {
        RebarItem.register(
                PortableFluidTank.Item.class,
                PORTABLE_FLUID_TANK_OBSIDIAN,
                PylonKeys.PORTABLE_FLUID_TANK_OBSIDIAN
        );
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(PORTABLE_FLUID_TANK_OBSIDIAN);
    }

    public static final ItemStack PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE
            = ItemStackBuilder.rebar(Material.BLACK_STAINED_GLASS, PylonKeys.PORTABLE_FLUID_TANK_IGNEOUS_COMPOSITE)
            .editPdc(pdc -> pdc.set(PortableFluidTank.Item.FLUID_AMOUNT_KEY, RebarSerializers.DOUBLE, 0.0))
            .addCustomModelDataString("pylon:fluid:empty")
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
            .addCustomModelDataString("pylon:fluid:empty")
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

    public static final ItemStack FLUID_TANK_CASING_OBSIDIAN
            = ItemStackBuilder.rebar(Material.BLACK_STAINED_GLASS, PylonKeys.FLUID_TANK_CASING_OBSIDIAN)
            .build();

    static {
        RebarItem.register(FluidTankCasing.Item.class, FLUID_TANK_CASING_OBSIDIAN, PylonKeys.FLUID_TANK_CASING_OBSIDIAN);
        PylonPages.FLUID_PIPES_AND_TANKS.addItem(FLUID_TANK_CASING_OBSIDIAN);
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

    public static final ItemStack SMOKESTACK = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.SMOKESTACK)
            .build();
    static {
        RebarItem.register(Smokestack.Item.class, SMOKESTACK, PylonKeys.SMOKESTACK);
        PylonPages.FLUID_MACHINES.addItem(SMOKESTACK);
    }

    public static final ItemStack FLARE_STACK = ItemStackBuilder.rebar(Material.NETHER_BRICKS, PylonKeys.FLARE_STACK)
            .build();
    static {
        RebarItem.register(FlareStack.Item.class, FLARE_STACK, PylonKeys.FLARE_STACK);
        PylonPages.FLUID_MACHINES.addItem(FLARE_STACK);
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
        RebarGuide.getOrCreateInfoPage(PylonKeys.HYDRAULIC_PIPE_BENDER)
                .addButton(new MachineRecipesButton(PipeBendingRecipe.RECIPE_TYPE));
    }

    public static final ItemStack HYDRAULIC_TABLE_SAW = ItemStackBuilder.rebar(Material.WAXED_CUT_COPPER, PylonKeys.HYDRAULIC_TABLE_SAW)
            .build();

    static {
        RebarItem.register(HydraulicTableSaw.Item.class, HYDRAULIC_TABLE_SAW, PylonKeys.HYDRAULIC_TABLE_SAW);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_TABLE_SAW);
        RebarGuide.getOrCreateInfoPage(PylonKeys.HYDRAULIC_TABLE_SAW)
                .addButton(new MachineRecipesButton(TableSawRecipe.RECIPE_TYPE));
    }

    public static final ItemStack HYDRAULIC_FARMER = ItemStackBuilder.rebar(Material.WAXED_EXPOSED_COPPER_BULB, PylonKeys.HYDRAULIC_FARMER)
            .build();

    static {
        RebarItem.register(HydraulicFarmer.Item.class, HYDRAULIC_FARMER, PylonKeys.HYDRAULIC_FARMER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_FARMER);
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
        RebarGuide.getOrCreateInfoPage(PylonKeys.HYDRAULIC_REFUELING_STATION)
                .addButton(HydraulicRefuelableItemsPage.getButton());
    }

    public static final ItemStack HYDRAULIC_CORE_DRILL = ItemStackBuilder.rebar(Material.WAXED_COPPER_BULB, PylonKeys.HYDRAULIC_CORE_DRILL)
            .build();

    static {
        RebarItem.register(HydraulicCoreDrill.Item.class, HYDRAULIC_CORE_DRILL, PylonKeys.HYDRAULIC_CORE_DRILL);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_CORE_DRILL);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Hydraulic Purification" defaultstate="collapsed">

    public static final ItemStack MANUAL_HYDRAULIC_PURIFIER = ItemStackBuilder.rebar(Material.DECORATED_POT, PylonKeys.MANUAL_HYDRAULIC_PURIFIER)
            .build();

    static {
        RebarItem.register(ManualHydraulicPurifier.Item.class, MANUAL_HYDRAULIC_PURIFIER, PylonKeys.MANUAL_HYDRAULIC_PURIFIER);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(MANUAL_HYDRAULIC_PURIFIER);
    }

    public static final ItemStack SOLAR_HYDRAULIC_PURIFIER_1 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_1)
            .build();

    static {
        RebarItem.register(SolarHydraulicPurifier.Item.class, SOLAR_HYDRAULIC_PURIFIER_1, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_1);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_HYDRAULIC_PURIFIER_1);
    }

    public static final ItemStack SOLAR_HYDRAULIC_PURIFIER_2 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_2)
            .build();

    static {
        RebarItem.register(SolarHydraulicPurifier.Item.class, SOLAR_HYDRAULIC_PURIFIER_2, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_2);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_HYDRAULIC_PURIFIER_2);
    }

    public static final ItemStack SOLAR_HYDRAULIC_PURIFIER_3 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_3)
            .build();

    static {
        RebarItem.register(SolarHydraulicPurifier.Item.class, SOLAR_HYDRAULIC_PURIFIER_3, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_3);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_HYDRAULIC_PURIFIER_3);
    }

    public static final ItemStack SOLAR_HYDRAULIC_PURIFIER_4 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_4)
            .build();

    static {
        RebarItem.register(SolarHydraulicPurifier.Item.class, SOLAR_HYDRAULIC_PURIFIER_4, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_4);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_HYDRAULIC_PURIFIER_4);
    }

    public static final ItemStack SOLAR_HYDRAULIC_PURIFIER_5 = ItemStackBuilder.rebar(Material.WAXED_COPPER_BLOCK, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_5)
            .build();

    static {
        RebarItem.register(SolarHydraulicPurifier.Item.class, SOLAR_HYDRAULIC_PURIFIER_5, PylonKeys.SOLAR_HYDRAULIC_PURIFIER_5);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(SOLAR_HYDRAULIC_PURIFIER_5);
    }

    public static final ItemStack BURNER_HYDRAULIC_PURIFIER = ItemStackBuilder.rebar(Material.BLAST_FURNACE, PylonKeys.BURNER_HYDRAULIC_PURIFIER)
            .build();

    static {
        RebarItem.register(BurnerHydraulicPurifier.Item.class, BURNER_HYDRAULIC_PURIFIER, PylonKeys.BURNER_HYDRAULIC_PURIFIER);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(BURNER_HYDRAULIC_PURIFIER);
    }

    public static final ItemStack CONVECTION_HYDRAULIC_PURIFIER = ItemStackBuilder.rebar(Material.SMOOTH_STONE, PylonKeys.CONVECTION_HYDRAULIC_PURIFIER)
            .build();

    static {
        RebarItem.register(ConvectionHydraulicPurifier.Item.class, CONVECTION_HYDRAULIC_PURIFIER, PylonKeys.CONVECTION_HYDRAULIC_PURIFIER);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(CONVECTION_HYDRAULIC_PURIFIER);
    }

    public static final ItemStack LISELETTE_HYDRAULIC_PURIFIER = ItemStackBuilder.rebar(Material.OCHRE_FROGLIGHT, PylonKeys.LISELETTE_HYDRAULIC_PURIFIER)
            .build();

    static {
        RebarItem.register(LiseletteHydraulicPurifier.Item.class, LISELETTE_HYDRAULIC_PURIFIER, PylonKeys.LISELETTE_HYDRAULIC_PURIFIER);
        PylonPages.HYDRAULIC_PURIFICATION.addItem(LISELETTE_HYDRAULIC_PURIFIER);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Boilers" defaultstate="collapsed">

    public static final ItemStack SMALL_BOILER = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.SMALL_BOILER)
            .build();
    static {
        RebarItem.register(AbstractBoiler.Item.class, SMALL_BOILER, PylonKeys.SMALL_BOILER);
        PylonPages.BOILERS.addItem(SMALL_BOILER);
    }

    public static final ItemStack LARGE_BOILER = ItemStackBuilder.rebar(Material.BRICK_STAIRS, PylonKeys.LARGE_BOILER)
            .build();
    static {
        RebarItem.register(AbstractBoiler.Item.class, LARGE_BOILER, PylonKeys.LARGE_BOILER);
        PylonPages.BOILERS.addItem(LARGE_BOILER);
    }

    public static final ItemStack SMALL_FLUID_BOILER = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.SMALL_FLUID_BOILER)
            .build();
    static {
        RebarItem.register(AbstractBoiler.Item.class, SMALL_FLUID_BOILER, PylonKeys.SMALL_FLUID_BOILER);
        PylonPages.BOILERS.addItem(SMALL_FLUID_BOILER);
        RebarGuide.getOrCreateInfoPage(PylonKeys.SMALL_FLUID_BOILER)
                .addButton(LiquidFuelsPage.getButton());
    }

    public static final ItemStack LARGE_FLUID_BOILER = ItemStackBuilder.rebar(Material.BRICK_STAIRS, PylonKeys.LARGE_FLUID_BOILER)
            .build();
    static {
        RebarItem.register(AbstractBoiler.Item.class, LARGE_FLUID_BOILER, PylonKeys.LARGE_FLUID_BOILER);
        PylonPages.BOILERS.addItem(LARGE_FLUID_BOILER);
        RebarGuide.getOrCreateInfoPage(PylonKeys.LARGE_FLUID_BOILER)
                .addButton(LiquidFuelsPage.getButton());
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Petrochemicals" defaultstate=collapsed>

    public static final ItemStack OIL_SAMPLE_DRILL = ItemStackBuilder.rebar(Material.CHISELED_DEEPSLATE, PylonKeys.OIL_SAMPLE_DRILL)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_BULB.getKey())
            .build();
    static {
        RebarItem.register(OilSampleDrill.Item.class, OIL_SAMPLE_DRILL, PylonKeys.OIL_SAMPLE_DRILL);
        PylonPages.PETROCHEMICALS.addItem(OIL_SAMPLE_DRILL);
    }

    public static final ItemStack HYDRAULIC_FRACTURING_DRILL = ItemStackBuilder.rebar(Material.NETHERITE_BLOCK, PylonKeys.HYDRAULIC_FRACTURING_DRILL)
            .build();
    static {
        RebarItem.register(HydraulicFracturingDrill.Item.class, HYDRAULIC_FRACTURING_DRILL, PylonKeys.HYDRAULIC_FRACTURING_DRILL);
        PylonPages.PETROCHEMICALS.addItem(HYDRAULIC_FRACTURING_DRILL);
    }

    public static final ItemStack HYDRAULIC_FRACTURE_SEAL = ItemStackBuilder.rebar(Material.TNT, PylonKeys.HYDRAULIC_FRACTURE_SEAL)
            .build();
    static {
        RebarItem.register(HydraulicFractureSeal.Item.class, HYDRAULIC_FRACTURE_SEAL, PylonKeys.HYDRAULIC_FRACTURE_SEAL);
        PylonPages.PETROCHEMICALS.addItem(HYDRAULIC_FRACTURE_SEAL);
    }

    public static final ItemStack HYDRAULIC_PUMPJACK = ItemStackBuilder.rebar(Material.POLISHED_TUFF, PylonKeys.HYDRAULIC_PUMPJACK)
            .build();
    static {
        RebarItem.register(HydraulicPumpjack.Item.class, HYDRAULIC_PUMPJACK, PylonKeys.HYDRAULIC_PUMPJACK);
        PylonPages.PETROCHEMICALS.addItem(HYDRAULIC_PUMPJACK);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Diesel Machines" defaultstate="collapsed">

    public static final ItemStack DIESEL_GRINDSTONE = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_GRINDSTONE)
            .set(DataComponentTypes.ITEM_MODEL, Material.SMOOTH_STONE_SLAB.getKey())
            .build();

    static {
        RebarItem.register(DieselGrindstone.Item.class, DIESEL_GRINDSTONE, PylonKeys.DIESEL_GRINDSTONE);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_GRINDSTONE);
        RebarGuide.getOrCreateInfoPage(PylonKeys.DIESEL_GRINDSTONE)
                .addButton(new MachineRecipesButton(GrindstoneRecipe.RECIPE_TYPE));
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
        RebarGuide.getOrCreateInfoPage(PylonKeys.DIESEL_PRESS)
                .addButton(PressableItemsPage.getButton());
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
        RebarGuide.getOrCreateInfoPage(PylonKeys.DIESEL_PIPE_BENDER)
                .addButton(new MachineRecipesButton(PipeBendingRecipe.RECIPE_TYPE));
    }

    public static final ItemStack DIESEL_TABLE_SAW = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_TABLE_SAW)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_BARS.getKey())
            .build();

    static {
        RebarItem.register(DieselTableSaw.Item.class, DIESEL_TABLE_SAW, PylonKeys.DIESEL_TABLE_SAW);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_TABLE_SAW);
        RebarGuide.getOrCreateInfoPage(PylonKeys.DIESEL_TABLE_SAW)
                .addButton(new MachineRecipesButton(TableSawRecipe.RECIPE_TYPE));
    }

    public static final ItemStack DIESEL_QUARRY = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_QUARRY)
            .set(DataComponentTypes.ITEM_MODEL, Material.YELLOW_TERRACOTTA.getKey())
            .build();

    static {
        RebarItem.register(DieselQuarry.Item.class, DIESEL_QUARRY, PylonKeys.DIESEL_QUARRY);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_QUARRY);
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
        RebarGuide.getOrCreateInfoPage(PylonKeys.DIESEL_BRICK_MOLDER)
                .addButton(new MachineRecipesButton(MoldingRecipe.RECIPE_TYPE));
    }

    public static final ItemStack DIESEL_CORE_DRILL = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.DIESEL_CORE_DRILL)
            .build();

    static {
        RebarItem.register(DieselCoreDrill.Item.class, DIESEL_CORE_DRILL, PylonKeys.DIESEL_CORE_DRILL);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_CORE_DRILL);
    }

    public static final ItemStack DIESEL_REFUELING_STATION = ItemStackBuilder.rebar(Material.QUARTZ_SLAB, PylonKeys.DIESEL_REFUELING_STATION)
            .build();

    static {
        RebarItem.register(RebarItem.class, DIESEL_REFUELING_STATION, PylonKeys.DIESEL_REFUELING_STATION);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_REFUELING_STATION);
    }

    //</editor-fold>

    //<editor-fold desc="Machines - Diesel Production" defaultstate="collapsed">

    public static final ItemStack FERMENTER = ItemStackBuilder.rebar(Material.PINK_TERRACOTTA, PylonKeys.FERMENTER)
            .build();

    static {
        RebarItem.register(Fermenter.Item.class, FERMENTER, PylonKeys.FERMENTER);
        PylonPages.DIESEL_PRODUCTION.addItem(FERMENTER);
    }

    public static final ItemStack BIOREFINERY = ItemStackBuilder.rebar(Material.PURPLE_TERRACOTTA, PylonKeys.BIOREFINERY)
            .build();

    static {
        RebarItem.register(Biorefinery.Item.class, BIOREFINERY, PylonKeys.BIOREFINERY);
        PylonPages.DIESEL_PRODUCTION.addItem(BIOREFINERY);
    }

    //</editor-fold>

    //<editor-fold desc="Assembling" defaultstate=collapsed>

    public static final ItemStack ASSEMBLY_TABLE = ItemStackBuilder.rebar(Material.ANVIL, PylonKeys.ASSEMBLY_TABLE)
            .set(DataComponentTypes.ITEM_MODEL, Material.CRAFTING_TABLE.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, ASSEMBLY_TABLE)
                .withBlock(AssemblyTable.class)
                .withGuidePage(PylonPages.ASSEMBLING)
                .withInfoPage(page -> page.addButton(new MachineRecipesButton(AssemblingRecipe.RECIPE_TYPE)));
    }

    public static final ItemStack COPPER_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.COPPER_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(PylonKeys.COPPER_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.COPPER_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();

    static {
        RebarItem.register(Screwdriver.class, COPPER_SCREWDRIVER)
                .withGuidePage(PylonPages.ASSEMBLING);
    }

    public static final ItemStack IRON_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.IRON_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(PylonKeys.IRON_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.IRON_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();

    static {
        RebarItem.register(Screwdriver.class, IRON_SCREWDRIVER)
                .withGuidePage(PylonPages.ASSEMBLING);
    }

    public static final ItemStack BRONZE_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.BRONZE_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.GOLDEN_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(PylonKeys.BRONZE_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.BRONZE_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();

    static {
        RebarItem.register(Screwdriver.class, BRONZE_SCREWDRIVER)
                .withGuidePage(PylonPages.ASSEMBLING);
    }

    public static final ItemStack STEEL_SCREWDRIVER = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.STEEL_SCREWDRIVER)
            .set(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_SHOVEL.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(PylonKeys.STEEL_SCREWDRIVER).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.STEEL_SCREWDRIVER).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.SCREWDRIVER)
            )
            .build();

    static {
        RebarItem.register(Screwdriver.class, STEEL_SCREWDRIVER)
                .withGuidePage(PylonPages.ASSEMBLING);
    }

    public static final ItemStack REDSTONE_SOLDERING_IRON = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.REDSTONE_SOLDERING_IRON)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPASS.getKey())
            .set(DataComponentTypes.MAX_STACK_SIZE, 1)
            .set(DataComponentTypes.DAMAGE, 0)
            .set(DataComponentTypes.MAX_DAMAGE, ConfigSection.fromSettings(PylonKeys.REDSTONE_SOLDERING_IRON).getOrThrow("durability", ConfigAdapter.INTEGER))
            .set(DataComponentTypes.USE_COOLDOWN,
                    UseCooldown.useCooldown(
                            ConfigSection.fromSettings(PylonKeys.REDSTONE_SOLDERING_IRON).getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER) / 20.0F
                    ).cooldownGroup(PylonKeys.REDSTONE_SOLDERING_IRON)
            )
            .build();

    static {
        RebarItem.register(RedstoneSolderingIron.class, REDSTONE_SOLDERING_IRON)
                .withGuidePage(PylonPages.ASSEMBLING);
    }

    //</editor-fold>

    //<editor-fold desc="Creative Items" defaultstate=collapsed>

    public static final ItemStack CREATIVE_FLUID_VOIDER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_FLUID_VOIDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_CONCRETE.getKey())
            .build();

    static {
        RebarItem.register(FluidVoider.Item.class, CREATIVE_FLUID_VOIDER)
                .withBlock(CreativeFluidSource.class)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    public static final ItemStack CREATIVE_FLUID_SOURCE = ItemStackBuilder.rebar(Material.PINK_CONCRETE, PylonKeys.CREATIVE_FLUID_SOURCE)
            .build();

    static {
        RebarItem.register(RebarItem.class, CREATIVE_FLUID_SOURCE)
                .withBlock(CreativeFluidSource.class)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    public static final ItemStack FLUID_PIPE_CREATIVE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.FLUID_PIPE_CREATIVE)
            .set(
                    DataComponentTypes.ITEM_MODEL,
                    ConfigSection.fromSettings(PylonKeys.FLUID_PIPE_CREATIVE).getOrThrow("material", ConfigAdapter.MATERIAL).key()
            )
            .build();

    static {
        RebarItem.register(FluidPipe.class, FLUID_PIPE_CREATIVE)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    public static final ItemStack CREATIVE_ITEM_SOURCE = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_ITEM_SOURCE)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_TERRACOTTA.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CREATIVE_ITEM_SOURCE)
                .withBlock(CreativeItemSource.class)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    public static final ItemStack CREATIVE_ITEM_VOIDER = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.CREATIVE_ITEM_VOIDER)
            .set(DataComponentTypes.ITEM_MODEL, Material.PINK_TERRACOTTA.getKey())
            .build();

    static {
        RebarItem.register(RebarItem.class, CREATIVE_ITEM_VOIDER)
                .withBlock(CreativeItemVoider.class)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    public static final ItemStack CREATIVE_POWER_SOURCE = ItemStackBuilder.rebar(Material.PINK_STAINED_GLASS, PylonKeys.CREATIVE_POWER_SOURCE)
            .build();

    static {
        RebarItem.register(RebarItem.class, CREATIVE_POWER_SOURCE)
                .withBlock(CreativePowerSource.class)
                .hideFromGuideUnlessAdmin()
                .withGuidePage(PylonPages.CREATIVE_ITEMS);
    }

    //</editor-fold>

    public static final ItemStack POTION_PEDESTAL = ItemStackBuilder.rebar(Material.END_STONE_BRICK_WALL, PylonKeys.POTION_PEDESTAL)
            .build();

    static {
        RebarItem.register(RebarItem.class, POTION_PEDESTAL, PylonKeys.POTION_PEDESTAL);
        PylonPages.SIMPLE_MACHINES.addItem(POTION_PEDESTAL);
    }

    public static final ItemStack POTION_ALTAR = ItemStackBuilder.rebar(Material.STONE_BRICK_SLAB, PylonKeys.POTION_ALTAR)
            .build();

    static {
        RebarItem.register(PotionAltar.Item.class, POTION_ALTAR, PylonKeys.POTION_ALTAR);
        PylonPages.SIMPLE_MACHINES.addItem(POTION_ALTAR);
    }

    public static final ItemStack ASCENDANT_EMBER = ItemStackBuilder.rebar(Material.BLAZE_POWDER, PylonKeys.ASCENDANT_EMBER)
            .build();

    static {
        RebarItem.register(AscendantEmber.class, ASCENDANT_EMBER, PylonKeys.ASCENDANT_EMBER);
        PylonPages.MAGIC.addItem(ASCENDANT_EMBER);
    }

    public static final ItemStack CHRONICLE_RESIN = ItemStackBuilder.rebar(Material.RESIN_CLUMP, PylonKeys.CHRONICLE_RESIN)
            .build();

    static {
        RebarItem.register(ChronicleResin.class, CHRONICLE_RESIN, PylonKeys.CHRONICLE_RESIN);
        PylonPages.MAGIC.addItem(CHRONICLE_RESIN);
    }

    public static final ItemStack EON_WEAVE_CRYSTAL = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.EON_WEAVE_CRYSTAL)
            .set(DataComponentTypes.ITEM_MODEL, Material.END_CRYSTAL.getKey())
            .build();

    static {
        RebarItem.register(EonWeaveCrystal.class, EON_WEAVE_CRYSTAL, PylonKeys.EON_WEAVE_CRYSTAL);
        PylonPages.MAGIC.addItem(EON_WEAVE_CRYSTAL);
    }

    public static final ItemStack CLEANSING_POTION = ItemStackBuilder.rebar(Material.SPLASH_POTION, PylonKeys.CLEANSING_POTION)
            .set(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents()
                    .customColor(Color.FUCHSIA)
                    .build())
            .build();

    static {
        RebarItem.register(CleansingPotion.class, CLEANSING_POTION);
        PylonPages.TOOLS.addItem(CLEANSING_POTION);
    }

    public static final ItemStack EXPERIENCE_DRAIN = ItemStackBuilder.rebar(Material.BLACKSTONE_SLAB, PylonKeys.EXPERIENCE_DRAIN)
            .build();

    static {
        RebarItem.register(ExperienceDrain.Item.class, EXPERIENCE_DRAIN, PylonKeys.EXPERIENCE_DRAIN);
        PylonPages.FLUID_MACHINES.addItem(EXPERIENCE_DRAIN);
    }

    public static final ItemStack EXPERIENCE_FOUNTAIN = ItemStackBuilder.rebar(Material.END_STONE, PylonKeys.EXPERIENCE_FOUNTAIN)
            .build();

    static {
        RebarItem.register(ExperienceFountain.Item.class, EXPERIENCE_FOUNTAIN, PylonKeys.EXPERIENCE_FOUNTAIN);
        PylonPages.FLUID_MACHINES.addItem(EXPERIENCE_FOUNTAIN);
    }

    public static final ItemStack EXPERIENCE_FOUNTAIN_SPOUT = ItemStackBuilder.rebar(Material.END_ROD, PylonKeys.EXPERIENCE_FOUNTAIN_SPOUT)
            .build();

    static {
        RebarItem.register(RebarItem.class, EXPERIENCE_FOUNTAIN_SPOUT, PylonKeys.EXPERIENCE_FOUNTAIN_SPOUT);
        PylonPages.FLUID_MACHINES.addItem(EXPERIENCE_FOUNTAIN_SPOUT);
    }

    public static final ItemStack HYDRAULIC_EXPERIENCE_BOTTLER = ItemStackBuilder.rebar(Material.BREWING_STAND, PylonKeys.HYDRAULIC_EXPERIENCE_BOTTLER)
            .build();

    static {
        RebarItem.register(FluidExperienceBottler.Item.class, HYDRAULIC_EXPERIENCE_BOTTLER, PylonKeys.HYDRAULIC_EXPERIENCE_BOTTLER);
        PylonPages.HYDRAULIC_MACHINES.addItem(HYDRAULIC_EXPERIENCE_BOTTLER);
    }

    public static final ItemStack DIESEL_EXPERIENCE_BOTTLER = ItemStackBuilder.rebar(Material.BREWING_STAND, PylonKeys.DIESEL_EXPERIENCE_BOTTLER)
            .build();

    static {
        RebarItem.register(FluidExperienceBottler.Item.class, DIESEL_EXPERIENCE_BOTTLER, PylonKeys.DIESEL_EXPERIENCE_BOTTLER);
        PylonPages.DIESEL_MACHINES.addItem(DIESEL_EXPERIENCE_BOTTLER);
    }

    public static final ItemStack LIQUID_XP_BOTTLE = ItemStackBuilder.rebar(Material.EXPERIENCE_BOTTLE, PylonKeys.LIQUID_XP_BOTTLE)
            .build();

    static {
        RebarItem.register(LiquidXPBottle.class, LIQUID_XP_BOTTLE);
        PylonPages.MAGIC.addItem(LIQUID_XP_BOTTLE);
    }

    public static final ItemStack LIQUID_XP_BOTTLE_SUPER = ItemStackBuilder.rebar(Material.EXPERIENCE_BOTTLE, PylonKeys.LIQUID_XP_BOTTLE_SUPER)
            .build();

    static {
        RebarItem.register(LiquidXPBottle.class, LIQUID_XP_BOTTLE_SUPER);
        PylonPages.MAGIC.addItem(LIQUID_XP_BOTTLE_SUPER);
    }

    public static final ItemStack LIQUID_XP_BOTTLE_ULTRA = ItemStackBuilder.rebar(Material.EXPERIENCE_BOTTLE, PylonKeys.LIQUID_XP_BOTTLE_ULTRA)
            .build();

    static {
        RebarItem.register(LiquidXPBottle.class, LIQUID_XP_BOTTLE_ULTRA);
        PylonPages.MAGIC.addItem(LIQUID_XP_BOTTLE_ULTRA);
    }

    // Only used for displaying ghost blocks; unobtainable
    public static final ItemStack HYDRAULIC_FRACTURE = ItemStackBuilder.rebar(Material.LIGHT_BLUE_CONCRETE_POWDER, PylonKeys.HYDRAULIC_FRACTURE)
            .build();
    static {
        RebarItem.register(RebarItem.class, HYDRAULIC_FRACTURE, PylonKeys.HYDRAULIC_FRACTURE);
    }

    // <editor-fold desc="Electricity" defaultstate=collapsed>
    public static final ItemStack WIRE_HUB = ItemStackBuilder.rebar(Material.STRUCTURE_VOID, PylonKeys.WIRE_HUB)
            .set(DataComponentTypes.ITEM_MODEL, Material.COPPER_BULB.key())
            .build();

    static {
        RebarItem.register(RebarItem.class, WIRE_HUB, PylonKeys.WIRE_HUB);
        PylonPages.ELECTRIC_UTILITIES.addItem(WIRE_HUB);
    }

    public static final ItemStack CAPACITOR_1_KJ = ItemStackBuilder.rebar(Material.BLUE_GLAZED_TERRACOTTA, PylonKeys.CAPACITOR_1_KJ)
            .build();

    static {
        RebarItem.register(Capacitor.Item.class, CAPACITOR_1_KJ, PylonKeys.CAPACITOR_1_KJ);
        PylonPages.ELECTRIC_UTILITIES.addItem(CAPACITOR_1_KJ);
    }

    public static final ItemStack WIRE_1_GAUGE = ItemStackBuilder.rebar(Material.CLAY_BALL, PylonKeys.WIRE_1_GAUGE)
            .set(DataComponentTypes.ITEM_MODEL, Material.STRING.key())
            .build();

    static {
        RebarItem.register(PylonWire.class, WIRE_1_GAUGE, PylonKeys.WIRE_1_GAUGE);
        PylonPages.ELECTRIC_UTILITIES.addItem(WIRE_1_GAUGE);
    }


    public static final ItemStack ELECTRICITY_INPUT_HATCH = ItemStackBuilder.rebar(Material.GREEN_CONCRETE, PylonKeys.ELECTRICITY_INPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, ELECTRICITY_INPUT_HATCH, PylonKeys.ELECTRICITY_INPUT_HATCH);
        PylonPages.COMPONENTS.addItem(ELECTRICITY_INPUT_HATCH);
    }

    public static final ItemStack ELECTRICITY_OUTPUT_HATCH = ItemStackBuilder.rebar(Material.ORANGE_CONCRETE, PylonKeys.ELECTRICITY_OUTPUT_HATCH)
            .build();

    static {
        RebarItem.register(RebarItem.class, ELECTRICITY_OUTPUT_HATCH, PylonKeys.ELECTRICITY_OUTPUT_HATCH);
        PylonPages.COMPONENTS.addItem(ELECTRICITY_OUTPUT_HATCH);
    }

    public static final ItemStack STEAM_ENGINE = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.STEAM_ENGINE)
            .build();

    static {
        RebarItem.register(SteamEngine.Item.class, STEAM_ENGINE, PylonKeys.STEAM_ENGINE);
        PylonPages.ELECTRIC_GENERATORS.addItem(STEAM_ENGINE);
    }

    public static final ItemStack GAS_TURBINE = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.GAS_TURBINE)
            .build();

    static {
        RebarItem.register(RebarItem.class, GAS_TURBINE, PylonKeys.GAS_TURBINE);
        PylonPages.ELECTRIC_GENERATORS.addItem(GAS_TURBINE);
        RebarGuide.getOrCreateInfoPage(PylonKeys.GAS_TURBINE)
                .addButton(new MachineRecipesButton(GasTurbineRecipe.RECIPE_TYPE));
    }

    public static final ItemStack COMBUSTION_TOWER = ItemStackBuilder.rebar(Material.BRICKS, PylonKeys.COMBUSTION_TOWER)
            .build();

    static {
        RebarItem.register(CombustionTower.Item.class, COMBUSTION_TOWER, PylonKeys.COMBUSTION_TOWER);
        PylonPages.ELECTRIC_GENERATORS.addItem(COMBUSTION_TOWER);
    }

    public static final ItemStack HEAT_EXCHANGER = ItemStackBuilder.rebar(Material.RED_NETHER_BRICK_WALL, PylonKeys.HEAT_EXCHANGER)
            .build();

    static {
        RebarItem.register(RebarItem.class, HEAT_EXCHANGER, PylonKeys.HEAT_EXCHANGER);
        PylonPages.ELECTRIC_GENERATORS.addItem(HEAT_EXCHANGER);
    }

    public static final ItemStack ELECTRIC_GRINDSTONE = ItemStackBuilder.rebar(Material.SMOOTH_STONE, PylonKeys.ELECTRIC_GRINDSTONE)
            .build();

    static {
        RebarItem.register(ElectricGrindstone.Item.class, ELECTRIC_GRINDSTONE, PylonKeys.ELECTRIC_GRINDSTONE);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_GRINDSTONE);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_GRINDSTONE)
                .addButton(new MachineRecipesButton(GrindstoneRecipe.RECIPE_TYPE));
    }

    public static final ItemStack ELECTRIC_BRICK_MOLDER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_BRICK_MOLDER)
            .build();

    static {
        RebarItem.register(ElectricBrickMolder.Item.class, ELECTRIC_BRICK_MOLDER, PylonKeys.ELECTRIC_BRICK_MOLDER);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_BRICK_MOLDER);
    }

    public static final ItemStack ELECTRIC_COMPRESSOR = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_COMPRESSOR)
            .build();

    static {
        RebarItem.register(ElectricCompressor.Item.class, ELECTRIC_COMPRESSOR, PylonKeys.ELECTRIC_COMPRESSOR);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_COMPRESSOR);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_COMPRESSOR)
                .addButton(new MachineRecipesButton(HammerRecipe.RECIPE_TYPE));
    }

    public static final ItemStack ELECTRIC_MIXER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_MIXER)
            .build();

    static {
        RebarItem.register(ElectricMixer.Item.class, ELECTRIC_MIXER, PylonKeys.ELECTRIC_MIXER);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_MIXER);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_MIXER)
                .addButton(new MachineRecipesButton(MixingPotRecipe.RECIPE_TYPE));
    }

    public static final ItemStack ELECTRIC_PRESS = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_PRESS)
            .set(DataComponentTypes.ITEM_MODEL, Material.COMPOSTER.getKey())
            .build();

    static {
        RebarItem.register(ElectricPress.Item.class, ELECTRIC_PRESS, PylonKeys.ELECTRIC_PRESS);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_PRESS);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_PRESS)
                .addButton(PressableItemsPage.getButton());
    }

    public static final ItemStack ELECTRIC_TABLE_SAW = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_TABLE_SAW)
            .set(DataComponentTypes.ITEM_MODEL, Material.IRON_BARS.getKey())
            .build();

    static {
        RebarItem.register(ElectricTableSaw.Item.class, ELECTRIC_TABLE_SAW, PylonKeys.ELECTRIC_TABLE_SAW);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_TABLE_SAW);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_TABLE_SAW)
                .addButton(new MachineRecipesButton(TableSawRecipe.RECIPE_TYPE));
    }

    public static final ItemStack ELECTRIC_QUARRY = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_QUARRY)
            .build();

    static {
        RebarItem.register(ElectricQuarry.Item.class, ELECTRIC_QUARRY, PylonKeys.ELECTRIC_QUARRY);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_QUARRY);
    }

    public static final ItemStack ELECTRIC_PIPE_BENDER = ItemStackBuilder.rebar(Material.IRON_BLOCK, PylonKeys.ELECTRIC_PIPE_BENDER)
            .build();

    static {
        RebarItem.register(ElectricPipeBender.Item.class, ELECTRIC_PIPE_BENDER, PylonKeys.ELECTRIC_PIPE_BENDER);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_PIPE_BENDER);
        RebarGuide.getOrCreateInfoPage(PylonKeys.ELECTRIC_PIPE_BENDER)
                .addButton(new MachineRecipesButton(PipeBendingRecipe.RECIPE_TYPE));
    }

    public static final ItemStack ELECTRIC_BREAKER = ItemStackBuilder.rebar(Material.DISPENSER, PylonKeys.ELECTRIC_BREAKER)
            .build();

    static {
        RebarItem.register(ElectricBreaker.Item.class, ELECTRIC_BREAKER, PylonKeys.ELECTRIC_BREAKER);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_BREAKER);
    }

    public static final ItemStack ELECTRIC_FURNACE = ItemStackBuilder.rebar(Material.FURNACE, PylonKeys.ELECTRIC_FURNACE)
            .build();

    static {
        RebarItem.register(ElectricFurnace.Item.class, ELECTRIC_FURNACE, PylonKeys.ELECTRIC_FURNACE);
        PylonPages.ELECTRIC_MACHINES.addItem(ELECTRIC_FURNACE);
    }
    // </editor-fold>

    static {
        PylonPages.initialise();
        PylonHelpPages.initialise();
    }

    // Calling this method forces all the static blocks to run, which initializes our items
    public static void initialize() {
    }
}
