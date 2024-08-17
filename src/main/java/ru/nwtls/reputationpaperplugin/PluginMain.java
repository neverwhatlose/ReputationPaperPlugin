package ru.nwtls.reputationpaperplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.gui.GuiManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

        connectToDatabase();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void connectToDatabase() {
        try {
            logger.info("Connecting to database...");
            Connection connection = DriverManager.getConnection(
                    getConfig().getString("main-database.url"),
                    getConfig().getString("main-database.login"),
                    getConfig().getString("main-database.password")
            );
            this.mainDatabase = new MainDatabase(connection);
            mainDatabase.init();
            logger.info("Main-Database connection established");
        } catch (SQLException e) {
            logger.warning("Failed to connect to database, connection aborted, exception: " + e.getMessage());
        }
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
        //TODO: config - local variable getConfig() (necessary???)
    }
}
