package io.github.pylonmc.pylon;

import com.google.common.base.Preconditions;
import io.github.pylonmc.pylon.command.PylonCommand;
import io.github.pylonmc.pylon.content.building.Immobilizer;
import io.github.pylonmc.pylon.content.machines.fluid.Sprinkler;
import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.content.machines.smelting.Bloomery;
import io.github.pylonmc.pylon.content.talismans.*;
import io.github.pylonmc.pylon.content.tools.SoulboundRune;
import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.github.pylonmc.rebar.config.ConfigSection;
import io.github.pylonmc.rebar.config.adapter.ConfigAdapter;
import io.github.pylonmc.rebar.registry.RebarRegistry;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static io.github.pylonmc.pylon.util.PylonUtils.pylonKey;

public class Pylon extends JavaPlugin implements RebarAddon {

    private static final int BSTATS_ID = 31410;
    private static Metrics metrics;

    @Getter
    private static Pylon instance;

    @Override
    public void onEnable() {
        instance = this;

        metrics = new Metrics(this, BSTATS_ID);

        registerWithRebar();

        saveDefaultConfig();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(PylonCommand.ROOT);
        });

        PylonItems.initialize();
        PylonBlocks.initialize();
        PylonEntities.initialize();
        PylonFluids.initialize();
        PylonRecipes.initialize();


        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new Sprinkler.SprinklerPlaceListener(), this);
        pm.registerEvents(new Immobilizer.FreezeListener(), this);
        pm.registerEvents(new Rune.RuneListener(), this);
        pm.registerEvents(new SoulboundRune.SoulboundRuneListener(), this);
        pm.registerEvents(new Bloomery.CreationListener(), this);
        pm.registerEvents(new Grindstone.PlaceListener(), this);
        pm.registerEvents(new FarmingTalisman.FarmingTalismanListener(), this);
        pm.registerEvents(new BarteringTalisman.BarteringTalismanListener(), this);
        pm.registerEvents(new BreedingTalisman.BreedingTalismanListener(), this);
        pm.registerEvents(new EnchantingTalisman.EnchantingListener(), this);
        pm.registerEvents(new HuntingTalisman.HuntingTalismanListener(), this);
        pm.registerEvents(new ExperienceTalisman.XPTalismanListener(), this);

        RebarRegistry.RESEARCHES.mapKey(pylonKey("simple_components"), pylonKey("components_1"));
        RebarRegistry.RESEARCHES.mapKey(pylonKey("scientific_revolution_4"), pylonKey("scientific_revolution_3"));

        getLogger().info("Load config from nonexistent file");
        Preconditions.checkState(ConfigSection.from(new File("bruh")) == null);
        try {
            ConfigSection.fromOrThrow(new File("bruh"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Load config from nonexistent path");
        Preconditions.checkState(ConfigSection.from(Path.of("bruh")) == null);
        try {
            ConfigSection.fromOrThrow(Path.of("bruh"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Load config from nonexistent resource");
        Preconditions.checkState(ConfigSection.fromResource(this, "bruh") == null);
        try {
            ConfigSection.fromResourceOrThrow(this, "bruh");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Load config from nonexistent data folder path");
        Preconditions.checkState(ConfigSection.fromDataFolder(this, "bruh") == null);
        try {
            ConfigSection.fromDataFolderOrThrow(this, "bruh");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Load malformed config");
        try {
            ConfigSection.fromResource(this, "ohyes.yml");
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Read nonexistent key");
        try {
            ConfigSection.fromResourceOrThrow(this, "ohno.yml").getOrThrow("hesssssllo", ConfigAdapter.INTEGER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Read with incorrect config adapter");
        try {
            ConfigSection.fromResourceOrThrow(this, "ohno.yml").getOrThrow("hello", ConfigAdapter.INTEGER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Read with correct then incorrect config adapter");
        ConfigSection config1 = ConfigSection.fromResourceOrThrow(this, "ohno.yml");
        try {
            config1.getOrThrow("hello", ConfigAdapter.STRING);
            config1.getOrThrow("hello", ConfigAdapter.INTEGER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLogger().info("Read with two different valid config adapters");
        ConfigSection config2 = ConfigSection.fromResourceOrThrow(this, "ohno.yml");
        try {
            config2.getSectionOrThrow("oh_no").getOrThrow("is_everything_ok", ConfigAdapter.INTEGER);
            config2.getSectionOrThrow("oh_no").getOrThrow("is_everything_ok", ConfigAdapter.DOUBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NotNull JavaPlugin getJavaPlugin() {
        return instance;
    }

    @Override
    public @NotNull Set<@NotNull Locale> getLanguages() {
        return Set.of(Locale.ENGLISH);
    }

    @Override
    public @NotNull Material getMaterial() {
        return Material.COPPER_INGOT;
    }
}
