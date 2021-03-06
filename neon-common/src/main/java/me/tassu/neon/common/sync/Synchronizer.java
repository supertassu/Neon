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

package me.tassu.neon.common.sync;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.tassu.neon.common.config.NeonConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

@Singleton
public class Synchronizer {

    @Inject private SynchronizerFactory factory;
    @Inject private NeonConfig config;

    private ISynchronizer impl;

    public void setup() {
        close();

        if (config.getConfig().isEnableBungeeSync()) {
            impl = factory.getBungeeSynchronizer();
            impl.open();
        } else {
            impl = null;
        }
    }

    public void close() {
        if (impl != null) impl.close();
    }

    public boolean isAvailable() {
        return impl != null;
    }

    @Nullable
    public String getImplementationName() {
        if (isAvailable()) return impl.getImplementationName();
        return null;
    }

    public void kick(long id) {
        if (isAvailable()) impl.kick(id);
    }

    public void sync(@NonNull UUID uuid) {
        if (isAvailable()) impl.sync(uuid);
    }

    public void broadcast(long id) {
        if (isAvailable()) impl.broadcast(id);
    }
}
