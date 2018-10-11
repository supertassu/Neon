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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import me.tassu.neon.common.config.NeonConfig;
import me.tassu.neon.common.db.factory.ConnectionFactory;

import static me.tassu.util.ErrorUtil.run;

@Singleton
public class StorageConnector {

    @Getter private ConnectionFactory factory;
    @Inject private Injector injector;
    @Inject private NeonConfig config;

    public void startup() {
        if (factory != null) throw new IllegalStateException("already connected");

        run(() -> factory = config.getConfig().getStorageConfig().getType()
                .getConnectionFactory().getConstructor(NeonConfig.class).newInstance(config));
        injector.injectMembers(factory);

        factory.init();
    }

    public void teardown() {
        factory.shutdown();
    }

}
