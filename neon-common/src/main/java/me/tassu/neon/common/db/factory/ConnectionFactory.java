/*
 * This file is part of Neon, licensed under the MIT License.
 *
 * Copyright (c) 2018 Tassu <hello@tassu.me>
 * Copyright (c) 2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tassu.neon.common.db.factory;

import com.google.inject.Inject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.val;
import me.tassu.neon.common.config.NeonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ConnectionFactory {

    @Inject private Logger logger;
    @Inject protected NeonConfig config;

    private HikariDataSource hikari;

    protected void appendProperties(HikariConfig config) {
        for (Map.Entry<String, String> property : this.config.getConfig().getStorageConfig().getProperties().entrySet()) {
            config.addDataSourceProperty(property.getKey(), property.getValue());
        }
    }

    protected void appendConfigurationInfo(HikariConfig config) {
        val addressSplit = this.config.getConfig().getStorageConfig().getAddress().split(":");
        val address = addressSplit[0];
        val port = addressSplit.length > 1 ? addressSplit[1] : "3306";

        config.setDataSourceClassName(getDriverClass());
        config.addDataSourceProperty("serverName", address);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", this.config.getConfig().getStorageConfig().getDatabase());

        config.setUsername(this.config.getConfig().getStorageConfig().getUsername());
        config.setPassword(this.config.getConfig().getStorageConfig().getPassword());
    }

    public abstract String getImplementationName();
    protected abstract String getDriverClass();

    public abstract Function<String, String> getStatementProcessor();

    public void init() {
        // hacky code to make hikari use a proper logger
        try {
            val dataSourceClass = HikariDataSource.class;
            val loggerField = dataSourceClass.getDeclaredField("LOGGER");
            loggerField.setAccessible(true);

            val modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);

            loggerField.set(null, LoggerFactory.getLogger("Neon/DB"));
        } catch (Exception ex) {
            logger.warn("Could not hijack Hikari logger!", ex);
        }

        val config = new HikariConfig();
        config.setPoolName("neon-database-pool");

        appendConfigurationInfo(config);
        appendProperties(config);

        config.setMaximumPoolSize(this.config.getConfig().getStorageConfig().getMaxPoolSize());
        config.setMinimumIdle(this.config.getConfig().getStorageConfig().getMinIdleConnections());
        config.setMaxLifetime(this.config.getConfig().getStorageConfig().getMaxLifetime());
        config.setConnectionTimeout(this.config.getConfig().getStorageConfig().getConnectionTimeout());

        // don't perform any initial connection validation - we subsequently call #getConnection
        // to setup the schema anyways
        config.setInitializationFailTimeout(-1);

        this.hikari = new HikariDataSource(config);
    }

    public Map<String, String> getMeta() {
        val ret = new LinkedHashMap<String, String>();
        boolean success = true;

        val start = System.currentTimeMillis();
        try (Connection c = getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute("/* ping */ SELECT 1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        }

        val duration = System.currentTimeMillis() - start;
        if (success) {
            ret.put("Ping", duration + "ms");
            ret.put("Connected", "true");
        } else {
            ret.put("Connected", "false");
        }

        return ret;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = this.hikari.getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool.");
        }
        return connection;
    }

    public void shutdown() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

}
