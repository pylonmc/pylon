package io.github.pylonmc.pylon;

import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

import io.github.pylonmc.rebar.item.RebarItemTag;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.github.pylonmc.rebar.item.ItemTypeWrapper;

public class PylonTags {
    public static final RebarItemTag BRONZE_EQUIPMENT = new RebarItemTag(pylonKey("bronze_equipment"), Set.of(
        ItemTypeWrapper.of(PylonItems.BRONZE_HELMET),
        ItemTypeWrapper.of(PylonItems.BRONZE_CHESTPLATE),
        ItemTypeWrapper.of(PylonItems.BRONZE_LEGGINGS),
        ItemTypeWrapper.of(PylonItems.BRONZE_BOOTS),
        ItemTypeWrapper.of(PylonItems.BRONZE_SWORD),
        ItemTypeWrapper.of(PylonItems.BRONZE_PICKAXE),
        ItemTypeWrapper.of(PylonItems.BRONZE_AXE),
        ItemTypeWrapper.of(PylonItems.BRONZE_SHOVEL),
        ItemTypeWrapper.of(PylonItems.BRONZE_HOE),
        ItemTypeWrapper.of(PylonItems.BRONZE_SCREWDRIVER)
    ));
    public static final RebarItemTag STEEL_EQUIPMENT = new RebarItemTag(pylonKey("steel_equipment"), Set.of(
        ItemTypeWrapper.of(PylonItems.STEEL_HELMET),
        ItemTypeWrapper.of(PylonItems.STEEL_CHESTPLATE),
        ItemTypeWrapper.of(PylonItems.STEEL_LEGGINGS),
        ItemTypeWrapper.of(PylonItems.STEEL_BOOTS),
        ItemTypeWrapper.of(PylonItems.STEEL_SWORD),
        ItemTypeWrapper.of(PylonItems.STEEL_PICKAXE),
        ItemTypeWrapper.of(PylonItems.STEEL_AXE),
        ItemTypeWrapper.of(PylonItems.STEEL_SHOVEL),
        ItemTypeWrapper.of(PylonItems.STEEL_HOE),
        ItemTypeWrapper.of(PylonItems.STEEL_SCREWDRIVER)
    ));
    public static final RebarItemTag PALLADIUM_EQUIPMENT = new RebarItemTag(pylonKey("palladium_equipment"), Set.of(
        ItemTypeWrapper.of(PylonItems.PALLADIUM_HELMET),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_CHESTPLATE),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_LEGGINGS),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_BOOTS),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_SWORD),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_PICKAXE),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_AXE),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_SHOVEL),
        ItemTypeWrapper.of(PylonItems.PALLADIUM_HOE)
    ));
    
    @SuppressWarnings("unchecked")
    public static void initialize() {
        RebarRegistry.ITEM_TAGS.register(
            BRONZE_EQUIPMENT,
            STEEL_EQUIPMENT,
            PALLADIUM_EQUIPMENT
        );
    }
}
