package io.github.pylonmc.pylon.content.tools;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author balugaq
 */
@SuppressWarnings("UnstableApiUsage")
public class FireproofRune extends Rune {
    public static final Tag<DamageType> IS_FIRE_TAG = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getTag(DamageTypeTagKeys.IS_FIRE);

    public static final Component SUCCESS = Component.translatable("pylon.message.fireproof_result.success");
    public static final Component TOOLTIP = Component.translatable("pylon.message.fireproof_result.tooltip");

    private final RandomizedSound applySound = getSettingOrThrow("apply-sound", ConfigAdapter.RANDOMIZED_SOUND);

    public FireproofRune(@NotNull ItemStack stack) {
        super(stack);
    }

    @Override
    public boolean isRuneApplicable(@NotNull Player player, @NotNull Item runeItem, @NotNull Item item) {
        DamageResistant data = item.getItemStack().getData(DataComponentTypes.DAMAGE_RESISTANT);
        if (data == null) {
            return true;
        }

        RegistryKeySet<DamageType> types = data.types();
        for (TypedKey<DamageType> fireType : IS_FIRE_TAG.values()) {
            if (!types.contains(fireType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRuneApply(@NotNull Player player, @NotNull Item runeItem, @NotNull Item targetItem) {
        ItemStack targetItemStack = targetItem.getItemStack();
        int consumed = Math.min(getStack().getAmount(), targetItemStack.getAmount());

        ItemStack fireProof = ItemStackBuilder.of(targetItemStack.asQuantity(consumed))
                .editDataOrSet(DataComponentTypes.DAMAGE_RESISTANT, resistance -> {
                    if (resistance == null) {
                        return DamageResistant.damageResistant(IS_FIRE_TAG);
                    }
                    Set<TypedKey<DamageType>> types = new HashSet<>(resistance.types().values());
                    types.addAll(IS_FIRE_TAG.values());
                    return DamageResistant.damageResistant(RegistrySet.keySet(RegistryKey.DAMAGE_TYPE, types));
                })
                .lore(TOOLTIP)
                .build();

        int remainingRunes = getStack().getAmount() - consumed;
        int remainingTargets = targetItemStack.getAmount() - consumed;

        Location runeLocation = runeItem.getLocation();
        World world = runeItem.getWorld();
        if (remainingRunes > 0) {
            world.dropItemNaturally(runeLocation, getStack().asQuantity(remainingRunes)).setGlowing(true);
        }
        if (remainingTargets > 0) {
            world.dropItemNaturally(runeLocation, targetItemStack.asQuantity(remainingTargets)).setGlowing(true);
        }
        world.dropItemNaturally(runeLocation, fireProof).setGlowing(true);

        spawnParticles(Particle.EXPLOSION, runeLocation, 1);
        spawnParticles(Particle.FLAME, runeLocation, 50);
        spawnParticles(Particle.SMOKE, runeLocation, 40);
        applySound.play(runeLocation);

        runeItem.remove();
        targetItem.remove();
        player.sendMessage(SUCCESS);
    }

    public void spawnParticles(@NotNull Particle particle, @NotNull Location location, int count) {
        new ParticleBuilder(particle)
                .location(location)
                .offset(0, 0, 0)
                .count(count)
                .spawn();
    }
}
