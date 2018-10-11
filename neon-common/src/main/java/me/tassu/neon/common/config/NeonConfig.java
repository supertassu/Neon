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

package me.tassu.neon.common.config;

import com.google.inject.Inject;
import lombok.Getter;
import me.tassu.neon.common.db.StorageConfig;
import me.tassu.util.config.AbstractConfig;
import me.tassu.util.config.ConfigFactory;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NeonConfig extends AbstractConfig<NeonConfig> {

    public static final int CONFIG_VERSION = 1;

    @Inject
    public NeonConfig(@NonNull ConfigFactory factory) {
        // get configuration loader for "neon.conf"
        loader = factory.getLoader("neon.conf");

        try {
            // logic handled by AbstractConfig
            this.configMapper = ObjectMapper.forObject(this);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @ConfigSerializable
    public static class Config {
        @Setting("storage") @Getter
        private StorageConfig storageConfig = new StorageConfig();
    }

    @Getter @Setting("neon")
    private Config config = new Config();

    @Setting("config-version") @Getter
    public int configVersion = CONFIG_VERSION;

}
