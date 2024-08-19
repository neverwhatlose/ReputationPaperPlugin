package ru.nwtls.reputationpaperplugin.util;

import java.text.SimpleDateFormat;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Formatter {
    public static @NotNull String locationToString(@Nullable Location location) {
        if (location == null) {
            return "не установлена";
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return "x" + x + "/y" + y + "/z" + z + "/" + location.getWorld().getName();
    }

    public static @NotNull String teleportToLocation(@NotNull Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return "/tp " + x + " " + y + " " + z;
    }

    public static @NotNull String formatTime(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd.MM.yy");
        return format.format(millis);
    }
}
