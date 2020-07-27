/*
 * SquirrelID, a UUID library for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) SquirrelID team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.squirrelid.cache;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.sk89q.squirrelid.Profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * An implementation of a UUID cache using a MySQL database. Please refer to {@link
 * #create(Connection, String)} and {@link #create(DataSource, String)} for instances of this
 * class.
 *
 * <p>The implementation performs all requests in a single thread, so calls may block for a short
 * period of time.</p>
 */
public class MySQLCache extends AbstractProfileCache {

    public static final String TABLE_NAME = "uuid_cache";
    private static final Logger log = Logger.getLogger(MySQLCache.class.getCanonicalName());
    private final String tableName;
    private final String queryString;
    private DataSource dataSource;
    private Connection connection;

    private MySQLCache(@Nonnull Object o, @Nonnull String tableName) throws SQLException {
        checkNotNull(o);
        checkNotNull(tableName, "tableName cannot be null.");
        checkArgument(!tableName.isEmpty(), "tableName cannot be empty.");

        if (o instanceof Connection) {
            this.connection = ((Connection) o);
        } else {
            this.dataSource = ((DataSource) o);
        }
        this.tableName = tableName;
        this.queryString = "REPLACE INTO `" + tableName + "` (uuid, name) VALUES (?, ?)";
        createTable();
    }

    /**
     * Creates an instance of {@link MySQLCache} with a {@link DataSource}. The provided {@link
     * DataSource} is used for getting a connection using {@link DataSource#getConnection()}. Once a
     * {@link Connection} is done with, it is then closed, calling {@link Connection#close()}. The
     * table name used for caching is {@link #TABLE_NAME}.
     *
     * @param dataSource data source to use for interacting with the mysql database
     *
     * @return the newly constructed {@link MySQLCache}
     *
     * @throws SQLException thrown if an error occurs whilst creating the tables
     */
    public static MySQLCache create(@Nonnull DataSource dataSource)
            throws SQLException {
        checkNotNull(dataSource, "dataSource cannot be null.");
        return new MySQLCache(dataSource, TABLE_NAME);
    }

    /**
     * Creates an instance of {@link MySQLCache} with a {@link DataSource}. The provided {@link
     * DataSource} is used for getting a connection using {@link DataSource#getConnection()}. Once a
     * {@link Connection} is done with, it is then closed, calling {@link Connection#close()}.
     *
     * @param dataSource data source to use for interacting with the mysql database
     * @param tableName name of the table that will cache the name and uuids
     *
     * @return the newly constructed {@link MySQLCache}
     *
     * @throws SQLException thrown if an error occurs whilst creating the tables
     */
    public static MySQLCache create(@Nonnull DataSource dataSource, @Nonnull String tableName)
            throws SQLException {
        checkNotNull(dataSource, "dataSource cannot be null.");
        return new MySQLCache(dataSource, tableName);
    }

    /**
     * Creates an instance of {@link MySQLCache} with a {@link Connection}. The table name used for
     * caching is {@link #TABLE_NAME}.
     *
     * @param connection connection to use for interacting with the mysql database
     *
     * @return the newly constructed {@link MySQLCache}
     *
     * @throws SQLException thrown if an error occurs whilst creating the tables
     */
    public static MySQLCache create(@Nonnull Connection connection)
            throws SQLException {
        checkNotNull(connection, "connection cannot be null.");
        return new MySQLCache(connection, TABLE_NAME);
    }

    /**
     * Creates an instance of {@link MySQLCache} with a {@link Connection}.
     *
     * @param connection connection to use for interacting with the mysql database
     * @param tableName name of the table that will cache the name and uuids
     *
     * @return the newly constructed {@link MySQLCache}
     *
     * @throws SQLException thrown if an error occurs whilst creating the tables
     */
    public static MySQLCache create(@Nonnull Connection connection, @Nonnull String tableName)
            throws SQLException {
        checkNotNull(connection, "connection cannot be null.");
        return new MySQLCache(connection, tableName);
    }

    @Override
    public void putAll(Iterable<Profile> profiles) {
        try {
            executePut(profiles);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute queries", e);
        }
    }

    @Override
    public ImmutableMap<UUID, Profile> getAllPresent(Iterable<UUID> ids) {
        try {
            return executeGet(ids);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to execute queries", e);
        }

        return ImmutableMap.of();
    }

    /**
     * Create the necessary tables and indices if they do not exist yet. This method is called when a
     * new instance of {@link MySQLCache} is created.
     *
     * @throws SQLException thrown on error
     */
    public void createTable() throws SQLException {
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `" + this.tableName + "` ("
                            + "`uuid` CHAR(36) PRIMARY KEY NOT NULL, "
                            + "`name` VARCHAR(16) NOT NULL UNIQUE KEY)");
        } catch (SQLException e) {
            throw new SQLException("Failed to create table.", e);
        } finally {
            close(conn);
        }
    }

    protected synchronized void executePut(Iterable<Profile> profiles) throws SQLException {
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(this.queryString)) {
            for (Profile profile : profiles) {
                stmt.setString(1, profile.getUniqueId().toString());
                stmt.setString(2, profile.getName());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            close(conn);
        }
    }

    protected ImmutableMap<UUID, Profile> executeGet(Iterable<UUID> ids) throws SQLException {
        Iterator<UUID> it = ids.iterator();
        // It was an empty collection
        if (!it.hasNext()) {
            return ImmutableMap.of();
        }

        StringBuilder builder = new StringBuilder();
        // SELECT ... WHERE ... IN ('abc', 'def', 'ghi');
        builder.append("SELECT name, uuid FROM `").append(this.tableName).append("` WHERE uuid IN ('");
        Joiner.on("', '").skipNulls().appendTo(builder, ids);
        builder.append("');");

        synchronized (this) {
            Connection conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(builder.toString());
                Map<UUID, Profile> map = new HashMap<>();

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    map.put(uuid, new Profile(uuid, rs.getString("name")));
                }

                return ImmutableMap.copyOf(map);
            } finally {
                close(conn);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        if (this.connection != null) {
            return this.connection;
        }
        return this.dataSource.getConnection();
    }

    private void close(Connection connection) throws SQLException {
        // Close the current connection if it was provided by the DataSource.
        if (this.dataSource != null) {
            connection.close();
        }
    }

    /**
     * Gets the table name this {@link MySQLCache} uses to cache uuids.
     *
     * @return table name
     */
    @Nonnull
    public String getTableName() {
        return tableName;
    }
}
