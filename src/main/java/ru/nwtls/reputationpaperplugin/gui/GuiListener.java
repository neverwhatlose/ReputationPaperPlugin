package ru.nwtls.reputationpaperplugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.PluginMain;

public class GuiListener implements Listener {
    private static final @NotNull PluginMain plugin = PluginMain.getInstance();
    private static final @NotNull GuiManager manager = plugin.getGuiManager();

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Gui gui = manager.getGui(player.getUniqueId());

        if (event.getClickedInventory() == null || player.getMetadata("profile").isEmpty() || gui == null) return;
        event.setCancelled(true);

        Button button = gui.getButtonById(event.getSlot());
        if (button == null) return;
        button.execute(manager, player);
    }

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        manager.closeGui((Player) event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(@NotNull PlayerQuitEvent event) {
        manager.closeGui(event.getPlayer());
    }
}
