package io.github.pylonmc.pylon.content.building;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.github.pylonmc.rebar.block.RebarBlock;
import io.github.pylonmc.rebar.block.context.BlockCreateContext;
import io.github.pylonmc.rebar.block.interfaces.JumpRebarBlockHandler;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.RebarItem;
import io.github.pylonmc.rebar.util.gui.unit.UnitFormat;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3d;

import java.util.List;

public class Launchpad extends RebarBlock implements JumpRebarBlockHandler {

    public final double launchForce = getSettingOrThrow("launch-force", ConfigAdapter.DOUBLE);

    public Launchpad(@NotNull Block block, @NotNull BlockCreateContext context) {
        super(block, context);
    }

    public Launchpad(@NotNull Block block, @NotNull PersistentDataContainer pdc) {
        super(block, pdc);
    }

    public static class Item extends RebarItem {
        public final double launchForce = getSettingOrThrow("launch-force", ConfigAdapter.DOUBLE);

        public Item(@NotNull ItemStack stack) {
            super(stack);
        }

        @Override
        public @NotNull List<@NotNull RebarArgument> getPlaceholders() {
            return List.of(
                    RebarArgument.of("launch-force", UnitFormat.BLOCKS_PER_SECOND.format(launchForce))
            );
        }
    }

    @Override @MultiHandler(priorities = EventPriority.MONITOR)
    public void onJumpedOn(@NotNull PlayerJumpEvent event, @NotNull EventPriority priority) {
        Vector playerVelocity = event.getTo().toVector().subtract(event.getFrom().toVector()); // Player.getVelocity() seems to lag a bit behind, just going to use data form the event.
        // Use current player velocity as direction, multiply by scalar to get force and add that to original velocity.
        Vector3d forceVec = getUnitVec(playerVelocity.toVector3d())
                .mul(launchForce); // TODO change from applying this force all at once to adding over time.
        event.getPlayer().setVelocity(playerVelocity.add(Vector.fromJOML(forceVec)));
    }

    private static Vector3d getUnitVec(Vector3d vector){
        double magnitude = vector.length();
        return new Vector3d(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude);
    }
}
