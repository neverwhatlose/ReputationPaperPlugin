package ru.nwtls.reputationpaperplugin.command;

import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.nwtls.reputationpaperplugin.MainDatabase;
import ru.nwtls.reputationpaperplugin.PluginMain;

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

        manager.command(manager
                .commandBuilder("goodrep")
                .argument(PlayerArgument.<CommandSender>builder("target").asRequired())
                .senderType(Player.class)
                .handler(ctx -> handle((Player) ctx.getSender(), ReputationType.GOOD_REPUTATION, ctx.get("target")))
        );

        manager.command(manager
                .commandBuilder("badrep")
                .argument(PlayerArgument.<CommandSender>builder("target").asRequired())
                .senderType(Player.class)
                .handler(ctx -> handle((Player) ctx.getSender(), ReputationType.BAD_REPUTATION, ctx.get("target")))
        );

        manager.command(manager
                .commandBuilder("checkrep")
                .argument(PlayerArgument.<CommandSender>builder("target").asRequired())
                .senderType(Player.class)
                .handler(ctx -> handle((Player) ctx.getSender(), ctx.get("target")))
        );
    }

    private static void handle(@NotNull Player sender, @NotNull ReputationType type, @NotNull Player target) {
        if (hasPreviousDecision(target, sender, type)) {
            sender.sendMessage(green("Вы уже оценили этого игрока"));
            return;
        }
        if (sender.getUniqueId() == target.getUniqueId()) {
            sender.sendMessage(green("К сожалению, самого себя нельзя оценивать"));
            return;
        }

        switch (type) {
            case GOOD_REPUTATION -> {
                mainDatabase.addGoodRep(target.getUniqueId(), sender.getUniqueId());
                sender.sendMessage(single(
                        gray("Вы изменили репутацию "),
                        yellow(target.getName()),
                        gray(" на "),
                        green("+1!")
                ));
                Bukkit.broadcast(single(green("Репутация игрока " + target.getName() + " была изменена на +1 "), getTotalReputation(target)));
            }
            case BAD_REPUTATION -> {
                mainDatabase.addBadRep(target.getUniqueId(), sender.getUniqueId());
                sender.sendMessage(single(
                        gray("Вы изменили репутацию "),
                        yellow(target.getName()),
                        gray(" на "),
                        red("-1!")
                ));
                Bukkit.broadcast(single(red("Репутация игрока " + target.getName() + " была изменена на -1 "), getTotalReputation(target)));
            }
        }
    }

    private static void handle(@NotNull Player sender, @NotNull Player target) {
        int badrep = mainDatabase.getBadRep(target.getUniqueId());
        int goodrep = mainDatabase.getGoodRep(target.getUniqueId());

        Component msg = single(
                gray(target.getName()),
                yellow(" имеет репутацию: "),
                green(goodrep),
                gray("/ "),
                red(badrep),
                getTotalReputation(target)
                );
        sender.sendMessage(msg);
    }

    private static boolean hasPreviousDecision(@NotNull Player target, @NotNull Player sender, @NotNull ReputationType type) {
        switch (type) {
            case GOOD_REPUTATION:
                try (Connection connection = mainDatabase.getConnection()) {
                    String query = "SELECT * FROM goodreps WHERE (target = ? AND sender = ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, target.getUniqueId().toString());
                    statement.setString(2, sender.getUniqueId().toString());
                    return statement.executeQuery().next();
                } catch (SQLException e) {
                    logger.warning(e.getMessage());
                    return false;
                }
            case BAD_REPUTATION:
                try (Connection connection = mainDatabase.getConnection()) {
                    String query = "SELECT * FROM badreps WHERE (target = ? AND sender = ?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, target.getUniqueId().toString());
                    statement.setString(2, sender.getUniqueId().toString());
                    return statement.executeQuery().next();
                } catch (SQLException e) {
                    logger.warning(e.getMessage());
                    return false;
                }
        }
        return false;
    }

    private static @NotNull Component getTotalReputation(@NotNull Player player) {
        return gray("(" + (mainDatabase.getGoodRep(player.getUniqueId()) - mainDatabase.getBadRep(player.getUniqueId())) + ")");
    }
}
