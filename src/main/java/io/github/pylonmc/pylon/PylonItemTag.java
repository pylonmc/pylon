package io.github.pylonmc.pylon;

import com.destroystokyo.paper.MaterialSetTag;
import io.github.pylonmc.rebar.item.RebarItemTag;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import org.bukkit.Material;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonItemTag {
    public static final MaterialSetTag GLASSLIKE_BUKKIT = new MaterialSetTag(pylonKey("all_glass"))
            .endsWith("_GLASS")
            .endsWith("GLASS_PANE")
            .add(Material.GLASS)
            .add(Material.SEA_LANTERN)
            .add(Material.BEACON)
            .add(Material.GLOWSTONE)
            .add(Material.REDSTONE_LAMP);

    public static final RebarItemTag GLASSLIKE = new RebarItemTag(
        GLASSLIKE_BUKKIT.getKey(),
        GLASSLIKE_BUKKIT.getValues().toArray(new Material[0])
    );

    static {
        RebarRegistry.ITEM_TAGS.register(GLASSLIKE);
    }
}
