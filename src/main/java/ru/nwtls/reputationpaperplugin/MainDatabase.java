package ru.nwtls.reputationpaperplugin;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MainDatabase {
    private final @NotNull Connection connection;
    private final @NotNull Logger logger = PluginMain.getInstance().getLogger();

    public MainDatabase(@NotNull Connection connection) {
        this.connection = connection;
    }

    public void init() {
        try (Connection connection = this.connection) {
            String query = "CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36) NOT NULL PRIMARY KEY)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            if (statement.getWarnings() != null) {
                logger.info("Table already exists, no creation is required");
            } else {
                logger.info("Table created successfully");
            }
        } catch (SQLException e) {
            logger.warning(e.getMessage());
        }
    }
}
