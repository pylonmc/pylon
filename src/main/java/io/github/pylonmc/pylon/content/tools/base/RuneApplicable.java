package io.github.pylonmc.pylon.content.tools.base;

import io.github.pylonmc.rebar.item.interfaces.ArrowRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BlockBreakRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BowRebarItemHandler;
import io.github.pylonmc.rebar.item.interfaces.BucketRebarItemHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Defines items that can be applied with runes.
 *
 * <p>Default rune-applicable items are defined in {@link Rune#DEFAULT_APPLICABLES},
 * including but not limited to:
 * <ul>
 *   <li>{@link ArrowRebarItemHandler}</li>
 *   <li>{@link BowRebarItemHandler}</li>
 *   <li>{@link BucketRebarItemHandler}</li>
 *   <li>{@link BlockBreakRebarItemHandler}</li>
 *   <li>{@link EntityAttackRebarItemHandler}</li>
 * </ul>
 *
 * <p><b>Implementation Example:</b></p>
 * <pre>{@code
 * public class MyCustomWeapon extends RebarItem implements RuneApplicable {
 *     @Override
 *     public boolean isApplicableToTarget(
 *         @NotNull PlayerDropItemEvent event,
 *         @NotNull ItemStack rune
 *     ) {
 *         // Only allow diamond swords to be applied with runes
 *         return target.getType() == Material.DIAMOND_SWORD;
 *     }
 * }
 * }</pre>
 *
 * @author balugaq
 */
public interface RuneApplicable {
    /**
     * Called when a rune is dropped on a target item.
     *
     * @param event The event
     * @param rune  The rune
     * @return true if the rune is applicable to the target item, false otherwise
     */
    boolean applicableToTarget(@NotNull PlayerDropItemEvent event, @NotNull ItemStack rune);
}
