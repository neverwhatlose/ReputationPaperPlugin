package ru.nwtls.reputationpaperplugin.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.gson.internal.bind.util.ISO8601Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.MainDatabase;
import ru.nwtls.reputationpaperplugin.PluginMain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ReputationCommand {
    private static final @NotNull MainDatabase mainDatabase = PluginMain.getInstance().getMainDatabase();
    private static final @NotNull Logger logger = PluginMain.getInstance().getLogger();
    public static void register(@NotNull PaperCommandManager<CommandSender> manager) {
        manager.command(manager
                .commandBuilder("reputation")
                .argument(StringArgument.<CommandSender>builder("type").asRequired())
                .argument(PlayerArgument.<CommandSender>builder("target").asRequired())
                .senderType(Player.class)
                .handler(ctx -> handle((Player) ctx.getSender(), ctx.get("type"), ctx.get("target")))
                //прикрутить бы парсер, который парсит type и выдает либо [good, bad] либо [idk], чтобы отсеять неверный тип
        );
    }

    private static void handle(@NotNull Player author, @NotNull String type, @NotNull Player target) {
        System.out.println(hasPreviousDecision(target, author, type));
    }

    private static boolean hasPreviousDecision(@NotNull Player target, @NotNull Player author, @NotNull String type) {
        boolean result = false;
        switch (type) {
            case "good":
                try (Connection connection = mainDatabase.getConnection()) {
                    String query = "SELECT * FROM `goodreps` WHERE (target = ? AND author = ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, target.getUniqueId().toString());
                    statement.setString(2, author.getUniqueId().toString());
                    ResultSet resultSet = statement.executeQuery();
                    result = resultSet.next();
                    return result;
                } catch (SQLException e) {
                    logger.warning(e.getMessage());
                }
            case "bad":
                try (Connection connection = mainDatabase.getConnection()) {
                    String query = "SELECT * FROM `badreps` WHERE (target = ? AND author = ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, target.getUniqueId().toString());
                    statement.setString(2, author.getUniqueId().toString());
                    ResultSet resultSet = statement.executeQuery();
                    result = resultSet.next();
                    return result;
                } catch (SQLException e) {
                    logger.warning(e.getMessage());
                }
            default:
                //сюда надо чета написать
                break;
        }
        return result;
    }
}
