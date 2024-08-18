package ru.nwtls.reputationpaperplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.gui.GuiManager;
import ru.nwtls.reputationpaperplugin.listening.EventListener;

import java.io.File;
import java.util.logging.Logger;

public class PluginMain extends JavaPlugin {
    private GuiManager guiManager;
    private MainDatabase mainDatabase;
    private final @NotNull Logger logger = getLogger();
    @Override
    public void onEnable() {
        this.guiManager = new GuiManager();
        saveDefaultConfig();

        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));


        mainDatabase = new MainDatabase(
                getConfig().getString("main-database.url"),
                getConfig().getString("main-database.login"),
                getConfig().getString("main-database.password")
        );
        mainDatabase.init();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
    }

    public static @NotNull PluginMain getInstance() {
        return PluginMain.getPlugin(PluginMain.class);
    }

    public @NotNull GuiManager getGuiManager() {
        return this.guiManager;
    }

    public @NotNull MainDatabase getMainDatabase() {
        return this.mainDatabase;
    }

    public void loadConfig() {
        //TODO: delete if exists -> load config
        //TODO: default getConfig() return value ../ReputationPaperPlugin/config.yml
    }
}
