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

package me.tassu.neon.common.plugin;

import com.google.inject.Guice;
import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import me.tassu.neon.api.NeonAPI;
import me.tassu.neon.common.config.MessageConfig;
import me.tassu.neon.common.config.NeonConfig;
import me.tassu.neon.common.db.StorageConnector;
import me.tassu.neon.common.db.factory.ConnectionFactory;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.sync.Synchronizer;
import me.tassu.util.ArrayUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;

import static me.tassu.util.ErrorUtil.run;

public abstract class NeonPlugin {

    @Inject private Logger logger;
    @Inject private Platform platform;
    @Inject private Scheduler scheduler;
    @Inject private Synchronizer synchronizer;

    @Inject private MessageConfig locale;
    @Inject private NeonConfig config;

    @Inject private StorageConnector connector;
    @Getter(value = AccessLevel.PRIVATE, lazy = true) private final ConnectionFactory factory = connector.getFactory();

    public NeonPlugin(@NonNull NeonBootstrap bootstrap) {
        val injector = Guice.createInjector(ArrayUtil.join(
                        bootstrap.getPlatformSpecificModules(),
                        new ImplementationModule(bootstrap),
                        new InternalModule(this)
        ));

        injector.injectMembers(this);
        injector.injectMembers(NeonAPI.getInstance());
    }

    public void startup() {
        long startTime = System.currentTimeMillis();
        logger.info("");
        logger.info("§4  /\\ \\ \\___  ___  _ __  ");
        logger.info("§c /  \\/ / _ \\/ _ \\| '_ \\ ");
        logger.info("§c/ /\\  /  __/ (_) | | | |");
        logger.info("§c\\_\\ \\/ \\___|\\___/|_| |_|");
        logger.info("");
        logger.info("§4= §7Booting up Neon §cv" + platform.getPluginVersion()
                + "§7 running on §f" + platform.getPlatformName() + " " + platform.getPlatformVersion() + "§7.");

        logger.info("§4== §7Loading configuration");
        run(() -> {
            locale.load();
            locale.save();

            config.load();
            config.save();
        });

        if (config.getConfigVersion() != NeonConfig.CONFIG_VERSION) {
            logger.error("§4=== §7Unsupported config version §c" + config.getConfigVersion());
            throw new RuntimeException("Unsupported config version " + config.getConfigVersion());
        }

        logger.info("§4== §7Starting scheduler");
        scheduler.boot();

        logger.info("§4== §7Connecting to database");
        connector.startup();
        if (getFactory() == null) {
            logger.error("§4=== §7Could not connect to database!");
            throw new RuntimeException("Could not connect to database!");
        }

        val meta = getFactory().getMeta();

        if (!meta.getOrDefault("Connected", "false").equals("true")) {
            logger.error("§4=== §7Could not connect to database §c" + getFactory().getImplementationName() + "§7.");
            logger.error("§4==== §7This may be caused by one of the following: ");
            logger.error("§4===== §7Your database server is not running");
            logger.error("§4===== §7You have wrong username or password");
            logger.error("§4===== §7You have wrong database name or the database does not exist.");
            throw new RuntimeException("Could not connect to database " + getFactory().getImplementationName());
        }

        logger.info("§4=== §7Connected to database with ping of §c" + meta.getOrDefault("Ping", "(NaN)") + "§7.");

        connector.init();
        logger.info("§4=== §7Database is good to go!");

        logger.info("§4=== §7Starting synchronizer");
        synchronizer.setup();

        logger.info("§4= §7Neon should be good to go. Took §c" + (System.currentTimeMillis() - startTime) + "ms§7.");
    }

    public final void shutdown() {
        if (scheduler != null) scheduler.shutdown();
        if (connector != null) connector.teardown();
        if (synchronizer != null) synchronizer.close();

        if (config != null) run(() -> config.save());
    }

    /**
     * Gets a bundled resource file from the jar
     *
     * @param path the path of the file
     * @return the file as an input stream
     */
    @Nullable
    public abstract InputStream getResourceStream(@NonNull String path);

}
