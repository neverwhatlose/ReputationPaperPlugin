package ru.nwtls.reputationpaperplugin.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import ru.nwtls.reputationpaperplugin.PluginMain;

import java.util.HashMap;

public class Gui implements InventoryHolder {
    private final Inventory inventory;
    private final int rows;
    private final Component title;

    private static final @NotNull GuiManager manager = PluginMain.getInstance().getGuiManager();

    private final HashMap<Integer, Button> buttons;

    public Gui(@NotNull Component title, @Range(from = 1, to = 6) int rows, @NotNull Player player) {
        this.rows = rows;
        this.title = title;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.buttons = new HashMap<>();
        manager.addGui(player, this);
    }

    public int getRows() {
        return rows;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public @Nullable Button getButtonById(int id) {
        return buttons.get(id);
    }

    /**
     * Установка кнопки в инвентаре
     *
     * @param row столбец (0 - 5)
     * @param column строка (0 - 8)
     * @param button кнопка (Button.java)
     * @return Gui
     */

    @Contract("_, _, _ -> this")
    public @NotNull Gui setButton(@Range(from = 0, to = 5) int row, @Range(from = 0, to = 8) int column, @NotNull Button button) {
        if (checkRange(this, row)) {
            this.inventory.setItem(row * 9 + column, button.getItem());
            this.buttons.put(row * 9 + column, button);
        }
        return this;
    }

    public HashMap<Integer, Button> getButtons() {
        return this.buttons;
    }

    private static boolean checkRange(@NotNull Gui inv, int row) {
        return inv.getRows() - 1 >= row;
    }
}
