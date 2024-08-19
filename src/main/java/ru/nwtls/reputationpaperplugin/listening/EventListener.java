package ru.nwtls.reputationpaperplugin.listening;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.MainDatabase;
import ru.nwtls.reputationpaperplugin.PluginMain;

public class EventListener implements Listener {
    private final @NotNull MainDatabase mainDatabase = PluginMain.getInstance().getMainDatabase();

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        this.mainDatabase.addPlayer(event.getPlayer().getUniqueId());
    }
}
