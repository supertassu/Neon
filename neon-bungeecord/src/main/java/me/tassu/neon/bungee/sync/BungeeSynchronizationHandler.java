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

package me.tassu.neon.bungee.sync;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.experimental.var;
import lombok.val;
import me.tassu.neon.api.punishment.Punishment;
import me.tassu.neon.api.punishment.PunishmentManager;
import me.tassu.neon.api.user.RealUser;
import me.tassu.neon.api.user.UserManager;
import me.tassu.neon.bungee.NBungeeBootstrap;
import me.tassu.neon.common.punishment.PunishmentHandler;
import me.tassu.neon.common.scheduler.Scheduler;
import me.tassu.neon.common.sync.ISynchronizer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.slf4j.Logger;

import java.util.UUID;

import static me.tassu.neon.common.util.ChatColor.color;

@Singleton
public class BungeeSynchronizationHandler implements ISynchronizer, Listener {

    private static final String CHANNEL = "neon:update";

    @Inject private Logger logger;
    @Inject private ProxyServer proxy;
    @Inject private Scheduler scheduler;
    @Inject private NBungeeBootstrap plugin;
    @Inject private UserManager userManager;
    @Inject private PunishmentHandler handler;
    @Inject private PunishmentManager punishmentManager;

    @Override
    public void sync(UUID uuid) {
        scheduler.async(() -> {
            val user = userManager.getUser(uuid);
            if (!(user instanceof RealUser)) return;
            if (!((RealUser) user).isOnline()) return;

            val ban = punishmentManager.getActivePunishments(user)
                    .stream().filter(it -> it.getType().shouldPreventJoin()).findAny();
            ban.ifPresent(punishment -> ((RealUser) user).disconnect(handler.getKickMessage(punishment)));
        });
    }

    @Override
    public void kick(long id) {
        val kick = punishmentManager.getPunishmentById(id);
        if (kick == null) throw new RuntimeException("tried to kick but no kick was found");

        val user = (RealUser) kick.getTarget();
        user.disconnect(handler.getKickMessage(kick));
    }

    @Override
    public void broadcast(long id) {
        val punishment = punishmentManager.getPunishmentById(id);
        if (punishment != null) syncBroadcast(punishment);
    }

    private void syncBroadcast(Punishment punishment) {
        val broadcast = handler.getBroadcast(punishment);
        for (String message : broadcast) {
            proxy.broadcast(TextComponent.fromLegacyText(color(message)));
        }
    }

    @Override
    public void open() {
        proxy.getPluginManager().registerListener(plugin, this);
        proxy.registerChannel(CHANNEL);
    }

    @Override
    public void close() {
        proxy.unregisterChannel(CHANNEL);
        proxy.getPluginManager().unregisterListener(this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL)) {
            return;
        }

        event.setCancelled(true);

        if (event.getSender() instanceof ProxiedPlayer) {
            return;
        }

        val data = event.getData();
        val in = ByteStreams.newDataInput(data);
        val msg = in.readUTF();

        logger.debug("Handling a " + msg + " BungeeSync packet.");
        switch (msg) {
            case "Sync":
                val uuid = UUID.fromString(in.readUTF());
                sync(uuid);
                break;
            case "Broadcast":
                var id = Integer.parseInt(in.readUTF());
                broadcast(id);
                break;
            case "Kick":
                id = Integer.parseInt(in.readUTF());
                kick(id);
                break;
            default:
                logger.warn("Received a message in unknown channel: " + msg);
                break;
        }
    }

    @Override
    public String getImplementationName() {
        return "BungeeCord";
    }
}
