package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.button.AddonPageButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.guide.pages.info.sub.ResearchingInfoPage;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.Item;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonGuides {
    public static final SimpleStaticGuidePage INFO = new SimpleStaticGuidePage(pylonKey("info"));

    public static final SimpleStaticGuidePage PROGRESSION = new SimpleStaticGuidePage(pylonKey("info_progression"));
    public static final SimpleStaticGuidePage PIT_KILN = new SimpleStaticGuidePage(pylonKey("info_pit_kiln"));
    public static final SimpleStaticGuidePage FLUID_HANDLING = new SimpleStaticGuidePage(pylonKey("info_fluid_handling"));
    public static final SimpleStaticGuidePage HYDRAULICS = new SimpleStaticGuidePage(pylonKey("info_hydraulics"));
    public static final SimpleStaticGuidePage BLOOMERY = new SimpleStaticGuidePage(pylonKey("info_bloomery"));
    public static final SimpleStaticGuidePage SMELTERY = new SimpleStaticGuidePage(pylonKey("info_smeltery"));

    public static void initialise() {
        ResearchingInfoPage.INSTANCE.addButton(Item.simple(ItemStackBuilder.guide(Material.GLASS_PANE, Pylon.getInstance(), "info.loupe")));
        ResearchingInfoPage.INSTANCE.addButton(Item.simple(ItemStackBuilder.guide(Material.RED_BANNER, Pylon.getInstance(), "info.research_packs")));

        RebarGuide.getInfoPage().addButton(new AddonPageButton(Pylon.getInstance(), INFO));

        INFO.addPage(Material.BOOKSHELF, PROGRESSION);
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.DECORATED_POT).addCustomModelDataString(PylonKeys.PIT_KILN.asString()), PIT_KILN));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.BROWN_TERRACOTTA).addCustomModelDataString(PylonKeys.FLUID_PIPE_WOOD.asString()), FLUID_HANDLING));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.WAXED_COPPER_BULB).addCustomModelDataString(PylonKeys.HYDRAULIC_CORE_DRILL.asString()), HYDRAULICS));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.MAGMA_BLOCK).addCustomModelDataString(PylonKeys.BLOOMERY.asString()), BLOOMERY));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.BLAST_FURNACE).addCustomModelDataString(PylonKeys.SMELTERY_CONTROLLER.asString()), SMELTERY));

    }
}
