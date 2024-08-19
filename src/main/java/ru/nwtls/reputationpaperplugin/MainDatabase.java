package ru.nwtls.reputationpaperplugin;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainDatabase {
    private final @NotNull Logger logger = PluginMain.getInstance().getLogger();

    private final @NotNull String url;
    private final @NotNull String login;
    private final @NotNull String password;

    public enum TableName {
        PLAYERS_TABLE,
        GOODREPS_TABLE,
        BADREPS_TABLE
    }

    //temp or idk
    public enum TablesColumn {
        TARGET_COLUMN,
        SENDER_COLUMN,
        UUID_COLUMN,
        GOODREP_COLUMN,
        BADREP_COLUMN
    }

    public MainDatabase(@Nullable String url, @Nullable String login, @Nullable String password) throws MainDatabaseException {
        if (url == null || login == null || password == null) {
            throw new MainDatabaseException("Provided connection configuration to database is incorrect, startup aborted");
        }
        this.url = url;
        this.login = login;
        this.password = password;
    }

    public static final class MainDatabaseException extends Exception {
        public MainDatabaseException(String message) {
            super(message);
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(this.url, this.login, this.password);
        } catch (SQLException e) {
            this.logger.warning("Failed to connect to database, connection aborted, exception: " + e.getMessage());
        }
        return connection;
    }

    public void init() {
        HashMap<String, String> tables = new HashMap<>();

        tables.put("players", "CREATE TABLE IF NOT EXISTS players ("
                + "uuid VARCHAR(36) NOT NULL PRIMARY KEY,"
                + "goodrep TINYINT UNSIGNED NOT NULL,"
                + "badrep TINYINT UNSIGNED NOT NULL"
                + ");");
        tables.put("goodreps", "CREATE TABLE IF NOT EXISTS goodreps ("
                + "target VARCHAR(36) NOT NULL,"
                + "sender VARCHAR(36) NOT NULL"
                + ");");
        tables.put("badreps", "CREATE TABLE IF NOT EXISTS badreps ("
                + "target VARCHAR(36) NOT NULL,"
                + "sender VARCHAR(36) NOT NULL"
                + ");");

        List<String> tablesName = List.of("players", "goodreps", "badreps");

        this.initializeTables(tablesName, tables);
    }

    public void addPlayer(@NotNull UUID uuid) {
        try (Connection connection = this.getConnection()) {
            if (this.isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, uuid.toString())) return;

            String query = "INSERT INTO `players` (uuid, goodrep, badrep) VALUES (?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());
            statement.setInt(2, 0);
            statement.setInt(3, 0);

            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addPlayer: " + e.getMessage());
        }
    }

    public void addGoodRep(@NotNull UUID targetUUID, @NotNull UUID senderUUID) {
        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) this.addPlayer(targetUUID);

            String query = "UPDATE `players` SET `goodrep`= goodrep + 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addGoodRep/UPDATE: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) this.addPlayer(targetUUID);

            String query = "INSERT INTO `goodreps` (target, sender) VALUES (?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, senderUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addGoodRep/INSERT: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.SENDER_COLUMN, senderUUID.toString()))) return;

            String query = "DELETE FROM `badreps` WHERE (target = ? AND sender = ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, senderUUID.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addGoodRep/DELETE: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.GOODREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.SENDER_COLUMN, senderUUID.toString()))) return;

            String query = "UPDATE `players` SET `badrep`= badrep - 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addGoodRep/UPDATE-REMOVE: " + e.getMessage());
        }
    }

    public void addBadRep(@NotNull UUID targetUUID, @NotNull UUID senderUUID) {
        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) this.addPlayer(targetUUID);

            String query = "UPDATE `players` SET `badrep`= badrep + 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addBadRep/UPDATE: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.PLAYERS_TABLE, TablesColumn.UUID_COLUMN, targetUUID.toString()))) this.addPlayer(targetUUID);

            String query = "INSERT INTO `badreps` (target, sender) VALUES (?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, senderUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addBadRep/INSERT: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.SENDER_COLUMN, senderUUID.toString()))) return;

            String query = "DELETE FROM `goodreps` WHERE (target = ? AND sender = ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.setString(2, senderUUID.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addBadRep/DELETE: " + e.getMessage());
        }

        try (Connection connection = this.getConnection()) {
            if (!(this.isExists(TableName.BADREPS_TABLE, TablesColumn.TARGET_COLUMN, targetUUID.toString(), TablesColumn.SENDER_COLUMN, senderUUID.toString()))) return;

            String query = "UPDATE `players` SET `goodrep`= goodrep - 1 WHERE uuid=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.logger.warning("addBadRep/UPDATE-REMOVE: " + e.getMessage());
        }
    }

    public int getGoodRep(@NotNull UUID targetUUID) {
        try (Connection connection = this.getConnection()) {
            String query = "SELECT * FROM players WHERE uuid = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getShort("goodrep");
            }
        } catch (SQLException e) {
            this.logger.warning("getGoodRep: " + e.getMessage());
        }
        return -1;
    }

    public int getBadRep(@NotNull UUID targetUUID) {
        try (Connection connection = this.getConnection()) {
            String query = "SELECT * FROM players WHERE uuid = ?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, targetUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getShort("badrep");
            }
        } catch (SQLException e) {
            this.logger.warning("getBadRep: " + e.getMessage());
        }
        return -1;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn column, @NotNull String param) {
        try (Connection connection = this.getConnection()) {
            String query = "SELECT * FROM " + this.validTableName(table) + " WHERE " + this.validColumnName(column) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, param);

            return statement.executeQuery().next();
        } catch (SQLException e) {
            this.logger.warning("isExists: " + e.getMessage());
        }
        return false;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn targetColumn, @NotNull String targetParam, @NotNull TablesColumn senderColumn, @NotNull String senderParam) {
        try (Connection connection = this.getConnection()) {
            String query = "SELECT * FROM " + this.validTableName(table) + " WHERE " + this.validColumnName(targetColumn) + " = ? AND " + this.validColumnName(senderColumn) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, targetParam);
            statement.setString(2, senderParam);

            return statement.executeQuery().next();
        } catch (SQLException e) {
            this.logger.warning("isExists: " + e.getMessage());
        }
        return false;
    }

    public boolean isExists(@NotNull TableName table, @NotNull TablesColumn column, @NotNull Integer param) {
        try (Connection connection = this.getConnection()) {
            String query = "SELECT * FROM " + this.validTableName(table) + " WHERE " + this.validColumnName(column) + " = ?;";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, param);

            return statement.executeQuery().next();
        } catch (SQLException e) {
            this.logger.warning(e.getMessage());
        }
        return false;
    }

    private void initializeTables(@NotNull List<String> tablesName, @NotNull HashMap<String, String> tables) {
        for (String name : tablesName) {
            try (Connection connection = this.getConnection()) {
                String query = tables.get(name);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.executeUpdate();
                if (statement.getWarnings() == null) {
                    this.logger.info("Table '" + name + "' created successfully");
                }
            } catch (SQLException e) {
                this.logger.warning("Failed to create the table '" + name + "' , connection aborted, exception: " + e.getMessage());
            }
        }
        this.logger.info("Connection to database established");
    }

    private @NotNull String validTableName(@NotNull TableName table) {
        return table.toString().toLowerCase().replace("_table", "");
    }

    private @NotNull String validColumnName(@NotNull TablesColumn column) {
        return column.toString().toLowerCase().replace("_column", "");
    }
}
