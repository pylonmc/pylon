package io.github.pylonmc.pylon.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import io.github.pylonmc.pylon.content.science.Loupe;
import lombok.Getter;
import lombok.Setter;

/**
 * Called when a {@link Player} attempts to start scanning something with the {@link Loupe}
 * This event marks the start of a scan and {@link LoupeCompleteScanningEvent} marks the completion of a scan
 * <br>
 * If you'd like to add custom scanning functionality, use {@link LoupeStartScanningEvent#setCustomHandled(boolean)}
 * to handle the starting logic (e.g. filtering out anything that shouldn't be scanned, communicating why to players, etc)
 * And use {@link LoupeCompleteScanningEvent#setCustomHandled(boolean)} to handle the scan completed logic (e.g. giving
 * research points, etc)
 */
public class LoupeStartScanningEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The target of the {@link Loupe}'s scan, guaranteed to have either a {@link RayTraceResult#getHitBlock()}
     * or {@link RayTraceResult#getHitEntity()}
     */
    @Getter private final RayTraceResult scanTarget;
    /**
     * If the scan logic is being handled by an outside plugin, such as to introduce
     * scanning new objects or introducing special cases.
     * <br>
     * When implementing custom handling, the {@link Loupe} will only act as a tracker for beginning and completing
     * scans and assuring the target remains the same throughout. No other logic such as player feedback, research
     * point rewards, etc, is run, you must do that all yourself.
     * <br>
     * If one plugin is already custom handling, it is recommended not to do it as well.
     */
    @Getter @Setter private boolean customHandled = false;
    @Getter @Setter private boolean cancelled = false;

    public LoupeStartScanningEvent(@NotNull Player player, @NotNull RayTraceResult scanTarget) {
        super(player);
        this.scanTarget = scanTarget;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
