package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.content.machines.fluid.FluidTankWithDisplayEntity;
import io.github.pylonmc.pylon.recipes.CrucibleRecipe;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.BlockStorage;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarCauldron;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarInteractBlock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockBreakContext;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.Settings;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.fluid.RebarFluid;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.recipe.FluidOrItem;
import io.github.pylonmc.rebar.util.RebarUtils;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public final class Crucible extends RebarBlock implements
        RebarInteractBlock,
        FluidTankWithDisplayEntity,
        RebarDirectionalBlock,
        RebarCauldron,
        RebarTickingBlock {

    public final int capacity = getSettings().getOrThrow("capacity", ConfigAdapter.INTEGER);
    public final int smeltTime = getSettings().getOrThrow("smelt-time", ConfigAdapter.INTEGER);

    private ItemStack processingType = null;
    private int amount = 0;

    private static final NamespacedKey PROCESSING_KEY = pylonKey("processing");
    private static final NamespacedKey AMOUNT_KEY = pylonKey("amount");

    public static final Map<Material, Integer> VANILLA_BLOCK_HEAT_MAP = Settings.get(PylonKeys.CRUCIBLE).getOrThrow("vanilla-block-heat-map", ConfigAdapter.MAP.from(ConfigAdapter.MATERIAL, ConfigAdapter.INTEGER));

    @SuppressWarnings("unused")
    public Crucible(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block);
        createFluidDisplay();
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.NORTH, context, false);
        setCapacity(1000.0);
        setFacing(context.getFacing());
    }

    @SuppressWarnings("unused")
    public Crucible(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block);
        processingType = pdc.get(PROCESSING_KEY, RebarSerializers.ITEM_STACK);
        amount = pdc.get(AMOUNT_KEY, RebarSerializers.INTEGER);
    }

    //region Inventory handling
    public int spaceAvailable() {
        return capacity - amount;
    }

    @Override
    public void onBreak(@NotNull List<ItemStack> drops, @NotNull BlockBreakContext context) {
        FluidTankWithDisplayEntity.super.onBreak(drops, context);
        if (processingType == null || amount == 0) return;

        int maxStack = processingType.getMaxStackSize();
        int cycles = amount / maxStack;
        int spare = amount % maxStack;
        for (int i = 0; i < cycles; i++) {
            drops.add(processingType.asQuantity(maxStack));
        }

        drops.add(processingType.asQuantity(spare));
    }
    //endregion

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        RebarUtils.setNullable(pdc, PROCESSING_KEY, RebarSerializers.ITEM_STACK, processingType);
        pdc.set(AMOUNT_KEY, RebarSerializers.INTEGER, amount);
    }

    @Override
    public boolean isAllowedFluid(@NotNull RebarFluid fluid) {
        return true;
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        // Don't allow fluid to be manually inserted/removed
        if (PylonUtils.handleFluidTankRightClick(this, event)) {
            return;
        }

        if (event.getPlayer().isSneaking()
            || event.getHand() != EquipmentSlot.HAND
            || event.getAction() != Action.RIGHT_CLICK_BLOCK
        ) {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        if (!CrucibleRecipe.isValid(item)) {
            return;
        }

        int amount = Math.min(item.getAmount(), spaceAvailable());
        if (amount == 0) {
            return;
        }

        if (processingType == null) {
            this.processingType = item.asOne();
            this.amount = amount;
            item.subtract(amount);
        } else if (processingType.isSimilar(item)) {
            this.amount += amount;
            item.subtract(amount);
        }
    }

    public void clearInventory() {
        this.processingType = null;
        this.amount = 0;
    }

    public boolean tryDoRecipe() {
        if (processingType == null || getHeatFactor() == null) {
            return false;
        }

        for (CrucibleRecipe recipe : CrucibleRecipe.RECIPE_TYPE.getRecipes()) {
            if (recipe.matches(processingType)) {
                doRecipe(recipe);
                return true;
            }
        }

        return false;
    }

    private void doRecipe(@NotNull CrucibleRecipe recipe) {
        if (getFluidType() != null && !recipe.output().fluid().equals(getFluidType())) {
            return;
        }

        if (getFluidSpaceRemaining() < recipe.output().amountMillibuckets()) {
            return;
        }

        FluidOrItem.Fluid fluid = recipe.output();

        setFluidType(fluid.fluid());
        addFluid(fluid.amountMillibuckets());

        new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                .count(20)
                .extra(0.05)
                .location(getBlock().getLocation().toCenterLocation())
                .spawn();

        new ParticleBuilder(Particle.LAVA)
                .count(10)
                .location(getBlock().getLocation().toCenterLocation())
                .spawn();

        this.amount--;
        if (amount == 0) {
            clearInventory();
        }
    }

    @Override
    public void onLevelChange(@NotNull CauldronLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @Override
    public Vector3d fluidDisplayTranslation() {
        return new Vector3d(0, -0.2, 0);
    }

    @Override
    public Vector3d fluidDisplayScale() {
        return new Vector3d(0.9, 0.65, 0.9);
    }

    @Override
    public @NotNull WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
            RebarArgument.of("item_info", processingType == null ?
                Component.translatable("pylon.waila.crucible.item.empty") :
                Component.translatable("pylon.waila.crucible.item.stored",
                    RebarArgument.of("type", processingType.getData(DataComponentTypes.ITEM_NAME)),
                    RebarArgument.of("amount", amount)
                )),

            RebarArgument.of("liquid_info", getFluidType() == null ?
                Component.translatable("pylon.waila.crucible.liquid.empty") :
                Component.translatable("pylon.waila.crucible.liquid.filled",
                    RebarArgument.of("fluid", getFluidType().getName()),
                    RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        getFluidAmount(),
                        getFluidCapacity(),
                        20,
                        TextColor.color(200, 255, 255)
                    ))
                ))
        ));
    }

    //region Tick handling
    @Override
    public void tick() {
        tryDoRecipe();
    }

    @Override
    public int getTickInterval() {
        if (processingType == null) {
            return smeltTime; // minimize ticking when empty
        }

        Integer heatFactor = getHeatFactor();
        if (heatFactor == null) {
            return smeltTime; // probably won't even work be operating in such scenario
        }

        // stronger heat factor -> faster smelting
        return smeltTime / heatFactor;
    }

    //endregion

    //region Heat handling
    public interface HeatedBlock extends Keyed {
        int heatGenerated();
    }

    public @Nullable Integer getHeatFactor() {
        Block below = getBlock().getRelative(BlockFace.DOWN);
        var pylonBlock = BlockStorage.get(below);
        if (pylonBlock != null) {
            if (pylonBlock instanceof HeatedBlock heatedBlock) {
                return heatedBlock.heatGenerated();
            }

            return null;
        }

        return VANILLA_BLOCK_HEAT_MAP.get(below.getType());
    }
    //endregion
}
