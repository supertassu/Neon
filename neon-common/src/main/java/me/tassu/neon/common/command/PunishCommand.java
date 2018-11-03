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

package me.tassu.neon.common.command;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import lombok.experimental.var;
import lombok.val;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.punishment.SimplePunishmentType;
import me.tassu.neon.api.user.RealUser;
import me.tassu.neon.api.user.User;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.command.meta.Command;
import me.tassu.neon.common.config.MessageConfig;
import me.tassu.neon.common.scheduler.Scheduler;

import java.util.List;

import static me.tassu.neon.common.util.DurationParser.parse;
import static me.tassu.util.ArrayUtil.withoutFirst;

public class PunishCommand extends Command {

    @Inject
    private MessageConfig locale;

    @Inject
    private Scheduler scheduler;

    @Inject
    private UserManager userManager;

    @Inject
    private PunishmentManager punishmentManager;

    @Override
    public String[] names() {
        return new String[] {"punish"};
    }

    @Override
    public void run(User sender, String label, List<String> args) throws Exception {
        if (!sender.hasPermission("neon.command.punish")) {
            sender.sendMessage(locale.getLocale().getCommand().getPermissionMessage());
            return;
        }

        scheduler.async(() -> {
            if (args.size() < 2) {
                sender.sendMessage("Usage: /" + label + " <target> <type> [length] [reason...]"); // todo
                return;
            }

            val target = userManager.getUser(args.get(0));

            if (target == null || !target.isRealPlayer()) {
                sender.sendMessage("No such target " + args.get(0));
                return;
            }

            val type = SimplePunishmentType.match(args.get(1));

            if (type == null) {
                sender.sendMessage("No such type");
                return;
            }

            val rawReason = withoutFirst(withoutFirst(args));
            var expires = -1L;
            var reason = "No reason specified";

            if (!rawReason.isEmpty()) {
                val parsed = parse(rawReason.get(0));
                if (parsed > 0) {
                    expires = System.currentTimeMillis() + parsed;
                    rawReason.remove(0);
                }
            }

            if (!rawReason.isEmpty()) {
                reason = Joiner.on(' ').join(rawReason);
            }

            punishmentManager.createPunishment((RealUser) target, userManager.getConsoleUser(), expires, reason, type);
        });
    }
}
