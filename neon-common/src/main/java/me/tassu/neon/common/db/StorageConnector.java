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

package me.tassu.neon.common.db;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.experimental.var;
import lombok.val;
import me.tassu.neon.common.config.NeonConfig;
import me.tassu.neon.common.db.factory.ConnectionFactory;
import me.tassu.neon.common.plugin.NeonPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.function.Function;

import static me.tassu.util.ErrorUtil.run;

@Singleton
public class StorageConnector {

    @Getter private ConnectionFactory factory;
    @Inject private Injector injector;
    @Inject private NeonPlugin plugin;
    @Inject private NeonConfig config;

    @Getter
    private Function<String, String> statementProcessor;

    public void startup() {
        if (factory != null) throw new IllegalStateException("already connected");

        run(() -> factory = config.getConfig().getStorageConfig().getType().getConnectionFactory().newInstance());
        injector.injectMembers(factory);

        factory.init();

        this.statementProcessor = factory.getStatementProcessor()
                .compose(s -> s.replace("{prefix}", config.getConfig().getStorageConfig().getTablePrefix()));
    }

    public void teardown() {
        if (factory != null) factory.shutdown();
    }

    public void init() {
        run(() -> {
            if (!tableExists("{prefix}_punishments")) {
                try (val is = plugin.getResourceStream("schemas/" + factory.getImplementationName().toLowerCase() + ".schema")) {
                    if (is == null) {
                        throw new Exception("Couldn't locate schema file for " + factory.getImplementationName());
                    }

                    try (val reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        try (val connection = factory.getConnection()) {
                            try (val s = connection.createStatement()) {
                                var sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("--") || line.startsWith("#")) continue;

                                    sb.append(line);

                                    // check for end of declaration
                                    if (line.endsWith(";")) {
                                        sb.deleteCharAt(sb.length() - 1);

                                        val result = this.statementProcessor.apply(sb.toString().trim());
                                        if (!result.isEmpty()) s.addBatch(result);

                                        // reset
                                        sb = new StringBuilder();
                                    }
                                }

                                s.executeBatch();
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean tableExists(@SuppressWarnings("SameParameterValue") String table) throws SQLException {
        try (val connection = this.factory.getConnection()) {
            try (val rs = connection.getMetaData().getTables(null, null, "%", null)) {
                while (rs.next()) {
                    if (rs.getString(3).equalsIgnoreCase(table)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

}
