package io.github.pylonmc.pylon.content.machines.experience;

import io.github.pylonmc.pylon.PylonFluids;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.interfaces.FluidBufferRebarBlock;
import io.github.pylonmc.rebar.block.interfaces.TickingRebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.fluid.FluidPointType;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ExperienceDrain extends RebarBlock implements TickingRebarBlock, FluidBufferRebarBlock {
    public final int xpDrainPeriodTicks = getSettingOrThrow("xp-drain-period-ticks", ConfigAdapter.INTEGER);
    public final int xpDrainAmount = getSettingOrThrow("xp-drain-amount", ConfigAdapter.INTEGER);
    public final int xpBufferAmount = getSettingOrThrow("xp-buffer-amount", ConfigAdapter.INTEGER);

    public static class Item extends RebarItem {
        public final int xpDrainPeriodTicks = getSettingOrThrow("xp-drain-period-ticks", ConfigAdapter.INTEGER);
        public final int xpDrainAmount = getSettingOrThrow("xp-drain-amount", ConfigAdapter.INTEGER);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("xp-drain-rate", UnitFormat.EXPERIENCE_PER_SECOND.format((double) xpDrainAmount / ((double) xpDrainPeriodTicks / 20))));
        }
    }

    public ExperienceDrain(@NotNull Block block, BlockCreateContext ctx) {
        super(block, ctx);
        createFluidBuffer(PylonFluids.LIQUID_XP, xpBufferAmount, false, true);
        createFluidPoint(FluidPointType.OUTPUT, BlockFace.NORTH, ctx, false, 0.5f);
        setTickInterval(xpDrainPeriodTicks);
    }

    public ExperienceDrain(@NotNull Block block, PersistentDataContainer pdc) {
        super(block, pdc);
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        return WailaDisplay.of(this, player)
                .add(ProgressBar.fluidContents(
                        PylonFluids.LIQUID_XP,
                        fluidCapacity(PylonFluids.LIQUID_XP),
                        fluidAmount(PylonFluids.LIQUID_XP)
                ));
    }

    // For some unknown reason, if you use /xp to give someone xp, it doesn't update player.getTotalExperience
    public int getRealTotalExperience(@NonNull Player player) {
        int level = player.getLevel();
        int totalExp = 0;

        if (level <= 16) {
            totalExp = (int) (Math.pow(level, 2) + (6 * level));
        } else if (level <= 31) {
            totalExp = (int) (2.5 * Math.pow(level, 2) - (40.5 * level) + 360);
        } else {
            totalExp = (int) (4.5 * Math.pow(level, 2) - (162.5 * level) + 2220);
        }

        float progress = player.getExp();
        int expForNextLevel = Math.round(getExpToNextLevel(level) * progress);

        return totalExp + expForNextLevel;
    }

    private static int getExpToNextLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }

    public int subtractExperience(Player player, int amountToSubtract) {
        int currentTotalExp = getRealTotalExperience(player);

        int actualSubtracted = Math.min(currentTotalExp, amountToSubtract);
        int newTotalExp = currentTotalExp - actualSubtracted;

        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        player.giveExp(newTotalExp);

        return actualSubtracted;
    }

    @Override
    public void tick() {
        for (Player player : getBlock().getWorld().getNearbyPlayers(getBlock().getLocation(), 1d, 5d, Player::isSneaking)) {
            int actualSubtracted = subtractExperience(player, Math.min(xpDrainAmount, (int) Math.floor(fluidSpaceRemaining(PylonFluids.LIQUID_XP))));
            addFluid(PylonFluids.LIQUID_XP, actualSubtracted);
            if (actualSubtracted != 0) {
                getBlock().getWorld().spawnParticle(Particle.BUBBLE, getBlock().getLocation().toCenterLocation().clone().add(0, 0.25, 0), 1);
            }
        }
    }
}
