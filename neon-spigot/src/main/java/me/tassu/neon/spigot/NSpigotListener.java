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
import lombok.val;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.punishment.SimplePunishmentType;
import me.tassu.neon.api.user.RealUser;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.common.punishment.PunishmentHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import static me.tassu.neon.common.util.ChatColor.color;

public class NSpigotListener implements Listener {

    @Inject private UserManager userManager;
    @Inject private PunishmentManager punishmentManager;

    @Inject private PunishmentHandler handler;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void temp(AsyncPlayerChatEvent event) {
        punishmentManager.createPunishment((RealUser) userManager.getUser(event.getPlayer().getUniqueId()), userManager.getConsoleUser(),
                System.currentTimeMillis() + 60000, "Debugging", SimplePunishmentType.BAN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        val kick = handler.onJoin(event.getUniqueId());
        if (kick.isPresent()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(color(kick.orElse("no")));
        }
    }
}
