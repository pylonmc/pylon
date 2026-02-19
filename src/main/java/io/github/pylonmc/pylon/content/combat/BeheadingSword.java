package io.github.pylonmc.pylon.content.combat;

import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.base.RebarWeapon;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


public class BeheadingSword extends RebarItem implements RebarWeapon {

    private final double normalEntityHeadChance = getSettings().getOrThrow("head-chance.normal-entity", ConfigAdapter.DOUBLE);
    private final double witherSkeletonHeadChance = getSettings().getOrThrow("head-chance.wither-skeleton", ConfigAdapter.DOUBLE);

    public BeheadingSword(@NotNull ItemStack stack) {
        super(stack);
    }

    private static final Map<EntityType, ItemStack> ENTITY_HEADS = Map.of(
            EntityType.WITHER_SKELETON, new ItemStack(Material.WITHER_SKELETON_SKULL),
            EntityType.PLAYER, new ItemStack(Material.PLAYER_HEAD),
            EntityType.CREEPER, new ItemStack(Material.CREEPER_HEAD),
            EntityType.PIGLIN, new ItemStack(Material.PIGLIN_HEAD),
            EntityType.ENDER_DRAGON, new ItemStack(Material.DRAGON_HEAD),
            EntityType.ZOMBIE, new ItemStack(Material.ZOMBIE_HEAD),
            EntityType.SKELETON, new ItemStack(Material.SKELETON_SKULL)
    );

    @Override @MultiHandler(ignoreCancelled = true)
    public void onUsedToKillEntity(@NotNull EntityDeathEvent event, @NotNull EventPriority priority) {
        ItemStack head = ENTITY_HEADS.get(event.getEntityType());
        if (head == null || event.getDrops().contains(head)) {
            return;
        }

        float chance = ThreadLocalRandom.current().nextFloat();
        if (event.getEntityType() == EntityType.WITHER_SKELETON) {
            if (chance < witherSkeletonHeadChance) {
                event.getDrops().add(head.clone());
            }
            return;
        } else if (chance > normalEntityHeadChance) {
            return;
        }

        head = head.clone();
        if (event.getEntity() instanceof Player player) {
            head.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
            for (ItemStack drop : event.getDrops()) {
                if (drop.getType() == Material.PLAYER_HEAD) {
                    return;
                }
            }
        }
        event.getDrops().add(head);
    }

    @Override
    public @NotNull List<RebarArgument> getPlaceholders() {
        return List.of(
                RebarArgument.of("default-chance", normalEntityHeadChance * 100),
                RebarArgument.of("wither-skeleton-chance", witherSkeletonHeadChance * 100)
        );
    }
}
