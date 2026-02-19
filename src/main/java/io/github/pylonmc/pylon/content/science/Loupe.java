package io.github.pylonmc.pylon.content.science;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.event.LoupeCompleteScanningEvent;
import io.github.pylonmc.pylon.event.LoupeStartScanningEvent;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.config.Config;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarConsumable;
import io.github.pylonmc.rebar.item.base.RebarInteractor;
import io.github.pylonmc.rebar.item.research.Research;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


@SuppressWarnings("UnstableApiUsage")
public final class Loupe extends RebarItem implements RebarInteractor, RebarConsumable {

    public static final NamespacedKey CONSUMED_KEY = pylonKey("consumed");
    public static final PersistentDataType<PersistentDataContainer, Map<NamespacedKey, Integer>> CONSUMED_TYPE =
            RebarSerializers.MAP.mapTypeFrom(
                    RebarSerializers.NAMESPACED_KEY,
                    RebarSerializers.INTEGER
            );

    public static final NamespacedKey EXAMINED_KEY = pylonKey("examined");
    public static final ListPersistentDataType<long[], UUID> EXAMINED_TYPE =
            RebarSerializers.LIST.listTypeFrom(RebarSerializers.UUID);
    public static final PersistentDataType<PersistentDataContainer, Map<Long, List<UUID>>> CHUNK_EXAMINED_TYPE =
            RebarSerializers.MAP.mapTypeFrom(
                    RebarSerializers.LONG,
                    EXAMINED_TYPE
            );

    public static final Map<ItemRarity, EntryConfig> ITEM_CONFIGS;
    public static final Map<Material, EntryConfig> ITEM_OVERRIDES;

    public static final EntryConfig ENTITY_DEFAULT_CONFIG;
    public static final Map<Tag<EntityType>, EntryConfig> ENTITY_CONFIGS;
    public static final Map<EntityType, EntryConfig> ENTITY_OVERRIDES;

    private static final Map<UUID, RayTraceResult> SCANNING = new HashMap<>();

    static {
        Config loupeConfig = Settings.get(PylonKeys.LOUPE);

        ConfigSection itemOverridesConfig = loupeConfig.getSection("item_overrides");
        Map<ItemRarity, EntryConfig> itemConfigs = new EnumMap<>(ItemRarity.class);
        Map<Material, EntryConfig> itemOverrides = new EnumMap<>(Material.class);
        for (ItemRarity rarity : ItemRarity.values()) {
            itemConfigs.put(
                    rarity,
                    EntryConfig.loadFrom(loupeConfig.getSectionOrThrow("items." + rarity.name().toLowerCase(Locale.ROOT)))
            );
        }
        if (itemOverridesConfig != null) {
            for (String type : itemOverridesConfig.getKeys()) {
                Material material = Registry.MATERIAL.get(Key.key(type));
                if (material == null) {
                    Pylon.getInstance().getLogger().warning("Invalid item type '" + type + "' under loupe.yml/item_overrides, skipping!");
                    continue;
                }

                itemOverrides.put(material, EntryConfig.loadFrom(itemOverridesConfig.getSectionOrThrow(type)));
            }
        }

        ConfigSection entities = loupeConfig.getSectionOrThrow("entities");
        ConfigSection entityOverridesConfig = loupeConfig.getSection("entity_overrides");
        EntryConfig entityDefaultConfig = EntryConfig.loadFrom(loupeConfig.getSectionOrThrow("entity_default"));
        Map<Tag<EntityType>, EntryConfig> entityConfigs = new HashMap<>();
        Map<EntityType, EntryConfig> entityOverrides = new EnumMap<>(EntityType.class);
        for (String tagKey : entities.getKeys()) {
            try {
                Tag<EntityType> tag = Registry.ENTITY_TYPE.getTag(TagKey.create(RegistryKey.ENTITY_TYPE, tagKey));
                entityConfigs.put(tag, EntryConfig.loadFrom(entities.getSectionOrThrow(tagKey)));
            } catch (Exception e) {
                Pylon.getInstance().getLogger().warning("Invalid entity tag '" + tagKey + "' under loupe.yml/entities, skipping!");
            }
        }
        if (entityOverridesConfig != null) {
            for (String type : entityOverridesConfig.getKeys()) {
                EntityType entityType = Registry.ENTITY_TYPE.get(Key.key(type));
                if (entityType == null) {
                    Pylon.getInstance().getLogger().warning("Invalid entity type '" + type + "' under loupe.yml/entity_overrides, skipping!");
                    continue;
                }

                entityOverrides.put(entityType, EntryConfig.loadFrom(entityOverridesConfig.getSectionOrThrow(type)));
            }
        }

        ITEM_CONFIGS = Map.copyOf(itemConfigs);
        ITEM_OVERRIDES = Map.copyOf(itemOverrides);

        ENTITY_DEFAULT_CONFIG = entityDefaultConfig;
        ENTITY_CONFIGS = Map.copyOf(entityConfigs);
        ENTITY_OVERRIDES = Map.copyOf(entityOverrides);
    }

