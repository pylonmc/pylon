package io.github.pylonmc.pylon.content.resources;

import io.github.pylonmc.pylon.content.tools.Moldable;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.datatypes.RebarSerializers;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.util.ProgressBar;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import io.github.pylonmc.rebar.waila.WailaDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;


public class RefractoryMix extends RebarBlock implements Moldable {

    private static final NamespacedKey MOLDING_CLICKS_KEY = pylonKey("molding-clicks");

    private int moldingClicksRemaining;

    @SuppressWarnings("unused")
    public RefractoryMix(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
        moldingClicksRemaining = totalMoldingClicks();
    }

    @SuppressWarnings("unused")
    public RefractoryMix(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
        moldingClicksRemaining = pdc.get(MOLDING_CLICKS_KEY, RebarSerializers.INTEGER);
    }

    @Override
    public void write(@NotNull PersistentDataContainer pdc) {
        pdc.set(MOLDING_CLICKS_KEY, RebarSerializers.INTEGER, moldingClicksRemaining);
    }

    @Override
    public void doMoldingClick() {
        moldingClicksRemaining--;
    }

    @Override
    public boolean isMoldingFinished() {
        return moldingClicksRemaining == 0;
    }

    @Override
    public @Nullable WailaDisplay getWaila(@NotNull Player player) {
        WailaDisplay display = WailaDisplay.of(this, player);
        if (moldingClicksRemaining != totalMoldingClicks()) {
            display.add(new ProgressBar()
                    .bars(totalMoldingClicks())
                    .proportion((double) (totalMoldingClicks() - moldingClicksRemaining) / totalMoldingClicks())
                    .barColor(TextColor.color(150, 100, 100))
                    .suffix(Component.text(" ")
                            .append(Component.text(totalMoldingClicks() - moldingClicksRemaining))
                            .append(Component.text("/"))
                            .append(Component.text(totalMoldingClicks()))
                    )
            );
        }
        return display;
    }
}
