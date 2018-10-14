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

package me.tassu.neon.spigot;

import com.google.inject.Inject;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.punishment.SimplePunishmentType;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.plugin.NeonPlugin;
import me.tassu.neon.spigot.task.HousekeeperTask;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.util.UUID;

public final class NSpigotPlugin extends NeonPlugin {

    @Inject private NSpigotBootstrap plugin;
    @Inject private NSpigotScheduler scheduler;
    @Inject private HousekeeperTask housekeeper;

    @Inject private PluginManager manager;
    @Inject private NSpigotListener listener;

    @Inject private UserManager userManager;
    @Inject private PunishmentManager punishmentManager;

    public NSpigotPlugin(@NonNull NSpigotBootstrap bootstrap) {
        super(bootstrap);

        if (plugin == null) {
            throw new IllegalStateException("injection failed");
        }
    }

    @Override
    public void startup() {
        super.startup();

        if (plugin == null) {
            throw new IllegalStateException("injection failed");
        }

        scheduler.schedule(housekeeper);
        manager.registerEvents(listener, plugin);

        System.out.println(punishmentManager.createPunishment(userManager.getUser(UUID.fromString("c19bbd36-3fd0-4b30-b40c-89f70e989dcb")),
                userManager.getConsoleUser(), System.currentTimeMillis() + 3600000, "Being a great developer", SimplePunishmentType.BAN));
    }

    @Override
    @Nullable
    public InputStream getResourceStream(@NonNull String path) {
        return plugin.getResource(path);
    }
}
