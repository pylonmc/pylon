package io.github.pylonmc.pylon;

import io.github.pylonmc.rebar.content.guide.RebarGuide;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class PylonPages {

    public static final SimpleStaticGuidePage SCIENCE = new SimpleStaticGuidePage(pylonKey("science"));

    public static final SimpleStaticGuidePage METALS = new SimpleStaticGuidePage(pylonKey("metals"));
    public static final SimpleStaticGuidePage CORE_CHUNKS = new SimpleStaticGuidePage(pylonKey("core_chunks"));
    public static final SimpleStaticGuidePage MAGIC = new SimpleStaticGuidePage(pylonKey("magic"));
    public static final SimpleStaticGuidePage MISCELLANEOUS = new SimpleStaticGuidePage(pylonKey("miscellaneous"));
    public static final SimpleStaticGuidePage RESOURCES = new SimpleStaticGuidePage(pylonKey("resources"));
    public static final SimpleStaticGuidePage COMPONENTS = new SimpleStaticGuidePage(pylonKey("components"));

    public static final SimpleStaticGuidePage TOOLS = new SimpleStaticGuidePage(pylonKey("tools"));
    public static final SimpleStaticGuidePage COMBAT = new SimpleStaticGuidePage(pylonKey("combat"));
    public static final SimpleStaticGuidePage TALISMANS = new SimpleStaticGuidePage(pylonKey("talismans"));
    public static final SimpleStaticGuidePage ARMOUR = new SimpleStaticGuidePage(pylonKey("armour"));
    public static final SimpleStaticGuidePage FOOD = new SimpleStaticGuidePage(pylonKey("food"));

    public static final SimpleStaticGuidePage SIMPLE_MACHINES = new SimpleStaticGuidePage(pylonKey("simple_machines"));
    public static final SimpleStaticGuidePage SMELTING = new SimpleStaticGuidePage(pylonKey("smelting"));
    public static final SimpleStaticGuidePage LOGISTICS = new SimpleStaticGuidePage(pylonKey("logistics"));
    public static final SimpleStaticGuidePage SILOS = new SimpleStaticGuidePage(pylonKey("silos"));
    public static final SimpleStaticGuidePage CARGO = new SimpleStaticGuidePage(pylonKey("cargo"));
    public static final SimpleStaticGuidePage FLUID_HANDLING = new SimpleStaticGuidePage(pylonKey("fluid_handling"));
    public static final SimpleStaticGuidePage FLUID_PIPES_AND_TANKS = new SimpleStaticGuidePage(pylonKey("fluid_pipes_and_tanks"));
    public static final SimpleStaticGuidePage FLUID_MACHINES = new SimpleStaticGuidePage(pylonKey("fluid_machines"));
    public static final SimpleStaticGuidePage BOILERS = new SimpleStaticGuidePage(pylonKey("boilers"));
    public static final SimpleStaticGuidePage HYDRAULICS = new SimpleStaticGuidePage(pylonKey("hydraulics"));
    public static final SimpleStaticGuidePage HYDRAULIC_MACHINES = new SimpleStaticGuidePage(pylonKey("hydraulic_machines"));
    public static final SimpleStaticGuidePage HYDRAULIC_PURIFICATION = new SimpleStaticGuidePage(pylonKey("hydraulic_purification"));
    public static final SimpleStaticGuidePage PETROCHEMICALS = new SimpleStaticGuidePage(pylonKey("petrochemicals"));
    public static final SimpleStaticGuidePage OIL_EXTRACTION = new SimpleStaticGuidePage(pylonKey("oil_extraction"));
    public static final SimpleStaticGuidePage OIL_REFINEMENT = new SimpleStaticGuidePage(pylonKey("oil_refining"));
    public static final SimpleStaticGuidePage DIESEL_MACHINES = new SimpleStaticGuidePage(pylonKey("diesel_machines"));
    public static final SimpleStaticGuidePage ELECTRICITY = new SimpleStaticGuidePage(pylonKey("electricity"));
    public static final SimpleStaticGuidePage ELECTRIC_GENERATORS = new SimpleStaticGuidePage(pylonKey("electric_generators"));
    public static final SimpleStaticGuidePage ELECTRIC_MACHINES = new SimpleStaticGuidePage(pylonKey("electric_machines"));
    public static final SimpleStaticGuidePage ELECTRIC_UTILITIES = new SimpleStaticGuidePage(pylonKey("electric_utilities"));
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

        RebarGuide.getRootPage().addPage(PylonItems.PRESS, SIMPLE_MACHINES);

        RebarGuide.getRootPage().addPage(PylonItems.SMELTERY_CONTROLLER, SMELTING);

        LOGISTICS.addPage(PylonItems.TIN_SILO, SILOS);
        LOGISTICS.addPage(PylonItems.CARGO_EXTRACTOR, CARGO);
        RebarGuide.getRootPage().addPage(PylonItems.CARGO_SPLITTER, LOGISTICS);

        RebarGuide.getRootPage().addPage(PylonItems.FLUID_PIPE_BRONZE, FLUID_HANDLING);
        FLUID_HANDLING.addPage(PylonItems.PORTABLE_FLUID_TANK_COPPER, FLUID_PIPES_AND_TANKS);
        FLUID_HANDLING.addPage(PylonItems.FLUID_METER, FLUID_MACHINES);
        FLUID_HANDLING.addPage(PylonItems.SMALL_BOILER, BOILERS);

        RebarGuide.getRootPage().addPage(PylonItems.HYDRAULIC_TABLE_SAW, HYDRAULICS);
        HYDRAULICS.addPage(PylonItems.HYDRAULIC_PIPE_BENDER, HYDRAULIC_MACHINES);
        HYDRAULICS.addPage(PylonItems.SOLAR_HYDRAULIC_PURIFIER_2, HYDRAULIC_PURIFICATION);

        RebarGuide.getRootPage().addPage(PylonItems.OIL_SAMPLE_DRILL, PETROCHEMICALS);
        PETROCHEMICALS.addPage(PylonItems.HYDRAULIC_PUMPJACK, OIL_EXTRACTION);
        PETROCHEMICALS.addPage(PylonItems.BIOREFINERY, OIL_REFINEMENT);

        RebarGuide.getRootPage().addPage(PylonItems.DIESEL_PIPE_BENDER, DIESEL_MACHINES);

        ELECTRICITY.addPage(PylonItems.GAS_TURBINE, ELECTRIC_GENERATORS);
        ELECTRICITY.addPage(PylonItems.ELECTRIC_GRINDSTONE, ELECTRIC_MACHINES);
        ELECTRICITY.addPage(PylonItems.WIRE_HUB, ELECTRIC_UTILITIES);
        RebarGuide.getRootPage().addPage(PylonItems.WIRE_1_GAUGE, ELECTRICITY); // TODO change to battery when added?

        RebarGuide.getRootPage().addPage(PylonItems.ASSEMBLY_TABLE, ASSEMBLING);

        RebarGuide.getRootPage().addPage(PylonItems.ELEVATOR_1, BUILDING);

        RebarGuide.getRootPage().addPage(PylonItems.BRONZE_PICKAXE, TOOLS);

        RebarGuide.getRootPage().addPage(PylonItems.BEHEADING_SWORD, COMBAT);

        RebarGuide.getRootPage().addPage(PylonItems.HEALTH_TALISMAN_SIMPLE, TALISMANS);

        RebarGuide.getRootPage().addPage(PylonItems.BRONZE_CHESTPLATE, ARMOUR);

        RebarGuide.getRootPage().addPage(Material.APPLE, FOOD);

        RebarGuide.getRootPage().addPage(PylonItems.FLUID_PIPE_CREATIVE, CREATIVE_ITEMS);
    }
}
