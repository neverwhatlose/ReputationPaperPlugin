package ru.nwtls.reputationpaperplugin.command;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.destroystokyo.paper.entity.villager.ReputationType;
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

import static ru.nwtls.reputationpaperplugin.util.StyleUtils.*;

public class ReputationCommand {
    private static final @NotNull MainDatabase mainDatabase = PluginMain.getInstance().getMainDatabase();
    private static final @NotNull Logger logger = PluginMain.getInstance().getLogger();

    public enum ReputationType {
        GOOD_REPUTATION,
        BAD_REPUTATION
    }

    public static void register(@NotNull PaperCommandManager<CommandSender> manager) {
        manager.command(manager
                .commandBuilder("reputation")
                .argument(ReputationTypeArgument.<CommandSender>builder("type").asRequired())
                .argument(PlayerArgument.<CommandSender>builder("target").asRequired())
                .senderType(Player.class)
                .handler(ctx -> handle((Player) ctx.getSender(), ctx.get("type"), ctx.get("target")))
        );
    }

    private static void handle(@NotNull Player author, @NotNull ReputationType type, @NotNull Player target) {
        if(hasPreviousDecision(target, author, type)) { author.sendMessage(green("Вы уже оценили этого игрока")); return; }
        if(author.getUniqueId() == target.getUniqueId()) { author.sendMessage(green("К сожалению, самого себя нельзя оценивать")); return; }

        switch (type) {
            case GOOD_REPUTATION:
        }
        mainDatabase.addGoodRep(target.getUniqueId(), author.getUniqueId());
        author.sendMessage(green("Cur good rep of player: " + mainDatabase.getGoodRep(target.getUniqueId())));
    }

    private static boolean hasPreviousDecision(@NotNull Player target, @NotNull Player author, @NotNull ReputationType type) {
        boolean result = false;
        switch (type) {
            case GOOD_REPUTATION:
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
            case BAD_REPUTATION:
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
        }
        return result;
    }
}
