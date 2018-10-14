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

package me.tassu.neon.bungee;

import com.google.inject.AbstractModule;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.bungee.sync.BungeeSynchronizerFactory;
import me.tassu.neon.bungee.user.BungeeUserFactory;
import me.tassu.neon.common.plugin.NeonBootstrap;
import me.tassu.neon.common.plugin.Platform;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.sync.SynchronizerFactory;
import net.md_5.bungee.api.plugin.Plugin;

public final class NBungeeBootstrap extends Plugin implements NeonBootstrap {

    private NBungeePlugin plugin;

    @Override
    public void onEnable() {
        plugin = new NBungeePlugin(this);
        plugin.startup();
    }

    @Override
    public void onDisable() {
        plugin.shutdown();
    }

    @Override
    public Class<? extends Platform> getPlatform() {
        return BungeePlatform.class;
    }

    @Override
    public Class<? extends Scheduler> getScheduler() {
        return NBungeeScheduler.class;
    }

    @Override
    public Class<? extends UserManager> getUserManager() {
        return BungeeUserFactory.class;
    }

    @Override
    public Class<? extends SynchronizerFactory> getSynchronizerFactory() {
        return BungeeSynchronizerFactory.class;
    }

    @Override
    public AbstractModule[] getPlatformSpecificModules() {
        return new AbstractModule[] {
                new BungeeModule(this)
        };
    }
}
