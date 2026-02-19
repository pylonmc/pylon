package io.github.pylonmc.pylon;

import io.github.pylonmc.pylon.command.PylonCommand;
import io.github.pylonmc.pylon.content.building.Immobilizer;
import io.github.pylonmc.pylon.content.machines.fluid.Sprinkler;
import io.github.pylonmc.pylon.content.machines.simple.Grindstone;
import io.github.pylonmc.pylon.content.machines.smelting.Bloomery;
import io.github.pylonmc.pylon.content.talismans.*;
import io.github.pylonmc.pylon.content.tools.ShimmerMagnet;
import io.github.pylonmc.pylon.content.tools.SoulboundRune;
import io.github.pylonmc.pylon.content.tools.base.Rune;
import io.github.pylonmc.rebar.addon.RebarAddon;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

public class Pylon extends JavaPlugin implements RebarAddon {

    private static final int BSTATS_ID = 27323;
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

        new ShimmerMagnet.Ticker().runTaskTimer(this, 0, 10);

        pm.registerEvents(new HungerTalisman.JoinListener(), this);
        pm.registerEvents(new FarmingTalisman.FarmingTalismanListener(), this);
        pm.registerEvents(new BarteringTalisman.BarteringTalismanListener(), this);
        pm.registerEvents(new BreedingTalisman.BreedingTalismanListener(), this);
        pm.registerEvents(new EnchantingTalisman.EnchantingListener(), this);
        pm.registerEvents(new HuntingTalisman.HuntingTalismanListener(), this);
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
