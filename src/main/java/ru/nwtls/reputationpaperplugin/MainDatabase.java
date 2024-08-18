package ru.nwtls.reputationpaperplugin;

import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class MainDatabase {
    private final @NotNull PluginMain plugin = PluginMain.getInstance();
    private final @NotNull Logger logger = PluginMain.getInstance().getLogger();

    private String url;
    private String login;
    private String password;

    public MainDatabase(@NotNull String url, @NotNull String login, @NotNull String password) {
        this.url = url;
        this.login = login;
        this.password = password;
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            logger.warning("Failed to connect to database, connection aborted, exception: " + e.getMessage());
        }
        return connection;
    }

    public void init() {
        logger.info("Connecting to database...");
        HashMap<String, String> tables = new HashMap<>();
        List<String> tablesName = List.of("players", "goodreps", "badreps");

        tables.put("players", "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "goodrep TINYINT UNSIGNED NOT NULL," +
                "badrep TINYINT UNSIGNED NOT NULL" +
                ");");
        tables.put("goodreps", "CREATE TABLE IF NOT EXISTS goodreps (" +
                "target VARCHAR(36) NOT NULL PRIMARY KEY," +
                "author VARCHAR(36) NOT NULL" +
                ");");
        tables.put("badreps", "CREATE TABLE IF NOT EXISTS badreps (" +
                "target VARCHAR(36) NOT NULL PRIMARY KEY," +
                "author VARCHAR(36) NOT NULL" +
                ");");

        initializeTables(tablesName, tables);
    }

    public void addPlayer(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            if (isExists(uuid)) return;
            String query = "INSERT INTO `players` (uuid, goodrep, badrep) VALUES (?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);

            //TODO: PreparedStatement updateStatement(List<Object> list):
            //      .stream().map(statement.set{type of object}(id, object))
            statement.setString(1, uuid.toString());
            statement.setInt(2, 0);
            statement.setInt(3, 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning(e.getMessage());
        }
    }

    public void addGoodRep(@NotNull UUID uuid) {
        try (Connection connection = getConnection()) {
            if (!(isExists(uuid))) addPlayer(uuid);
            String query = "UPDATE `players` SET `goodrep`= goodrep - 1 WHERE name=?;";
            PreparedStatement statement = connection.prepareStatement(query);

        } catch (SQLException e) {
            //заглушка
            logger.warning(e.getMessage());
        }
    }

    public void addBadRep(@NotNull UUID uuid) {

    }

    public void getGoodRep(@NotNull UUID uuid) {

    }

    public void getBadRep(@NotNull UUID uuid) {

    }

    public boolean isExists(@NotNull UUID uuid) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM `players` WHERE `uuid`=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            result = resultSet.next();
        } catch (SQLException e) {
            //заглушка
            logger.warning(e.getMessage());
        }
        return result;
    }

    //вот это уже в handle()
    //public boolean hasPreviousDecision(@NotNull String )

    private void initializeTables(@NotNull List<String> tablesName, @NotNull HashMap<String, String> tables) {
        for (String name : tablesName) {
            try (Connection connection = getConnection()) {
                String query = tables.get(name);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.executeUpdate();
                if (statement.getWarnings() != null) {
                    logger.info("Table '" + name + "' already exists, no creation is required");
                } else {
                    logger.info("Table '" + name + "' created successfully");
                }
            } catch (SQLException e) {
                logger.warning("Failed to create the table '" + name + "' , connection aborted, exception: " + e.getMessage());
            }
        }
    }
}
