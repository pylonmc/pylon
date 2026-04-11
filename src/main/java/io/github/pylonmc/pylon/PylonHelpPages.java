package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.button.AddonPageButton;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.guide.pages.help.sub.ResearchingHelpPage;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.Item;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonHelpPages {
    public static final SimpleStaticGuidePage HELP = new SimpleStaticGuidePage(pylonKey("help"));

    public static final SimpleStaticGuidePage PROGRESSION = new SimpleStaticGuidePage(pylonKey("help_progression"));
    public static final SimpleStaticGuidePage PIT_KILN = new SimpleStaticGuidePage(pylonKey("help_pit_kiln"));
    public static final SimpleStaticGuidePage FLUID_HANDLING = new SimpleStaticGuidePage(pylonKey("help_fluid_handling"));
    public static final SimpleStaticGuidePage HYDRAULICS = new SimpleStaticGuidePage(pylonKey("help_hydraulics"));
    public static final SimpleStaticGuidePage BLOOMERY = new SimpleStaticGuidePage(pylonKey("help_bloomery"));
    public static final SimpleStaticGuidePage SMELTERY = new SimpleStaticGuidePage(pylonKey("help_smeltery"));

    public static void initialise() {
        ResearchingHelpPage.INSTANCE.addButton(Item.simple(ItemStackBuilder.guide(Material.GLASS_PANE, Pylon.getInstance(), "help.loupe")));
        ResearchingHelpPage.INSTANCE.addButton(Item.simple(ItemStackBuilder.guide(Material.RED_BANNER, Pylon.getInstance(), "help.research_packs")));

        RebarGuide.getHelpPage().addButton(new AddonPageButton(Pylon.getInstance(), HELP));

        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.IRON_PICKAXE, Pylon.getInstance(), "help.progression.manual_core")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.CLOCK, Pylon.getInstance(), "help.loupe"))); // recycle loupe desc
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BRICKS, Pylon.getInstance(), "help.progression.pit_kiln")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BRICK, Pylon.getInstance(), "help.progression.bronze_age")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.WATER_BUCKET, Pylon.getInstance(), "help.progression.hydraulic_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.MINECART, Pylon.getInstance(), "help.progression.cargo_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.QUARTZ, Pylon.getInstance(), "help.progression.gypsum")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.BLAST_FURNACE, Pylon.getInstance(), "help.progression.steel_age")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.PISTON, Pylon.getInstance(), "help.progression.diesel_machines")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.NETHERITE_INGOT, Pylon.getInstance(), "help.progression.palladium")));
        PROGRESSION.addButton(Item.simple(ItemStackBuilder.guide(Material.ELYTRA, Pylon.getInstance(), "help.progression.flight_ring")));
        HELP.addPage(Material.BOOKSHELF, PROGRESSION);

        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "help.pit_kiln.kiln")));
        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "help.pit_kiln.construction")));
        PIT_KILN.addButton(Item.simple(ItemStackBuilder.guide(Material.DECORATED_POT, Pylon.getInstance(), "help.pit_kiln.crafting")));
        HELP.addButton(new PageButton(ItemStackBuilder.of(Material.DECORATED_POT).addCustomModelDataString(PylonKeys.PIT_KILN.asString()), PIT_KILN));

        HELP.addButton(new PageButton(ItemStackBuilder.of(Material.BROWN_TERRACOTTA).addCustomModelDataString(PylonKeys.FLUID_PIPE_WOOD.asString()), FLUID_HANDLING));

        HELP.addButton(new PageButton(ItemStackBuilder.of(Material.WAXED_COPPER_BULB).addCustomModelDataString(PylonKeys.HYDRAULIC_CORE_DRILL.asString()), HYDRAULICS));

        BLOOMERY.addButton(Item.simple(ItemStackBuilder.guide(Material.MAGMA_BLOCK, Pylon.getInstance(), "help.bloomery.bloomery")));
        HELP.addButton(new PageButton(ItemStackBuilder.of(Material.MAGMA_BLOCK).addCustomModelDataString(PylonKeys.BLOOMERY.asString()), BLOOMERY));

        HELP.addButton(new PageButton(ItemStackBuilder.of(Material.BLAST_FURNACE).addCustomModelDataString(PylonKeys.SMELTERY_CONTROLLER.asString()), SMELTERY));

    }
}
