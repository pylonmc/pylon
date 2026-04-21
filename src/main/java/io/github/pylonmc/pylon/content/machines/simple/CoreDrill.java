package io.github.pylonmc.pylon.content.machines.simple;

import com.destroystokyo.paper.ParticleBuilder;
import io.github.pylonmc.pylon.Pylon;
import io.github.pylonmc.pylon.util.PylonUtils;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.base.RebarDirectionalBlock;
import io.github.pylonmc.rebar.block.base.RebarProcessor;
import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock;
import io.github.pylonmc.rebar.block.base.RebarTickingBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.entity.display.ItemDisplayBuilder;
import io.github.pylonmc.rebar.entity.display.transform.TransformBuilder;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.util.position.BlockPosition;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public abstract class CoreDrill extends RebarBlock implements
        RebarSimpleMultiblock,
        RebarDirectionalBlock,
        RebarProcessor,
        RebarTickingBlock {

    public static class Item extends RebarItem {

        private final int rotationDuration = getSettings().getOrThrow("rotation-duration-ticks", ConfigAdapter.INTEGER);
        private final int rotationsPerCycle = getSettings().getOrThrow("rotations-per-cycle", ConfigAdapter.INTEGER);
        private final ItemStack output = getSettings().getOrThrow("output", ConfigAdapter.ITEM_STACK);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("cycle-time", UnitFormat.SECONDS.format(rotationsPerCycle * rotationDuration / 20)),
                    RebarArgument.of("cycle-output", output.effectiveName())
            );
        }
    }

    @Getter protected final int rotationDuration = getSettings().getOrThrow("rotation-duration-ticks", ConfigAdapter.INTEGER);
    @Getter protected final int rotationsPerCycle = getSettings().getOrThrow("rotations-per-cycle", ConfigAdapter.INTEGER);
    protected final boolean spawnBlockParticles = getSettings().getOrThrow("spawn-block-particles", ConfigAdapter.BOOLEAN);
    protected final ItemStack output = getSettings().getOrThrow("output", ConfigAdapter.ITEM_STACK);
    protected final Material drillMaterial = getSettings().getOrThrow("drill-material", ConfigAdapter.MATERIAL);
    protected final ItemStackBuilder drillStack = ItemStackBuilder.of(drillMaterial)
            .addCustomModelDataString(getKey() + ":drill");

    @SuppressWarnings("unused")
    protected CoreDrill(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        setFacing(context.getFacing());
        setMultiblockDirection(getFacing());
        addEntity("drill", new ItemDisplayBuilder()
                .itemStack(drillStack)
                .transformation(new TransformBuilder()
                        .scale(0.3, 2.1, 0.3)
                )
                .build(getBlock().getLocation().toCenterLocation().subtract(0, 1.5, 0))
        );
        setTickInterval(rotationDuration);
    }

    @SuppressWarnings("unused")
    protected CoreDrill(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public @Nullable ItemDisplay getDrillDisplay() {
        return getHeldEntity(ItemDisplay.class, "drill");
    }

    public static @NotNull Matrix4f getDrillDisplayMatrix(double rotation) {
        return new TransformBuilder()
                .scale(0.3, 2.1, 0.3)
                .rotate(0, rotation, 0)
                .buildForItemDisplay();
    }

    public void startCycle() {
        if (isProcessing() || !isFormedAndFullyLoaded()) {
            return;
        }

        startProcess(getCycleDuration());
        doRotationAnimation();
    }

    @Override
    public void tick() {
        if (!isFormedAndFullyLoaded()) {
            return;
        }

        if (!isProcessing()) {
            return;
        }

        progressProcess(getTickInterval());
        if (isProcessing()) { // don't want to start animation if we've just finished drilling
            doRotationAnimation();
        }
    }

    @Override
    public void onProcessFinished() {
        getBlock().getWorld().dropItemNaturally(
                getBlock().getRelative(BlockFace.DOWN, 2).getLocation().toCenterLocation(),
                output,
                (item) -> item.setVelocity(getFacing().getDirection().multiply(0.3))
        );
    }

    private void doRotationAnimation() {
        for (int j = 0; j < 4; j++) {
            double rotation = (j / 4.0) * 2.0 * Math.PI;
            Bukkit.getScheduler().runTaskLater(Pylon.getInstance(), () -> {
                if (!new BlockPosition(getBlock()).getChunk().isLoaded()) {
                    return;
                }

                PylonUtils.animate(getDrillDisplay(), rotationDuration / 4, getDrillDisplayMatrix(rotation));
                if (spawnBlockParticles) {
                    new ParticleBuilder(Particle.ITEM)
                            .count(5)
                            .data(new ItemStack(getBlock().getRelative(BlockFace.DOWN, 3).getType()))
                            .location(getBlock()
                                    .getRelative(BlockFace.DOWN, 2)
                                    .getLocation()
                                    .toCenterLocation()
                                    .subtract(0, 0.3, 0)
                            )
                            .spawn();
                }
            }, (long) ((j/4.0) * rotationDuration));
        }
    }

    public int getCycleDuration() {
        return rotationsPerCycle * rotationDuration;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        String wailaFormat = "pylon.item." + getKey().getKey() + ".waila_format";
        Integer timeLeft = getProcessTicksRemaining();
        return new WailaDisplay(getDefaultWailaTranslationKey().arguments(
            RebarArgument.of("duration_if_any",
                timeLeft == null
                    ? Component.empty()
                    : Component.translatable(wailaFormat).arguments(
                        RebarArgument.of("duration", PylonUtils.createProgressBar(
                                ((double) getProcessTimeTicks() - (double) getProcessTicksRemaining()) / (double) getProcessTimeTicks(),
                                20,
                                NamedTextColor.WHITE
                        ))
                    )
            )
        ));
    }
}
