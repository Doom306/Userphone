package com.general_hello.commands.Database;

import com.general_hello.commands.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SQLiteDataSource implements DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDataSource.class);
    public static HikariDataSource ds;

    public SQLiteDataSource() {
        try {
            final File dbFile = new File("database.db");

            if (!dbFile.exists()) {
                if (dbFile.createNewFile()) {
                    LOGGER.info("Created database file");
                } else {
                    LOGGER.info("Could not create database file");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:database.db");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);

        try (final Statement statement = getConnection().createStatement()) {
            final String defaultPrefix = Config.get("prefix");

            String[] commands = new String[]{
                    "CREATE TABLE IF NOT EXISTS guild_settings (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "guild_id VARCHAR(20) NOT NULL," +
                            "prefix VARCHAR(255) NOT NULL DEFAULT '" + defaultPrefix + "'" +
                            ");",
                    "CREATE TABLE IF NOT EXISTS UserData ( UserId INTEGER NOT NULL, " +
                            "UserName TEXT NOT NULL, " +
                            "UserProfilePicLink TEXT, " +
                            "PRIMARY KEY(UserId) ) WITHOUT ROWID",
                    "CREATE TABLE IF NOT EXISTS XPSystemUser (" +
                            "userId INTEGER UNIQUE," +
                            "xpPoints INTEGER DEFAULT 0" +
                            ");",
                    "CREATE TABLE IF NOT EXISTS GuildSettings (" +
                            "XPSystem INTEGER DEFAULT 0," +
                            "GuildId INTEGER" +
                            ");"
            };

            Connection connection = getConnection();

            if (connection == null) return;

            try {
                for (String command : commands) {
                    PreparedStatement ps = connection.prepareStatement(command);
                    ps.execute();
                    ps.close();
                }
            } catch (Exception e) {
                LOGGER.error("Could not run command", e);
            }
            LOGGER.info("Table initialised");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPrefix(long guildId) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("SELECT prefix FROM guild_settings WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("prefix");
                }
            }

            try (final PreparedStatement insertStatement = getConnection()
                    // language=SQLite
                    .prepareStatement("INSERT INTO guild_settings(guild_id) VALUES(?)")) {

                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Config.get("prefix");
    }

    @Override
    public void setPrefix(long guildId, String newPrefix) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("UPDATE guild_settings SET prefix = ? WHERE guild_id = ?")) {

            preparedStatement.setString(1, newPrefix);
            preparedStatement.setString(2, String.valueOf(guildId));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName(long userId) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection
                // language=SQLite
                .prepareStatement("SELECT UserName FROM UserData WHERE UserId = ?")) {

            preparedStatement.setString(1, String.valueOf(userId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("UserName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Integer getXpPoints(long userId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection
                     // language=SQLite
                     .prepareStatement("SELECT xpPoints FROM XPSystemUser WHERE userId = ?")) {

            preparedStatement.setString(1, String.valueOf(userId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("xpPoints");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = getConnection();
             PreparedStatement insertStatement = connection
                // language=SQLite
                .prepareStatement("INSERT INTO XPSystemUser(userId) VALUES(?)")) {

            insertStatement.setString(1, String.valueOf(userId));

            insertStatement.execute();
        }

        return 0;
    }

    @Override
    public void setXpPoints(long userId, long xpPoints) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("UPDATE XPSystemUser SET xpPoints=? WHERE UserId=?"
                )) {

            int xpPoints1 = (int) xpPoints;
            preparedStatement.setString(2, String.valueOf(userId));
            preparedStatement.setInt(1, xpPoints1);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getGuildSettings(long guildId) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection
                     // language=SQLite
                     .prepareStatement("SELECT XPSystem FROM GuildSettings WHERE GuildId = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("XPSystem");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection connection = getConnection();
             PreparedStatement insertStatement = connection
                     // language=SQLite
                     .prepareStatement("INSERT INTO GuildSettings(GuildId) VALUES(?)")) {

            insertStatement.setString(1, String.valueOf(guildId));

            insertStatement.execute();
        }

        return 0;
    }

    @Override
    public void setGuildSettings(long guildId, long enabledOrDisabled) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("UPDATE GuildSettings SET XPSystem=? WHERE GuildId=?"
                )) {

            preparedStatement.setString(2, String.valueOf(guildId));
            preparedStatement.setString(1, String.valueOf(enabledOrDisabled));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getProfilePictureLink(long userId) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT UserProfilePicLink FROM UserData WHERE UserId = ?")) {

            preparedStatement.setString(1, String.valueOf(userId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("UserProfilePicLink");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void newInfo(long userId, String userName, String profilePictureLink) {
        try (final PreparedStatement preparedStatement = getConnection()
             // language=SQLite
             .prepareStatement("INSERT INTO UserData" +
                     "(UserId, UserName, UserProfilePicLink)" +
                     "VALUES (?, ?, ?);")) {

            preparedStatement.setString(1, String.valueOf(userId));
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, profilePictureLink);

            preparedStatement.executeUpdate();
            preparedStatement.close();

            System.out.println("Added the user to the database successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setName(long userId, String name) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("UPDATE UserData SET UserName=? WHERE UserId=?"
                )) {

            preparedStatement.setString(2, String.valueOf(userId));
            preparedStatement.setString(1, name);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setProfilePictureLink(long userId, String link) {
        try (final PreparedStatement preparedStatement = getConnection()
                // language=SQLite
                .prepareStatement("UPDATE UserData SET UserProfilePicLink=? WHERE UserId=?"
                )) {

            preparedStatement.setString(2, String.valueOf(userId));
            preparedStatement.setString(1, link);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
