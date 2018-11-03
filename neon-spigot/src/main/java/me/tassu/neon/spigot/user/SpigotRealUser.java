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

package me.tassu.neon.spigot.user;

import me.tassu.neon.common.db.StorageConnector;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.user.AbstractRealUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

import static me.tassu.neon.common.util.ChatColor.color;

public class SpigotRealUser extends AbstractRealUser {

    SpigotRealUser(StorageConnector connector, Scheduler scheduler, UUID uuid) {
        super(connector, scheduler, uuid, Bukkit.getPlayer(uuid) == null ? null : Bukkit.getPlayer(uuid).getName());

        if (this.getName() == null) {
            scheduler.delay(25, () -> {
                if (Bukkit.getPlayer(uuid) != null) {
                    this.setName(Bukkit.getPlayer(uuid).getName());
                }
            });
        }
    }

    @Override
    public void kick(String reason) {
        scheduler.sync(() -> bukkit().getPlayer().kickPlayer(color(reason)));
    }

    private OfflinePlayer bukkit() {
        return Bukkit.getOfflinePlayer(getUuid());
    }

    @Override
    public boolean isOnline() {
        return bukkit().isOnline();
    }

    @Override
    public boolean hasPermission(String permission) {
        if (!isOnline()) return false;
        return bukkit().getPlayer().hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        if (!isOnline()) return;
        scheduler.sync(() -> bukkit().getPlayer().sendMessage(color(message)));
    }
}
