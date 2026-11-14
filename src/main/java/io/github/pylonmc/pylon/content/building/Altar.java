package io.github.pylonmc.pylon.content.building;

import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An altar that uses pedestals as ingredients, and can have its running recipe cancelled.
 */
public interface Altar {

    Block getBlock();

    /**
     * All pedestal blocks of this altar; entries may be null if the pedestal is unloaded or was replaced.
     */
    List<@Nullable Pedestal> getAllPedestals();

    /**
     * Existing pedestals that are not locked by another altar.
     */
    List<Pedestal> getValidPedestals();

    /**
     * Cancels the running recipe (if any), unlocking its pedestals.
     * Must be a no-op when no recipe is running.
     */
    void cancelRecipe();

    /**
     * @return true if every pedestal exists and none is locked by another altar
     */
    default boolean allPedestalsValid() {
        return getValidPedestals().size() == getAllPedestals().size();
    }

    default void lockPedestals() {
        for (Pedestal pedestal : getValidPedestals()) {
            pedestal.lock(getBlock());
        }
    }

    default void unlockPedestals() {
        for (Pedestal pedestal : getValidPedestals()) {
            pedestal.unlock();
        }
    }
}
