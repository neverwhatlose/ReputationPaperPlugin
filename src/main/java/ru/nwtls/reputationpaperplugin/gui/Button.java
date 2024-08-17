package ru.nwtls.reputationpaperplugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nwtls.reputationpaperplugin.util.StyleUtils;

import java.util.List;

import static ru.nwtls.reputationpaperplugin.util.StyleUtils.*;

public class Button {
    private ItemStack item;
    private String title;
    private int id;
    private ButtonType type;
    private Object param;

    public enum ButtonType {
        DECORATIVE,
        EXECUTABLE,
        CLOSE
    }

    /**
     * Конструктор кнопки
     *
     * @param item предмет, используемый в качетсве кнопки
     * @param title название предмета
     */

    public Button(@NotNull ItemStack item, @Nullable String title) {
        this.item = item;
        this.item.editMeta(meta -> {
            if (title != null) meta.displayName(destyle(aqua(title)));
        });
        this.id = id;
        this.type = ButtonType.DECORATIVE;
    }

    /**
     * Конструктор кнопки
     *
     * @param item предмет, используемый в качестве кнопки
     * @param title название предмета
     * @param lore лор (описание) предмета
     */

    public Button(@NotNull ItemStack item, @Nullable String title, @Nullable List<@NotNull Component> lore) {
        this.item = item;
        this.item.editMeta(meta -> {
            if (title != null) meta.displayName(destyle(aqua(title)));
            meta.lore(lore == null ? null : lore.stream()
                    .map(StyleUtils::destyle)
                    .toList());
        });
        this.type = ButtonType.DECORATIVE;
    }

    public Button(@NotNull ItemStack item, @Nullable String title, @Nullable List<@NotNull Component> lore, Object obj) {
        this.item = item;
        this.item.editMeta(meta -> {
            if (title != null) meta.displayName(destyle(aqua(title)));
            meta.lore(lore == null ? null : lore.stream()
                    .map(StyleUtils::destyle)
                    .toList());
        });
        this.type = ButtonType.EXECUTABLE;
        this.param = obj;
    }

    public Button(@NotNull ItemStack item, @Nullable String title, ButtonType type) {
        this.item = item;
        this.item.editMeta(meta -> {
            if (title != null) meta.displayName(destyle(aqua(title)));
        });
        this.type = type;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getTitle(){
        return ((TextComponent) (this.getItem().getItemMeta().displayName())).content();
    }

    public void execute(GuiManager manager, Player player) {
        if (this.type.equals(ButtonType.CLOSE)) player.closeInventory();

        if (this.type == ButtonType.DECORATIVE) return;
        if (this.param instanceof Gui gui) manager.showGui(player, gui);
        if (this.param instanceof String command) {
            player.performCommand(command);
            player.closeInventory();
            manager.closeGui(player);
        }
    }
}
