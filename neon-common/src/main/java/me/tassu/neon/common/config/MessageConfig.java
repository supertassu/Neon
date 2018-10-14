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

package me.tassu.neon.common.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import lombok.Getter;
import me.tassu.neon.api.punishment.SimplePunishmentType;
import me.tassu.util.config.AbstractConfig;
import me.tassu.util.config.ConfigFactory;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;

public class MessageConfig extends AbstractConfig<MessageConfig> {

    @Inject
    public MessageConfig(@NonNull ConfigFactory factory) {
        // get configuration loader for "locale.conf"
        loader = factory.getLoader("locale.conf");

        try {
            // logic handled by AbstractConfig
            this.configMapper = ObjectMapper.forObject(this);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Setting private Neon neon = new Neon();

    @ConfigSerializable
    private static class Neon {
        @Setting
        private Locale locale = new Locale();
    }

    public Locale getLocale() {
        return neon.locale;
    }

    @ConfigSerializable
    @Getter
    public static class Locale {

        @Setting("kick.permanent")
        private Map<String, List<String>> permanentKickMessages = ImmutableMap.<String, List<String>>builder()
                .put(SimplePunishmentType.BAN.getId(), Lists.newArrayList(
                        "&8&m   &8[&r You are banned from this server! &8]&m   &r",
                        "",
                        "&6&lBanned by &7{{actor}}",
                        "&6&lReason: &7{{reason}}",
                        "&7This ban will not expire.", "",
                        "&7You may &6appeal&7 at&r www.example.com/appeal&7."))
                .build();

        @Setting("kick.temp")
        private Map<String, List<String>> tempKickMessages = ImmutableMap.<String, List<String>>builder()
                .put(SimplePunishmentType.BAN.getId(), Lists.newArrayList(
                        "&8&m   &8[&r You are banned from this server! &8]&m   &r",
                        "",
                        "&6&lBanned by &7{{actor}}",
                        "&6&lReason: &7{{reason}}",
                        "&6&lExpires in &7{{expires}}.", "",
                        "&7You may &6appeal&7 at&r www.example.com/appeal&7."))
                .build();

    }

}
