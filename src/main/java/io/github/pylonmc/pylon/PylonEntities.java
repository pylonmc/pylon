package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.content.machines.diesel.machines.Lathe;
import io.github.pylonmc.pylon.util.DisplayProjectile;
import io.github.pylonmc.rebar.entity.RebarEntity;
import org.bukkit.entity.ItemDisplay;


public class PylonEntities {

    private PylonEntities() {
        throw new AssertionError("Utility class");
    }

    static {
        RebarEntity.register(PylonKeys.DISPLAY_PROJECTILE, ItemDisplay.class, DisplayProjectile.class, false);
        RebarEntity.register(PylonKeys.LATHE_BUTTON, ItemDisplay.class, Lathe.ButtonEntity.class, true);
    }

    // Calling this method forces all the static blocks to run, which initializes our items
    public static void initialize() {}
}
