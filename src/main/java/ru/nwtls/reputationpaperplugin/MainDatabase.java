package ru.nwtls.reputationpaperplugin;

import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class MainDatabase {
    private final @NotNull Logger logger = PluginMain.getInstance().getLogger();

    private final String url;
    private final String login;
    private final String password;

    public enum TableName {
        PLAYERS_TABLE,
        GOODREPS_TABLE,
        BADREPS_TABLE
    }

    //temp or idk
    public enum TablesColumn {
        TARGET_COLUMN,
        AUTHOR_COLUMN,
        UUID_COLUMN,
        GOODREP_COLUMN,
        BADREP_COLUMN
    }

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
            if (isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, uuid.toString())) return;

            String query = "INSERT INTO `players` (uuid, goodrep, badrep) VALUES (?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());
            statement.setInt(2, 0);
            statement.setInt(3, 0);

            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning("addPlayer: " + e.getMessage());
        }
    }

    public void addGoodRep(@NotNull UUID targetUUID, @NotNull UUID authorUUID) {
        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) addPlayer(targetUUID);

            String query = "UPDATE `players` SET `goodrep`= goodrep + 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning("addGoodRep/UPDATE: " + e.getMessage());
        }

        try (Connection connection = getConnection()){
            if (!(isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) addPlayer(targetUUID);

            String query = "INSERT INTO `goodreps` (target, author) VALUES (?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, authorUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warning("addGoodRep/INSERT: " + e.getMessage());
        }

        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.AUTHOR_COLUMN, authorUUID.toString()))) return;

            String query = "DELETE FROM `badreps` WHERE (target = ? AND author = ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, authorUUID.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warning("addGoodRep/DELETE: " + e.getMessage());
        }

        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.GOODREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.AUTHOR_COLUMN, authorUUID.toString()))) return;

            String query = "UPDATE `players` SET `badrep`= badrep - 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning("addGoodRep/UPDATE-REMOVE: " + e.getMessage());
        }
    }

    public void addBadRep(@NotNull UUID targetUUID, @NotNull UUID authorUUID) {
        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) addPlayer(targetUUID);

            String query = "UPDATE `players` SET `badrep`= badrep + 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning("addBadRep/UPDATE: " + e.getMessage());
        }

        try (Connection connection = getConnection()){
            if (!(isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) addPlayer(targetUUID);

            String query = "INSERT INTO `badreps` (target, author) VALUES (?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, authorUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warning("addBadRep/INSERT: " + e.getMessage());
        }

        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.AUTHOR_COLUMN, authorUUID.toString()))) return;

            String query = "DELETE FROM `goodreps` WHERE (target = ? AND author = ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, authorUUID.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warning("addBadRep/DELETE: " + e.getMessage());
        }

        try (Connection connection = getConnection()) {
            if (!(isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.AUTHOR_COLUMN, authorUUID.toString()))) return;

            String query = "UPDATE `players` SET `goodrep`= goodrep - 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            //заглушка
            logger.warning("addBadRep/UPDATE-REMOVE: " + e.getMessage());
        }
    }

    public int getGoodRep(@NotNull UUID targetUUID) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM players WHERE uuid = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getShort("goodrep");
            }
        } catch (SQLException e) {
            logger.warning("getGoodRep: " + e.getMessage());
        }
        return -1;
    }

    public int getBadRep(@NotNull UUID targetUUID) {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM players WHERE uuid = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getShort("badrep");
            }
        } catch (SQLException e) {
            logger.warning("getBadRep: " + e.getMessage());
        }
        return -1;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn column, @NotNull String param) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM " + validTableName(table) + " WHERE " + validColumnName(column) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, param);

            ResultSet resultSet = statement.executeQuery();
            result = resultSet.next();
        } catch (SQLException e) {
            //заглушка
            logger.warning("isExists: " + e.getMessage());
        }
        return result;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn targetColumn, @NotNull String targetParam, @NotNull TablesColumn authorColumn, @NotNull String authorParam) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM " + validTableName(table) +" WHERE " + validColumnName(targetColumn) + " = ? AND " + validColumnName(authorColumn) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, targetParam);
            statement.setString(2, authorParam);

            ResultSet resultSet = statement.executeQuery();
            result = resultSet.next();
        } catch (SQLException e) {
            //заглушка
            logger.warning("isExists: " + e.getMessage());
        }
        return result;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn column, @NotNull Integer param) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM " + validTableName(table) + " WHERE " + validColumnName(column) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, param);

            ResultSet resultSet = statement.executeQuery();
            result = resultSet.next();
        } catch (SQLException e) {
            //заглушка
            logger.warning(e.getMessage());
        }
        return result;
    }

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

    private @NotNull String validTableName(@NotNull TableName table) {
        return table.toString().toLowerCase().replace("_table", "");
    }

    private @NotNull String validColumnName(@NotNull TablesColumn column) {
        return column.toString().toLowerCase().replace("_column", "");
    }
}
