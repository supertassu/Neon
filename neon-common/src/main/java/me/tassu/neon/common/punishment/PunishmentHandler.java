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

package me.tassu.neon.common.punishment;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.val;
import me.tassu.neon.api.punishment.Punishment;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.config.MessageConfig;
import me.tassu.neon.common.util.DurationFormatter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class PunishmentHandler {

    @Inject private MessageConfig locale;

    @Inject private PunishmentManager punishmentManager;
    @Inject private UserManager userManager;

    public Optional<String> onJoin(UUID uuid) {
        val user = userManager.getUser(uuid);
        return onJoin(user);
    }

    private Optional<String> onJoin(User user) {
        val punishments = punishmentManager.getActivePunishments(user)
                .stream().filter(it -> it.getType().shouldPreventJoin()).findAny();
        return punishments.map(this::getKickMessage);
    }

    public String getKickMessage(Punishment punishment) {
        val message = (punishment.willExpire()
                ? locale.getLocale().getKick().getTempKickMessages()
                : locale.getLocale().getKick().getPermanentKickMessages())
                .get(punishment.getType().getId());
        if (message == null) return null;

        return formatMessage(Joiner.on('\n').join(message), punishment);
    }

    public List<String> getBroadcast(Punishment punishment) {
        val message = (punishment.willExpire()
                ? locale.getLocale().getBroadcast().getTempPunishmentMessages()
                : locale.getLocale().getBroadcast().getPermanentPunishmentMessages())
                .get(punishment.getType().getId());
        if (message == null) return Collections.singletonList("missing translation for " + (punishment.willExpire() ? "temp" : "perm") + " " + punishment.getType().getId());

        return message.stream().map(it -> formatMessage(it, punishment)).collect(Collectors.toList());
    }

    private String formatMessage(String message, Punishment punishment) {
        return message
                .replace("{{target}}", nullOrEmpty(punishment.getTarget().getName()))
                .replace("{{actor}}", nullOrEmpty(punishment.getActor().getName()))
                .replace("{{reason}}", nullOrEmpty(punishment.getReason()))
                .replace("{{expires}}", getLengthString(punishment));
    }

    private String getLengthString(Punishment punishment) {
        if (!punishment.willExpire()) return "";
        return DurationFormatter.getRemaining(punishment.getExpiryDate() - System.currentTimeMillis());
    }

    private String nullOrEmpty(String in) {
        if (in == null) return "";
        return in;
    }

}
