package ru.nwtls.reputationpaperplugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nwtls.reputationpaperplugin.PluginMain;

import java.util.HashMap;
import java.util.UUID;

public class GuiManager {
    private final HashMap<UUID, Gui> guis = new HashMap<>();
    private static final PluginMain plugin = PluginMain.getInstance();

    public GuiManager() { }

    public void addGui(@NotNull Player player, @NotNull Gui gui) {
        guis.put(player.getUniqueId(), gui);
    }

    public @Nullable Gui getGui(@NotNull UUID uuid) {
        return guis.get(uuid);
    }

    /**
     *
     * @param player target
     * @param gui gui
     */

    public void showGui(@NotNull Player player, @NotNull Gui gui) {
        player.openInventory(gui.getInventory());
        guis.put(player.getUniqueId(), gui);
        player.setMetadata("profile", new FixedMetadataValue(plugin, true));
    }

    public void updateGui(@NotNull Player player, @NotNull Gui gui) {
        if (guis.get(player.getUniqueId()) == null) {
            System.out.println("Gui does not exists");
            return;
        }
        guis.put(player.getUniqueId(), gui);
    }

    public void closeGui(@NotNull Player player) {
        if (!(player.hasMetadata("profile"))) return;
        player.removeMetadata("profile", plugin);
        guis.remove(player.getUniqueId());
    }

    public @NotNull HashMap<UUID, Gui> getGuis() {
        return guis;
    }
}
