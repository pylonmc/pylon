package io.github.pylonmc.pylon.guide;

import io.github.pylonmc.pylon.PylonItems;
import io.github.pylonmc.pylon.content.machines.electricity.generation.GeothermalPlant;
import io.github.pylonmc.rebar.guide.button.PageButton;
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage;
import io.github.pylonmc.rebar.i18n.RebarArgument;
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.invui.item.Item;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class GeothermalBiomesPage extends SimpleStaticGuidePage {

    private static final GeothermalBiomesPage INSTANCE = new GeothermalBiomesPage();
    @Getter
    private static final Item button = new PageButton(
            ItemStackBuilder.copyOf(PylonItems.GEOTHERMAL_PLANT)
                    .name(Component.translatable("pylon.guide.page.geothermal_biomes"))
                    .lore(Collections.emptyList())
                    .build(),
            INSTANCE
    );

    private GeothermalBiomesPage() {
        List<Item> items = new ArrayList<>();
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(2);
        for (Biome biome : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)) {
            ItemStack item = GeothermalPlant.BiomeSettings.getItem(biome);
            if (item == null) continue;

            @SuppressWarnings("DataFlowIssue")
            double temp = GeothermalPlant.BiomeSettings.getTemperature(biome);

            items.add(Item.simple(ItemStackBuilder.copyOf(item)
                    .name(Component.translatable(biome.translationKey()))
                    .lore(Component.translatable(
                            "pylon.guide.button.geothermal_biomes.temperature",
                            RebarArgument.of("temperature", format.format(temp))
                    ))
                    .build()
            ));
        }
        super(pylonKey("geothermal_biomes"), items);
    }
}
