package ru.nwtls.reputationpaperplugin;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import ru.nwtls.reputationpaperplugin.command.ReputationCommand;
import ru.nwtls.reputationpaperplugin.listening.EventListener;

public class PluginMain extends JavaPlugin {
    private MainDatabase mainDatabase;
    private final @NotNull Logger logger = getLogger();

    @Override
    public void onEnable() {
        this.loadConfig();
        try {
            this.logger.info("Connecting to database...");
            this.mainDatabase = new MainDatabase(
                    this.getConfig().getString("main-database.url"),
                    this.getConfig().getString("main-database.login"),
                    this.getConfig().getString("main-database.password")
            );
            this.mainDatabase.init();
        } catch (MainDatabase.MainDatabaseException e) {
            this.logger.warning(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            PaperCommandManager<CommandSender> commandManager = PaperCommandManager
                    .createNative(this, CommandExecutionCoordinator.simpleCoordinator());

            ReputationCommand.register(commandManager);
        } catch (Exception e) {
            this.logger.warning(e.getMessage());
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

    @TestOnly
    public void loadConfig() {
        File configFolder = getDataFolder();
        File[] files = configFolder.listFiles();
        if (files == null) {
            this.saveDefaultConfig();
            return;
        }
        Arrays.stream(files).forEach(it -> {
            try {
                if (it.getName().equals("config.yml")) Files.delete(it.toPath());
            } catch (IOException e) {
                this.logger.warning("On config load: " + e.getMessage());
            }
        });
        this.saveDefaultConfig();
    }

}
