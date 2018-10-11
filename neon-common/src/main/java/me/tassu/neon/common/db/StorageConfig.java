/*
 * This file is part of Neon, licensed under the MIT License.
 *
 * Copyright (c) tassu <hello@tassu.me>
 * Copyright (c) contributors
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

package me.tassu.neon.common.db;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.tassu.neon.common.db.factory.ConnectionFactory;
import me.tassu.neon.common.db.factory.MariaDbConnectionFactory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@Getter
@ConfigSerializable
public class StorageConfig {

    @Setting
    private StorageType type = StorageType.MARIADB;

    @Setting
    private String address = "localhost";

    @Setting(comment = "The name of the database to store LuckPerms data in.\n" +
            " - This must be created already. Don't worry about this setting if you're using MongoDB.")
    private String database = "minecraft";

    @Setting
    private String username = "root";

    @Setting
    private String password = "";

    @Setting(value = "maximum-pool-size", comment = "Sets the maximum size of the MySQL connection pool.\n" +
            " - Basically this value will determine the maximum number of actual\n" +
            "   connections to the database backend.\n" +
            " - More information about determining the size of connection pools can be found here:\n" +
            "   https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing")
    private int maxPoolSize = 10;

    @Setting(value = "minimum-idle", comment = "Sets the minimum number of idle connections that the pool will try to maintain.\n" +
            " - For maximum performance and responsiveness to spike demands, it is recommended to not set\n" +
            "   this value and instead allow the pool to act as a fixed size connection pool.\n" +
            "   (set this value to the same as 'maximum-pool-size')")
    private int minIdleConnections = maxPoolSize;

    @Setting(value = "maximum-lifetime", comment = "This setting controls the maximum lifetime of a connection in the pool in milliseconds.\n" +
            " - The value should be at least 30 seconds less than any database or infrastructure imposed\n" +
            "   connection time limit.")
    private int maxLifetime = 1800000;

    @Setting(value = "connection-timeout", comment = "This setting controls the maximum number of milliseconds that the plugin will wait for a\n" +
            " connection from the pool, before timing out.")
    private int connectionTimeout = 5000;

    @Setting(comment = "This setting allows you to define extra properties for connections.")
    private Map<String, String> properties = ImmutableMap.<String, String>builder()
            .put("useUnicode", "true")
            .put("characterEncoding", "utf8")
            .build();

    @AllArgsConstructor @Getter
    public enum StorageType {
        MARIADB(MariaDbConnectionFactory.class);

        private Class<? extends ConnectionFactory> connectionFactory;
    }

}
