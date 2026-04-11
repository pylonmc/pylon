package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonPages {

    public static final SimpleStaticGuidePage SCIENCE = new SimpleStaticGuidePage(pylonKey("science"));

    public static final SimpleStaticGuidePage METALS = new SimpleStaticGuidePage(pylonKey("resources_metals"));
    public static final SimpleStaticGuidePage CORE_CHUNKS = new SimpleStaticGuidePage(pylonKey("resources_core_chunks"));
    public static final SimpleStaticGuidePage MAGIC = new SimpleStaticGuidePage(pylonKey("resources_magic"));
    public static final SimpleStaticGuidePage MISCELLANEOUS = new SimpleStaticGuidePage(pylonKey("resources_miscellaneous"));
    public static final SimpleStaticGuidePage RESOURCES = new SimpleStaticGuidePage(pylonKey("resources"));

    public static final SimpleStaticGuidePage COMPONENTS = new SimpleStaticGuidePage(pylonKey("components"));
    public static final SimpleStaticGuidePage TOOLS = new SimpleStaticGuidePage(pylonKey("tools"));
    public static final SimpleStaticGuidePage COMBAT = new SimpleStaticGuidePage(pylonKey("combat"));
    public static final SimpleStaticGuidePage TALISMANS = new SimpleStaticGuidePage(pylonKey("talismans"));
    public static final SimpleStaticGuidePage ARMOUR = new SimpleStaticGuidePage(pylonKey("armour"));
    public static final SimpleStaticGuidePage FOOD = new SimpleStaticGuidePage(pylonKey("food"));

    public static final SimpleStaticGuidePage SIMPLE_MACHINES = new SimpleStaticGuidePage(pylonKey("machines_simple_machines"));
    public static final SimpleStaticGuidePage SMELTING = new SimpleStaticGuidePage(pylonKey("machines_smelting"));
    public static final SimpleStaticGuidePage FLUID_PIPES_AND_TANKS = new SimpleStaticGuidePage(pylonKey("machines_fluid_pipes_and_tanks"));
    public static final SimpleStaticGuidePage FLUID_MACHINES = new SimpleStaticGuidePage(pylonKey("machines_fluid_machines"));
    public static final SimpleStaticGuidePage HYDRAULIC_MACHINES = new SimpleStaticGuidePage(pylonKey("machines_hydraulic_machines"));
    public static final SimpleStaticGuidePage HYDRAULIC_PURIFICATION = new SimpleStaticGuidePage(pylonKey("machines_hydraulic_purification"));
    public static final SimpleStaticGuidePage CARGO = new SimpleStaticGuidePage(pylonKey("machines_cargo"));
    public static final SimpleStaticGuidePage DIESEL_MACHINES = new SimpleStaticGuidePage(pylonKey("machines_diesel_machines"));
    public static final SimpleStaticGuidePage DIESEL_PRODUCTION = new SimpleStaticGuidePage(pylonKey("machines_diesel_production"));
    public static final SimpleStaticGuidePage MACHINES = new SimpleStaticGuidePage(pylonKey("machines"));

    public static final SimpleStaticGuidePage ASSEMBLING = new SimpleStaticGuidePage(pylonKey("assembling"));
    public static final SimpleStaticGuidePage BUILDING = new SimpleStaticGuidePage(pylonKey("building"));
    public static final SimpleStaticGuidePage CREATIVE_ITEMS = new SimpleStaticGuidePage(pylonKey("creative_items")) {
        @Override
        public boolean shouldDisplay(@NotNull Player player) {
            return player.hasPermission("rebar.guide.view_admin_pages");
        }
    };

    public static void initialise() {
        RebarGuide.getRootPage().addPage(PylonItems.LOUPE, SCIENCE);

        RESOURCES.addPage(PylonItems.BRONZE_INGOT, METALS);
        RESOURCES.addPage(PylonItems.SHALLOW_CORE_CHUNK, CORE_CHUNKS);
        RESOURCES.addPage(PylonItems.COVALENT_BINDER, MAGIC);
        RESOURCES.addPage(PylonItems.UNFIRED_REFRACTORY_BRICK, MISCELLANEOUS);
        RebarGuide.getRootPage().addPage(PylonItems.COPPER_DUST, RESOURCES);

        RebarGuide.getRootPage().addPage(PylonItems.BRONZE_DRILL_BIT, COMPONENTS);
        RebarGuide.getRootPage().addPage(PylonItems.BRONZE_PICKAXE, TOOLS);
        RebarGuide.getRootPage().addPage(PylonItems.BEHEADING_SWORD, COMBAT);
        RebarGuide.getRootPage().addPage(PylonItems.HEALTH_TALISMAN_SIMPLE, TALISMANS);
        RebarGuide.getRootPage().addPage(PylonItems.BRONZE_CHESTPLATE, ARMOUR);
        RebarGuide.getRootPage().addPage(Material.APPLE, FOOD);

        MACHINES.addPage(PylonItems.PRESS, SIMPLE_MACHINES);
        MACHINES.addPage(PylonItems.SMELTERY_CONTROLLER, SMELTING);
        MACHINES.addPage(PylonItems.FLUID_PIPE_COPPER, FLUID_PIPES_AND_TANKS);
        MACHINES.addPage(PylonItems.FLUID_METER, FLUID_MACHINES);
        MACHINES.addPage(PylonItems.HYDRAULIC_PIPE_BENDER, HYDRAULIC_MACHINES);
        MACHINES.addPage(PylonItems.SOLAR_PURIFICATION_TOWER_3, HYDRAULIC_PURIFICATION);
        MACHINES.addPage(PylonItems.CARGO_BUFFER, CARGO);
        MACHINES.addPage(PylonItems.DIESEL_PIPE_BENDER, DIESEL_MACHINES);
        MACHINES.addPage(PylonItems.BIOREFINERY, DIESEL_PRODUCTION);
        RebarGuide.getRootPage().addPage(PylonItems.MIXING_POT, MACHINES);
        RebarGuide.getRootPage().addPage(PylonItems.ASSEMBLY_TABLE, ASSEMBLING);

        RebarGuide.getRootPage().addPage(PylonItems.ELEVATOR_1, BUILDING);
        RebarGuide.getRootPage().addPage(PylonItems.FLUID_PIPE_CREATIVE, CREATIVE_ITEMS);
    }
}
