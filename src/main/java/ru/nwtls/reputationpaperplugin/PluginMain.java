package ru.nwtls.reputationpaperplugin;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.command.ReputationCommand;
import ru.nwtls.reputationpaperplugin.listening.EventListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Logger;

public class PluginMain extends JavaPlugin {
    private @NotNull MainDatabase mainDatabase;
    private final @NotNull Logger logger = getLogger();

    @Override
    public void onEnable() {
        loadConfig();
        try {
            logger.info("Connecting to database...");
            this.mainDatabase = new MainDatabase(
                    getConfig().getString("main-database.url"),
                    getConfig().getString("main-database.login"),
                    getConfig().getString("main-database.password")
            );
            this.mainDatabase.init();
        } catch (MainDatabase.MainDatabaseException e) {
            logger.warning(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            PaperCommandManager<CommandSender> commandManager = PaperCommandManager
                    .createNative(this, CommandExecutionCoordinator.simpleCoordinator());

            ReputationCommand.register(commandManager);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
    }

    public static @NotNull PluginMain getInstance() {
        return PluginMain.getPlugin(PluginMain.class);
    }

    public @NotNull MainDatabase getMainDatabase() {
        return this.mainDatabase;
    }

    public void loadConfig() {
        File configFolder = getDataFolder();
        File[] files = configFolder.listFiles();
        if (files == null) { saveDefaultConfig(); return; }
        Arrays.stream(files).forEach(it -> {
            try {
                Files.delete(it.toPath());
            } catch (IOException e) {
                logger.warning("On config load: " + e.getMessage());
            }
        });
        saveDefaultConfig();

    }
}
