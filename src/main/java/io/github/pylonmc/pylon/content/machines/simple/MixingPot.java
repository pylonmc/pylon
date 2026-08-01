package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankWithDisplayEntity;
import io.github.pylonmc.pylon.recipes.MixingPotRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.CauldronRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.DirectionalRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.fluid.tags.FluidTemperature;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidOrItem;
import io.github.pylonmc.rebar.recipe.ingredient.FluidWithAmount;
import io.github.pylonmc.rebar.recipe.ingredient.ItemChoice;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.RandomizedSound;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.registry.keys.SoundEventKeys;
import kotlin.Pair;
import net.kyori.adventure.sound.Sound;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.List;


public final class MixingPot extends RebarBlock implements
        DirectionalRebarBlock,
        TickingRebarBlock,
        InteractRebarBlockHandler,
        FluidTankWithDisplayEntity,
        CauldronRebarBlockHandler {

    public static class MixingPotItem extends RebarItem {

        public MixingPotItem(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("capacity", UnitFormat.MILLIBUCKETS.format(1000))
            );
        }
    }

    @SuppressWarnings("unused")
    public MixingPot(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        createFluidDisplay();
        setFacing(context.getFacing());
        setTickInterval(1);
        setCapacity(1000.0);
        createFluidPoint(FluidPointType.INPUT, BlockFace.NORTH, context, false);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.SOUTH, context, false);
    }

    @SuppressWarnings("unused")
    public MixingPot(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true;
    }

    @Override @MultiHandler(priorities = EventPriority.LOWEST)
    public void onCauldronLevelChange(@NotNull CauldronLevelChangeEvent event, @NotNull EventPriority priority) {
        event.setCancelled(true);
    }

    @Override
    public @NotNull Vector3d fluidDisplayTranslation() {
        return new Vector3d(0, -0.2, 0);
    }

    @Override
    public @NotNull Vector3d fluidDisplayScale() {
        return new Vector3d(0.9, 0.65, 0.9);
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContentsWithName(
                        getFluidType(),
                        getFluidCapacity(),
                        getFluidAmount()
                ));
    }

    @Override @MultiHandler(priorities = { EventPriority.NORMAL, EventPriority.MONITOR })
    public void onInteractedWith(@NotNull PlayerInteractEvent event, @NotNull EventPriority priority) {
        if (event.getPlayer().isSneaking()
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == Event.Result.DENY
        ) {
            return;
        }

        if (event.getItem() != null && PylonUtils.handleFluidTankRightClick(this, event, priority)) {
            return;
        }

        if (priority == EventPriority.MONITOR) {
            tryDoRecipe();
        }
    }

    @Override
    public void tick() {
        for (LivingEntity entity : getBlock().getLocation().toCenterLocation().getNearbyLivingEntities(0.4)) {
            if (getFluidType().getTag(FluidTemperature.class) == FluidTemperature.HOT) {
                entity.setFireTicks(300);
                entity.damage(4.0F, DamageSource.builder(DamageType.LAVA).build());

                new RandomizedSound(List.of(SoundEventKeys.ENTITY_GENERIC_BURN), //play sound like vanilla cauldron
                        Sound.Source.NEUTRAL,
                        new Pair<>(0.4, 0.4),
                        new Pair<>(2.0, 2.4))
                    .playAt(entity);

            } else if (getFluidType().getTag(FluidTemperature.class) == FluidTemperature.COLD) {
                int freezeTick = Math.min(entity.getFreezeTicks() + getTickInterval() + 2, entity.getMaxFreezeTicks());
                entity.setFreezeTicks(freezeTick);

                if (freezeTick == entity.getMaxFreezeTicks() && entity.getTicksLived() % 40 == 0) {
                    entity.damage(1.0F, DamageSource.builder(DamageType.FREEZE).build());
                }
            }
        }
    }

    public boolean tryDoRecipe() {
        if (getFluidType() == null) return false;

        Material fireType = getFire().getType();
        boolean isFire = fireType == Material.FIRE || fireType == Material.SOUL_FIRE;
        if (!isFire) return false;

        RebarBlock ignitedBlock = BlockStorage.get(getIgnitedBlock());
        boolean isEnrichedFire = ignitedBlock != null
                && ignitedBlock.getSchema().getKey().equals(PylonKeys.ENRICHED_SOUL_SOIL);

        List<Item> items = getBlock()
                .getLocation()
                .toCenterLocation()
                .getNearbyEntitiesByType(Item.class, 0.5, 0.8, 0.5) // 0.8 to allow items on top to be used
                .stream()
                .toList();

        List<ItemStack> stacks = items.stream()
                .map(Item::getItemStack)
                .toList();

        for (MixingPotRecipe recipe : MixingPotRecipe.RECIPE_TYPE.getRecipes()) {
            if (!recipe.matches(stacks, isEnrichedFire, getFluidType(), getFluidAmount())) {
                continue;
            }

            doRecipe(recipe, items);
            return true;
        }

        new ParticleBuilder(Particle.SMOKE)
                .count(50)
                .extra(0)
                .offset(0.1, 0, 0.1)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.4, 0))
                .spawn();

        return false;
    }

    private void doRecipe(@NotNull MixingPotRecipe recipe, @NotNull List<Item> items) {
        for (ItemChoice choice : recipe.inputItems()) {
            for (Item item : items) {
                ItemStack stack = item.getItemStack();
                if (choice.matches(stack)) {
                    item.setItemStack(stack.subtract(choice.getAmount()));
                    break;
                }
            }
        }
        switch (recipe.output()) {
            case FluidOrItem.Item item -> {
                removeFluid(recipe.inputFluid().getAmount());
                getBlock().getWorld().dropItemNaturally(
                    getBlock().getLocation().toCenterLocation(),
                    item.item(),
                    (itemEdit) -> {
                        itemEdit.setVelocity(BlockFace.UP.getDirection().multiply(0.3));
                    }
                );
            }
            case FluidWithAmount fluid -> {
                setFluidType(fluid.fluid());
                setFluid(fluid.amountMillibuckets());
            }
            default -> {}
        }

        new ParticleBuilder(Particle.SPLASH)
                .count(20)
                .location(getBlock().getLocation().toCenterLocation().add(0, 0.5, 0))
                .offset(0.3, 0, 0.3)
                .spawn();

        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .count(30)
                .location(getBlock().getLocation().toCenterLocation())
                .extra(0.05)
                .spawn();
    }

    public @NotNull Block getFire() {
        return getBlock().getRelative(BlockFace.DOWN);
    }

    public @NotNull Block getIgnitedBlock() {
        return getFire().getRelative(BlockFace.DOWN);
    }
}
