package io.github.pylonmc.pylon.content.machines.experience;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.PylonKeys;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarFluidBufferBlock;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;

public class ExperienceFountain extends RebarBlock implements RebarTickingBlock, RebarFluidBufferBlock, RebarSimpleMultiblock {
    public final int amountToConvert = getSettings().getOrThrow("amount-to-convert", ConfigAdapter.INTEGER);
    public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);
    public final int liquidXpCapacity = getSettings().getOrThrow("liquid-xp-capacity", ConfigAdapter.INTEGER);

    public ExperienceFountain(@NotNull Block block, BlockCreateContext ctx) {
        super(block, ctx);
        createFluidBuffer(PylonFluids.LIQUID_XP, liquidXpCapacity, true, false);
        createFluidPoint(FluidPointType.INPUT, BlockFace.WEST, ctx, false, 0.5f);
        setTickInterval(tickInterval);
    }

    public ExperienceFountain(@NotNull Block block, PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public void tick() {
        if(!isFormedAndFullyLoaded()){
            return;
        }
        int amountConverted = (int)Math.min(amountToConvert, fluidAmount(PylonFluids.LIQUID_XP));
        if(amountConverted <= 0){
            return;
        }
        removeFluid(PylonFluids.LIQUID_XP, amountConverted);
        ExperienceOrb orb = (ExperienceOrb) getBlock().getWorld().spawnEntity(getBlock().getLocation(), EntityType.EXPERIENCE_ORB);
        orb.setExperience(amountConverted);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
                RebarArgument.of("bar", PylonUtils.createFluidAmountBar(
                        fluidAmount(PylonFluids.LIQUID_XP),
                        fluidCapacity(PylonFluids.LIQUID_XP),
                        20,
                        TextColor.fromHexString("#5024d1")
                ))
        ));
    }

    @Override
    public @NotNull Map<@NotNull Vector3i, @NotNull MultiblockComponent> getComponents() {
        return Map.of(new Vector3i(0, 1, 0), new RebarSimpleMultiblock.RebarMultiblockComponent(PylonKeys.EXPERIENCE_FOUNTAIN_SPOUT));
    }

    public static class Item extends RebarItem {
        public final int amountToConvert = getSettings().getOrThrow("amount-to-convert", ConfigAdapter.INTEGER);
        public final int tickInterval = getSettings().getOrThrow("tick-interval", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(RebarArgument.of("production-rate", UnitFormat.EXPERIENCE_PER_SECOND.format((double) amountToConvert / ((double) tickInterval / 20)).decimalPlaces(0)));
        }
    }
}