    public final int cooldownTicks = getSettings().getOrThrow("cooldown-ticks", ConfigAdapter.INTEGER);

    public Loupe(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onUsedToClick(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick() || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        RayTraceResult scan = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5,
                player.isUnderWater() ? FluidCollisionMode.NEVER : FluidCollisionMode.SOURCE_ONLY, false, 0.25, hit -> hit != player);
        if (scan == null) {
            return;
        }

        RayTraceResult initialScan = SCANNING.get(player.getUniqueId());
        if (initialScan != null && Objects.equals(scan.getHitBlock(), initialScan.getHitBlock()) && Objects.equals(scan.getHitEntity(), initialScan.getHitEntity())) {
            return;
        }

        LoupeStartScanningEvent scanEvent = new LoupeStartScanningEvent(player, scan);
        if (!scanEvent.callEvent()) {
            return;
        }

        if (scanEvent.isCustomHandled()) {
            SCANNING.put(player.getUniqueId(), scan);
        } else if (scan.getHitEntity() instanceof Item hit) {
            ItemStack stack = hit.getItemStack();
            if (RebarItem.fromStack(stack) != null) {
                player.sendActionBar(message("is_pylon"));
            } else if (!stack.getPersistentDataContainer().isEmpty()) {
                player.sendActionBar(message("is_other_plugin"));
            } else if (!hasUses(player, stack.getType())) {
                player.sendActionBar(message("max_uses"));
            } else {
                player.playSound(Sound.sound(SoundEventKeys.BLOCK_BELL_RESONATE, Sound.Source.PLAYER, 1f, 0.7f));
                player.sendActionBar(message("examining", RebarArgument.of("object", stack.effectiveName())));
                SCANNING.put(player.getUniqueId(), scan);
            }
        } else if (scan.getHitEntity() instanceof LivingEntity entity) {
            if (!player.canSee(entity) || entity.isInvisible() || entity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                return;
            }

            RebarArgument entityArg = RebarArgument.of("object", Component.translatable(entity.getType().translationKey()));
            if (entity instanceof Player) {
                player.sendActionBar(message("is_player"));
            } else if (alreadyExamined(player, entity)) {
                player.sendActionBar(message("already_examined", entityArg));
            } else if (!entity.getPersistentDataContainer().isEmpty() && (!entity.getPersistentDataContainer().has(EXAMINED_KEY) || entity.getPersistentDataContainer().getKeys().size() > 1)) {
                player.sendActionBar(message("is_other_plugin"));
            } else if (!hasUses(player, entity.getType())) {
                player.sendActionBar(message("max_uses"));
            } else {
                player.playSound(Sound.sound(SoundEventKeys.BLOCK_BELL_RESONATE, Sound.Source.PLAYER, 1f, 0.7f));
                player.sendActionBar(message("examining", entityArg));
                SCANNING.put(player.getUniqueId(), scan);
            }
        } else if (scan.getHitBlock() != null) {
            Block hit = scan.getHitBlock();
            Material type = hit.getType();
            if (BlockStorage.get(hit) != null) {
                player.sendActionBar(message("is_pylon"));
            } else if (!hasUses(player, type)) {
                player.sendActionBar(message("max_uses"));
            } else if (alreadyExamined(player, hit)) {
                player.sendActionBar(message("already_examined", RebarArgument.of("object", Component.translatable(type))));
            } else {
                player.playSound(Sound.sound(SoundEventKeys.BLOCK_BELL_RESONATE, Sound.Source.PLAYER, 1f, 0.7f));
                player.sendActionBar(message("examining", RebarArgument.of("object", Component.translatable(type))));
                SCANNING.put(player.getUniqueId(), scan);
            }
        }
    }

