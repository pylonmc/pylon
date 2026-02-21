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

        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.IRON_PICKAXE, Pylon.getInstance(), "info.progression.manual_core")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.CLOCK, Pylon.getInstance(), "info.loupe"))); // recycle loupe desc
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BRICKS, Pylon.getInstance(), "info.progression.pit_kiln")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BRICK, Pylon.getInstance(), "info.progression.bronze_age")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.WATER_BUCKET, Pylon.getInstance(), "info.progression.hydraulic_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.MINECART, Pylon.getInstance(), "info.progression.cargo_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.QUARTZ, Pylon.getInstance(), "info.progression.gypsum")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BLAST_FURNACE, Pylon.getInstance(), "info.progression.steel_age")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.PISTON, Pylon.getInstance(), "info.progression.diesel_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.NETHERITE_INGOT, Pylon.getInstance(), "info.progression.palladium")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.ELYTRA, Pylon.getInstance(), "info.progression.palladium_flight_ring")));
        INFO.addPage(Material.BOOKSHELF, PROGRESSION);

        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "info.pit_kiln.kiln")));
        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "info.pit_kiln.construction")));
        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "info.pit_kiln.crafting")));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.DECORATED_POT).addCustomModelDataString(PylonKeys.PIT_KILN.asString()), PIT_KILN));

        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.BROWN_TERRACOTTA).addCustomModelDataString(PylonKeys.FLUID_PIPE_WOOD.asString()), FLUID_HANDLING));

        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.WAXED_COPPER_BULB).addCustomModelDataString(PylonKeys.HYDRAULIC_CORE_DRILL.asString()), HYDRAULICS));

        BLOOMERY.addButton(Item.simple(ItemStackBuilder.guide(Material.MAGMA_BLOCK, Pylon.getInstance(), "info.bloomery.bloomery")));
        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.MAGMA_BLOCK).addCustomModelDataString(PylonKeys.BLOOMERY.asString()), BLOOMERY));

        INFO.addButton(new PageButton(ItemStackBuilder.of(Material.BLAST_FURNACE).addCustomModelDataString(PylonKeys.SMELTERY_CONTROLLER.asString()), SMELTERY));

    }
}
