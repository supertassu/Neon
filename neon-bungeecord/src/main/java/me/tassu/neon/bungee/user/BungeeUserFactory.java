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

package me.tassu.neon.bungee.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import me.tassu.neon.api.user.User;
import me.tassu.neon.common.db.StorageConnector;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.user.AbstractUserManager;
import me.tassu.neon.common.user.FakeUser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BungeeUserFactory extends AbstractUserManager {

    @Inject private StorageConnector connector;
    @Inject private Scheduler scheduler;

    private LoadingCache<UUID, BungeeRealUser> cache = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, BungeeRealUser>() {
                @Override
                public BungeeRealUser load(@NonNull UUID key) {
                    return new BungeeRealUser(connector, scheduler, key);
                }
            });

    private FakeUser console = new FakeUser();

    @Inject
    public BungeeUserFactory(StorageConnector connector) {
        super(connector);
    }

    @Override
    public @NonNull User getConsoleUser() {
        return console;
    }

    @Override
    public @NonNull User getUser(@NonNull UUID uuid) {
        if (uuid.compareTo(console.getUuid()) == 0) return console;
        return cache.getUnchecked(uuid);
    }

}