    @Override
    public void onConsumed(@NotNull PlayerItemConsumeEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        RayTraceResult initialScan = SCANNING.remove(player.getUniqueId());
        if (initialScan == null) {
            return;
        }

        RayTraceResult scan = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5,
                player.isUnderWater() ? FluidCollisionMode.NEVER : FluidCollisionMode.SOURCE_ONLY, false, 0.25, hit -> hit != player);
        if (scan == null || !Objects.equals(scan.getHitBlock(), initialScan.getHitBlock()) || !Objects.equals(scan.getHitEntity(), initialScan.getHitEntity())) {
            return;
        }

        LoupeCompleteScanningEvent scanEvent = new LoupeCompleteScanningEvent(player, scan);
        if (!scanEvent.callEvent()) {
            return;
        }

        if (scanEvent.isCustomHandled()) {
            //player.setCooldown(getStack(), cooldownTicks);
        } else if (scan.getHitEntity() instanceof Item hit) {
            ItemStack stack = hit.getItemStack();
            Material type = stack.getType();
            if (RebarItem.fromStack(stack) != null || !stack.getPersistentDataContainer().isEmpty() || !hasUses(player, type)) {
                player.sendMessage(message("examine_failed", RebarArgument.of("object", stack.effectiveName())));
                return;
            }

            PlayerAttemptPickupItemEvent pickupEvent = new PlayerAttemptPickupItemEvent(player, hit, stack.getAmount() - 1);
            if (!pickupEvent.callEvent()) {
                player.sendMessage(message("examine_failed", RebarArgument.of("object", stack.effectiveName())));
                return;
            }

            new ParticleBuilder(Particle.ITEM).data(stack).extra(0.05).count(16).location(hit.getLocation().add(0, hit.getHeight() / 2, 0)).spawn();
            hit.getWorld().playSound(Sound.sound(SoundEventKeys.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 0.5f, 1f), hit.getX(), hit.getY(), hit.getZ());
            if (stack.getAmount() == 1) {
                hit.remove();
            } else {
                stack.subtract();
                hit.setItemStack(stack);
            }
            addEntry(player, stack.effectiveName(), type.getKey(), getEntryConfig(type));
            //player.setCooldown(getStack(), cooldownTicks);
        } else if (scan.getHitEntity() instanceof LivingEntity entity) {
            RebarArgument entityArg = RebarArgument.of("object", Component.translatable(entity.getType().translationKey()));
            if (!player.canSee(entity) || entity.isInvisible() || entity.hasPotionEffect(PotionEffectType.INVISIBILITY) || entity instanceof Player || alreadyExamined(player, entity) || !hasUses(player, entity.getType())
                    || (!entity.getPersistentDataContainer().isEmpty() && (!entity.getPersistentDataContainer().has(EXAMINED_KEY) || entity.getPersistentDataContainer().getKeys().size() > 1))) {
                player.sendMessage(message("examine_failed", entityArg));
                return;
            }

            markAlreadyExamined(player, entity);
            addEntry(player, entityArg, entity.getType().getKey(), getEntryConfig(entity.getType()));
            //player.setCooldown(getStack(), cooldownTicks);
        } else if (scan.getHitBlock() != null) {
            Block hit = scan.getHitBlock();
            Material type = hit.getType();
            if (BlockStorage.get(hit) != null || !hasUses(player, type)) {
                player.sendMessage(message("examine_failed", RebarArgument.of("object", Component.translatable(type))));
                return;
            }

            // Permit unbreakable blocks, just don't try to break them
            if (type.getHardness() >= 0) {
                BlockBreakEvent breakEvent = new BlockBreakEvent(hit, player);
                breakEvent.setDropItems(false);
                breakEvent.setExpToDrop(0);
                if (!breakEvent.callEvent()) {
                    player.sendMessage(message("examine_failed", RebarArgument.of("object", Component.translatable(type))));
                    return;
                }

                hit.getWorld().playEffect(hit.getLocation(), Effect.STEP_SOUND, hit.getBlockData());
                hit.setType(Material.AIR, true);
            } else {
                // Prevents scanning the same instance of an unbreakable block again
                markAlreadyExamined(player, hit);
            }

            addEntry(player, Component.translatable(type), type.getKey(), getEntryConfig(type));
            //player.setCooldown(getStack(), cooldownTicks);
        }
    }

    private Component message(String key, RebarArgument... arguments) {
        return Component.translatable("pylon.message.loupe." + key, arguments);
    }

    private static long localChunkPosition(Block block) {
        long x = block.getX() & 0xFL;
        long z = block.getZ() & 0xFL;
        long y = block.getY() & 0xFFFFFFFFL;
        return (x << 48) | (y << 16) | z;
    }

    public static void markAlreadyExamined(Player player, Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        Map<Long, List<UUID>> examined = pdc.getOrDefault(EXAMINED_KEY, CHUNK_EXAMINED_TYPE, new HashMap<>());

        long localPos = localChunkPosition(block);
        List<UUID> examiners = examined.getOrDefault(localPos, new ArrayList<>());
        examiners.add(player.getUniqueId());
        examined.put(localPos, examiners);

        pdc.set(EXAMINED_KEY, CHUNK_EXAMINED_TYPE, examined);
    }

    public static void markAlreadyExamined(Player player, LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        List<UUID> examiners = pdc.getOrDefault(EXAMINED_KEY, EXAMINED_TYPE, new ArrayList<>());
        examiners.add(player.getUniqueId());
        pdc.set(EXAMINED_KEY, EXAMINED_TYPE, examiners);
    }

    public static boolean alreadyExamined(Player player, Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (!pdc.has(EXAMINED_KEY, CHUNK_EXAMINED_TYPE)) {
            return false;
        }

        Map<Long, List<UUID>> examined = pdc.getOrDefault(EXAMINED_KEY, CHUNK_EXAMINED_TYPE, Map.of());
        long localPos = localChunkPosition(block);
        return examined.containsKey(localPos) && examined.get(localPos).contains(player.getUniqueId());
    }

    public static boolean alreadyExamined(Player player, LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        return pdc.has(EXAMINED_KEY, EXAMINED_TYPE) && pdc.getOrDefault(EXAMINED_KEY, EXAMINED_TYPE, List.of()).contains(player.getUniqueId());
    }

    public static boolean hasUses(Player player, Material type) {
        var entries = player.getPersistentDataContainer().getOrDefault(CONSUMED_KEY, CONSUMED_TYPE, Map.of());
        int maxUses = getEntryConfig(type).uses;
        return entries.getOrDefault(type.getKey(), 0) < maxUses;
    }

    public static boolean hasUses(Player player, EntityType type) {
        var entries = player.getPersistentDataContainer().getOrDefault(CONSUMED_KEY, CONSUMED_TYPE, Map.of());
        int maxUses = getEntryConfig(type).uses;
        return entries.getOrDefault(type.getKey(), 0) < maxUses;
    }

    public static void addEntry(Player player, ComponentLike name, NamespacedKey type, EntryConfig config) {
        var entries = new HashMap<>(player.getPersistentDataContainer().getOrDefault(CONSUMED_KEY, CONSUMED_TYPE, Map.of()));

        entries.put(type, entries.getOrDefault(type, 0) + 1);
        player.getPersistentDataContainer().set(CONSUMED_KEY, CONSUMED_TYPE, entries);

        long totalPoints = Research.getResearchPoints(player) + config.points;
        Research.setResearchPoints(player, totalPoints);

        player.sendMessage(Component.translatable(
                "pylon.message.loupe.examined",
                RebarArgument.of("object", name)
        ));
        player.sendMessage(Component.translatable(
                "pylon.message.gained_research_points",
                RebarArgument.of("points", config.points),
                RebarArgument.of("total", totalPoints)
        ));
    }

    public static EntryConfig getEntryConfig(Material type) {
        EntryConfig override = ITEM_OVERRIDES.get(type);
        if (override != null) {
            return override;
        }

        ItemRarity rarity = type.isItem() ? type.getDefaultData(DataComponentTypes.RARITY) : ItemRarity.COMMON;
        return ITEM_CONFIGS.get(rarity);
    }

    public static EntryConfig getEntryConfig(EntityType type) {
        EntryConfig override = ENTITY_OVERRIDES.get(type);
        if (override != null) {
            return override;
        }

        // TODO: Maybe add a cache for this?
        TypedKey<EntityType> typeKey = TypedKey.create(RegistryKey.ENTITY_TYPE, type.getKey());
        for (Map.Entry<Tag<EntityType>, EntryConfig> entry : ENTITY_CONFIGS.entrySet()) {
            if (entry.getKey().contains(typeKey)) {
                return entry.getValue();
            }
        }
        return ENTITY_DEFAULT_CONFIG;
    }

    public record EntryConfig(int uses, int points) {
        public static EntryConfig loadFrom(ConfigSection section) {
            return new EntryConfig(
                    section.getOrThrow("uses", ConfigAdapter.INTEGER),
                    section.getOrThrow("points", ConfigAdapter.INTEGER)
            );
        }
    }
}
